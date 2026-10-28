package com.zafer.wordz

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log

object Base44RuntimeLevels {
    @Volatile
    private var active: List<Base44WordLevel>? = null

    val canonicalLevels: List<Base44WordLevel>
        get() = active ?: base44CanonicalLevels

    fun initialize(context: Context) {
        val assetNames = runCatching { context.assets.list("")?.toSet().orEmpty() }.getOrDefault(emptySet())
        if ("word_levels.json" !in assetNames) {
            active = base44CanonicalLevels
            return
        }

        val result = runCatching {
            val json = context.assets.open("word_levels.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            val parsed = Base44WordLevelJson.parse(json)
                .filter { it.isActive }
                .sortedBy { it.levelNumber }

            require(parsed.size == 120) {
                "word_levels.json tam Base44 export'u olmalı: 120 aktif bölüm bekleniyor, ${parsed.size} bulundu"
            }
            require(parsed.map { it.levelNumber }.distinct().size == parsed.size) { "tekrarlanan level_number var" }
            require(parsed.map { it.levelNumber } == (1..120).toList()) {
                "level_number değerleri 1..120 aralığında eksiksiz ve sıralı olmalı"
            }

            parsed.forEach { level ->
                val issues = validateBase44WordLevel(level)
                require(issues.isEmpty()) { "Bölüm ${level.levelNumber}: ${issues.joinToString("; ")}" }
                val expectedCountry = base44CountryForLevel(level.levelNumber).id
                require(level.country == expectedCountry) {
                    "Bölüm ${level.levelNumber}: country '${level.country}', beklenen '$expectedCountry'"
                }
            }
            parsed
        }

        result.onSuccess {
            active = it
            Log.i("WordZLevelImport", "120 canonical Base44 WordLevel asset'ten yüklendi")
        }.onFailure {
            active = base44CanonicalLevels
            Log.e("WordZLevelImport", "word_levels.json reddedildi; güvenli fallback kullanılıyor", it)
        }
    }
}

/** Initializes level data before the launcher Activity is created. */
class Base44LevelInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        context?.applicationContext?.let(Base44RuntimeLevels::initialize)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
