package dev.wndenis.lapka.utils

import android.animation.FloatEvaluator


class Lerp {
    companion object {
        private val evaluator = FloatEvaluator()
        public fun lerp(fraction: Float, from: Number, to: Number): Float {
            return evaluator.evaluate(fraction, from, to)
        }
    }
}