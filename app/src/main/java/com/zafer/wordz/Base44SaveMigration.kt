package com.zafer.wordz

import android.content.Context

/** Versioned, idempotent migrations for the existing wordz_progress SharedPreferences file. */
internal object Base44SaveMigrator {
    private const val KEY_SCHEMA = "b44_save_schema"
    const val CURRENT_SCHEMA = 3

    fun migrate(context: Context) {
        val prefs = context.getSharedPreferences(B44V2_PREFS, Context.MODE_PRIVATE)
        var schema = prefs.getInt(KEY_SCHEMA, 0).coerceAtLeast(0)
        if (schema >= CURRENT_SCHEMA) return

        if (schema < 1) {
            val editor = prefs.edit()
            if (!prefs.contains(B44V2_COINS)) editor.putInt(B44V2_COINS, 500)
            if (!prefs.contains(B44V2_HINTS)) editor.putInt(B44V2_HINTS, 3)
            editor.apply()
            schema = 1
        }

        if (schema < 2) {
            val levelCount = Base44RuntimeLevels.canonicalLevels.size
            val completed = prefs.getStringSet(B44V2_COMPLETED, emptySet()).orEmpty()
                .mapNotNull { it.toIntOrNull() }
                .filter { it in 0 until levelCount }
                .distinct()
                .map(Int::toString)
                .toSet()
            val unlocked = prefs.getInt(B44V2_UNLOCKED, 0)
                .coerceIn(0, (levelCount - 1).coerceAtLeast(0))
            prefs.edit()
                .putStringSet(B44V2_COMPLETED, completed)
                .putInt(B44V2_UNLOCKED, unlocked)
                .apply()
            Base44ProgressStore(prefs).score() // materialize legacy score migration once
            schema = 2
        }

        if (schema < 3) {
            // Reserve a stable schema boundary for daily/speed mode data.
            // Existing mode keys are intentionally left untouched if already present.
            schema = 3
        }

        prefs.edit().putInt(KEY_SCHEMA, schema).apply()
    }
}
