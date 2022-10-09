package dev.wndenis.lapka.utils

import androidx.compose.ui.geometry.Offset
import androidx.core.math.MathUtils
import dev.wndenis.lapka.compose.PI_HALF
import kotlin.math.*


class CatCalc(
    val basicConfig: BasicConfig
) {
    val origin = basicConfig.origin
    var tapTarget = Offset(0f, 0f)
        set(value) {
            field = value
            recalculate()
        }

    var distanceToTapTarget: Float = 0f
        private set
    var reachableTarget = Offset(0f, 0f)
        private set
    var elbowAngle: Double = 0.01
        private set
    var shoulderAngle: Double = 0.01
        private set
    var halfHandLength: Double = 0.01
        private set
    var elbowPosition = Offset(0f, 0f)
        private set
    var pawPosition = Offset(0f, 0f)
        private set
    var pawBaseVector = Offset(0f, 0f)
        private set


    fun getBonesState(): BonesState {
        return BonesState(
            startPosition = origin,
            elbowPosition = elbowPosition,
            pawPosition = pawPosition,
            pawRotation = pawBaseVector
        )
    }

    private fun recalculate() {
        calculateDistanceToTapTarget()
        calculateReachableTarget()
        calculateAngles()
        calculateHalfHandLength()
        calculateElbowPosition()
        calculatePawPosition()
        calculatePawBaseVector()
    }

    private fun calculateDistanceToTapTarget() {
        distanceToTapTarget = (tapTarget - origin).len()
    }

    private fun calculateReachableTarget() {
        reachableTarget =
            if (distanceToTapTarget > basicConfig.maxHandLength)
                origin + (tapTarget - origin).normFactor(basicConfig.maxHandLength)
            else tapTarget
    }

    private fun calculateAngles() {
        // distance factor - 0-close, 1-far
        val factor =
            MathUtils.clamp(
                distanceToTapTarget,
                basicConfig.maxHandLength * 0f,
                basicConfig.maxHandLength * 1f
            ) / basicConfig.maxHandLength * 1f

        val dx = abs(reachableTarget.x - origin.x)
        val rawMiddleFactor =
            let {
                val sub = if (reachableTarget.x > origin.x) {
                    basicConfig.screenWidth - origin.x
                } else {
                    origin.x
                }
                dx / (sub / 3)
            }
        val middleFactor = MathUtils.clamp(rawMiddleFactor, 0f, 1f)


        val angleFactor = -((factor * 2 - 1).toDouble().pow(2.0)) + 1
//            y = -(2 x -1)^2+1
//            Log.w("LapkaSin", "${angleFactor}")

        val tempAngleRaw = PI / 2 + PI / 6 + (1 - angleFactor) * PI / 3 - 0.001

        val tempAngle =
            Lerp.lerp((1f - middleFactor), tempAngleRaw, PI).toDouble()  // todo: check UX

        elbowAngle = if (reachableTarget.x < origin.x) tempAngle else 2 * PI - tempAngle

        // 180 - 90 - 180
        // 0 - 1 - 0
        // 1 - 0 - 1
        // 180 - 110 - 180
        shoulderAngle = (PI - abs(elbowAngle)) / 2
    }

    private fun calculateHalfHandLength() {
        // sin theorem
        halfHandLength = (reachableTarget - origin).len() / sin(elbowAngle) * sin(shoulderAngle)
    }

    private fun calculateElbowPosition() {
        elbowPosition = origin + (reachableTarget - origin)
            .normFactor(halfHandLength)
            .rotate(shoulderAngle)
    }

    private fun calculatePawPosition() {
        pawPosition = elbowPosition + (reachableTarget - elbowPosition)
            .normFactor(halfHandLength)
    }

    private fun calculatePawBaseVector() {
        // find 90 degree vector to target-origin
        val dx = reachableTarget.x - origin.x
        val dy = reachableTarget.y - origin.y
        pawBaseVector = Offset(
            x = (cos(PI_HALF) * dx - sin(PI_HALF) * dy).toFloat(),
            y = (sin(PI_HALF) * dx + cos(PI_HALF) * dy).toFloat()
        )
    }
}


object DrawStyleConfig { // hand - paw  - finger - claw
    val handThickness: Pair<Float, Float> =
        Pair(45f, 65f)
    val pawRadius: Pair<Float, Float> =
        Pair(35f, 40f)
    val fingerOffsetFromPaw: Pair<Float, Float> =
        Pair(28f, 38f)
    val fingerRadius: Pair<Float, Float> =
        Pair(14f, 15f)
    val clawLength: Pair<Float, Float> =
        Pair(12f, 25f)
    val clawWidth: Float = 1.5f
    val raiseScale: Float = 0.15f
}


class CatDrawableState(
    val bonesState: BonesState,
    val handRaiseFactor: Float,
    val pawFistFactor: Float
) {

    fun lerp(fraction: Float, fromTo: Pair<Number, Number>): Float {
        return Lerp.lerp(fraction, fromTo.first, fromTo.second)
    }

    val startPosition = bonesState.startPosition
    val elbowPosition = bonesState.elbowPosition
    val pawPosition = bonesState.pawPosition
    val pawRotation = bonesState.pawRotation

    fun scale(value: Float): Float {
        return value * (1 + handRaiseFactor * DrawStyleConfig.raiseScale)
    }

    val firstHandThickness = DrawStyleConfig.handThickness.first

    val secondHandThickness = lerp(
        handRaiseFactor,
        DrawStyleConfig.handThickness
    )
    val pawRadius = scale(
        lerp(
            pawFistFactor,
            DrawStyleConfig.pawRadius
        )
    )
    val fingerOffsetFromPaw = scale(
        lerp(
            pawFistFactor,
            DrawStyleConfig.fingerOffsetFromPaw
        )
    )
    val fingerRadius = scale(
        lerp(
            pawFistFactor,
            DrawStyleConfig.fingerRadius
        )
    )
    val clawLength = scale(
        lerp(
            pawFistFactor,
            DrawStyleConfig.clawLength
        )
    )
    val clawWidth = DrawStyleConfig.clawWidth
}

data class BonesState(
    val startPosition: Offset,
    val elbowPosition: Offset,
    val pawPosition: Offset,
    val pawRotation: Offset,
)


data class BasicConfig(
    val origin: Offset,
    val screenHeight: Float,
    val screenWidth: Float,
    val maxHandLength: Float
)


// TODO: IMPORTANT
data class CatSimpleState(
    val target: Offset,
    val handRaiseFactor: Float = 0f,  // 0..1, 0 - laying, 1 - floating
    val pawFistFactor: Float = 1f  // 0..1, 0 - relaxed, 1 - fist
)


class CatRepresenter(
    private val basicConfig: BasicConfig,  // like screen size, origin
) {
    fun unpack(catSimpleState: CatSimpleState): CatDrawableState {
        val cat = CatCalc(basicConfig)
        cat.tapTarget = catSimpleState.target

        return CatDrawableState(
            cat.getBonesState(),
            catSimpleState.handRaiseFactor,
            catSimpleState.pawFistFactor
        )
    }
}


