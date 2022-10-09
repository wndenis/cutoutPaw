package dev.wndenis.lapka.utils

import android.animation.FloatEvaluator

fun randRange(from: Float, to: Float): Float {
    assert(from <= to)
    return (from + Math.random() * (to - from)).toFloat()
}

class Lerp {
    companion object {
        private val evaluator = FloatEvaluator()
        public fun lerp(fraction: Float, from: Number, to: Number): Float {
            return evaluator.evaluate(fraction, from, to)
        }
    }
}


public fun <K> weightedMapOf(vararg pairs: Pair<K, Float>): WeightedMap<K> {
    val underlying = pairs.associate { it.first to it.second }
    return WeightedMap<K>(underlying)
}

fun <T> Iterable<T>.reductions(operation: (acc: T?, T) -> T): Sequence<T> = sequence {
    var last: T? = null
    forEach {
        last = operation(last, it)
        yield(last!!)
    }
}

class WeightedMap<K>(private val underlying: Map<K, Float>) : Map<K, Float> {
    override val entries: Set<Map.Entry<K, Float>> = underlying.entries
    override val keys: Set<K>
        get() = underlying.keys
    override val size: Int
        get() = underlying.size
    override val values: Collection<Float>
        get() = underlying.values

    val totalWeight = underlying.values.sum()
    val keyToThreshold =
        underlying.entries
            .sortedByDescending { it.value }
            .map { it.toPair() }
            .reductions { acc, new ->
                if (acc == null) {
                    new
                } else {
                    new.copy(second = acc.second + new.second)
                }
            }.associate { it.first to it.second }


    fun pickWeighted(): K {
        val randNum = randRange(0f, totalWeight)
        for (elem in keyToThreshold.entries)
            if (elem.value < randNum)
                return elem.key
        return keyToThreshold.keys.first()
//        throw Exception("Unable to pick element")
    }

    override fun containsKey(key: K): Boolean {
        return underlying.containsKey(key)
    }

    override fun containsValue(value: Float): Boolean {
        return underlying.containsValue(value)
    }

    override fun get(key: K): Float? {
        return underlying.get(key)
    }

    override fun isEmpty(): Boolean {
        return underlying.isEmpty()
    }
}
