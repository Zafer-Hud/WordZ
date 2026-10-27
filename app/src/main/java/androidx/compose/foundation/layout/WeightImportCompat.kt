package androidx.compose.foundation.layout

/**
 * Compatibility marker for source files that import `androidx.compose.foundation.layout.weight`
 * on Compose versions where the package also exposes an internal symbol with that name.
 * Actual `Modifier.weight(...)` calls still resolve to RowScope/ColumnScope extensions.
 */
@Suppress("unused")
val weight: Unit = Unit
