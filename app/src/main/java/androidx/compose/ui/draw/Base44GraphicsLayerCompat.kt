package androidx.compose.ui.draw

import android.content.res.Resources
import androidx.compose.ui.Modifier

class Base44GraphicsLayerScope {
    var rotationY: Float = 0f
    var cameraDistance: Float = 0f
    val density: Float
        get() = Resources.getSystem().displayMetrics.density
}

fun Modifier.graphicsLayer(block: Base44GraphicsLayerScope.() -> Unit): Modifier {
    Base44GraphicsLayerScope().apply(block)
    return this
}
