package com.zafer.wordz

import android.content.res.Resources
import androidx.compose.ui.unit.Dp

val Dp.minDimension: Float
    get() = value * Resources.getSystem().displayMetrics.density
