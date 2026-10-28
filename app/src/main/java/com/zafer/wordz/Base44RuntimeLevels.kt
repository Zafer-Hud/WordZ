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

    @Volatile
    var source: String = "native"
        private set

    val canonicalLevels: List<Base44WordLevel>
        get() = active ?: base44CanonicalLevels

    fun initialize(context: Context) {
        val assetNames = runCatching { context.assets.list("")?.toSet().orEmpty() }.getOrDefault(emptySet())
        if ("word_levels.json" !in assetNames) {
            active = base44CanonicalLevels
            source = "native"
            return
        }

        val result = runCatching {
            val json = context.assets.open("word_levels.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
            val parsed = Base44WordLevelJson.parse(json)
                .filter { it.isActive }
                .sortedBy { it.levelNumber }

            require(parsed.size == 120) {
                "word_levels.json 120 aktif bölüm içermeli; ${parsed.size} bulundu"
            }
            require(parsed.map { it.levelNumber }.distinct().size == parsed.size) { "tekrarlanan level_number var" }
            require(parsed.map { it.levelNumber } == (1..120).toList()) {
                "level_number değerleri 1..120 aralığında eksiksiz ve sıralı olmalı"
            }
            val allowedWorlds = base44CanonicalCountries.map { it.id }.toSet()
            parsed.forEach { level ->
                require(level.country in allowedWorlds) { "Bölüm ${level.levelNumber}: bilinmeyen tema ${level.country}" }
                val issues = validateBase44WordLevel(level)
                require(issues.isEmpty()) { "Bölüm ${level.levelNumber}: ${issues.joinToString("; ")}" }
            }
            parsed
        }

        result.onSuccess {
            active = it
            source = "asset"
            Log.i("WordZLevelImport", "120 özel WordZ bölümü asset'ten yüklendi")
        }.onFailure {
            active = base44CanonicalLevels
            source = "native"
            Log.e("WordZLevelImport", "word_levels.json reddedildi; 120 native tema bölümü kullanılıyor", it)
        }
    }

    fun levelsForCountry(countryId: String): List<Base44WordLevel> =
        canonicalLevels.filter { it.country == countryId }

    fun indexOfLevelNumber(levelNumber: Int): Int =
        canonicalLevels.indexOfFirst { it.levelNumber == levelNumber }
}

/** Initializes canonical level data and persistent save migrations before the launcher Activity. */
class Base44LevelInitProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        context?.applicationContext?.let { appContext ->
            Base44RuntimeLevels.initialize(appContext)
            Base44SaveMigrator.migrate(appContext)
        }
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
