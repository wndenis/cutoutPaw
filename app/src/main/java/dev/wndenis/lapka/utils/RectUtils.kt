package dev.wndenis.lapka.utils

import android.graphics.Rect


fun Rect.area(): Int {
    return height() * width()
}