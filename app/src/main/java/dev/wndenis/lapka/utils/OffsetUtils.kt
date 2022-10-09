package dev.wndenis.lapka.utils

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun Offset.len(): Float {
    return sqrt(this.x * this.x + this.y * this.y)
}

fun Offset.norm(): Offset {
    val len = this.len()
    return Offset(
        x = this.x / len,
        y = this.y / len
    )
}

fun Offset.normFactor(factor: Float): Offset {
    val norm = this.norm()
    return Offset(
        x = norm.x * factor,
        y = norm.y * factor
    )
}

fun Offset.normFactor(factor: Double): Offset {
    return this.normFactor(factor.toFloat())
}

fun Offset.rotate(angle: Double): Offset {
    return Offset(
        x = (cos(angle) * (this.x) - sin(angle) * (this.y)).toFloat(),
        y = (sin(angle) * (this.x) + cos(angle) * (this.y)).toFloat()
    )
}