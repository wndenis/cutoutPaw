package dev.wndenis.lapka.compose

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateOffset
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.math.MathUtils
import dev.wndenis.lapka.ui.theme.LapkaTheme
import dev.wndenis.lapka.utils.len
import dev.wndenis.lapka.utils.normFactor
import dev.wndenis.lapka.utils.rotate
import kotlinx.coroutines.*
import java.lang.Math.pow
import kotlin.math.*
import kotlin.random.Random


enum class HandBehaviour {
    Hidden, Naughty, Hunt
}

abstract class Behavior {
    abstract fun actions()

}

class Logic(
    val stateGet: () -> HandState
) {
    fun start() {

    }
}


//val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
var job: Job? = null

val minDist = 100f
val maxDist = 1000f

val minDelay = 200f
val maxDelay = 1200f

fun randRange(from: Float, to: Float): Float {
    assert(from <= to)
    return (from + Math.random() * (to - from)).toFloat()
}


class HandState(
    startPosition: Offset,
    elbowPosition: Offset?,
    pawPosition: Offset,
    pawRotation: Any,
    clawReleaseFactor: Float,  // 0..1, 0 - hidden, 1 - released
    handRaiseFactor: Float,  // 0..1, 0 - laying, 1 - floating
    pawFistFactor: Float,  // 0..1, 0 - relaxed, 1 - fist
) {

}


fun startAnim(
    scope: CoroutineScope,
    targetCallback: (Offset, Boolean) -> Unit,
    stdPosition: Offset,
    drop: Boolean
) {
    return
    if (drop)
        job?.cancel()
    job = scope.launch {
        Log.w("LAPKA", "LAPKATASK")
        delay(100L)
        if (randRange(0f, 100f) < 30f) {
            targetCallback(stdPosition, true)
            delay(100L)
            targetCallback(stdPosition, false)
        } else {
            targetCallback(
                Offset(
                    randRange(minDist, maxDist),
                    randRange(minDist, maxDist)
                ),
                true
            )
            delay(50L)
        }
        delay(randRange(minDelay, maxDelay).toLong())
        startAnim(scope, targetCallback, stdPosition, true)
    }
}


@Composable
fun Fish() {
    var fishPosition by remember { mutableStateOf(Offset(100f, 100f)) }

    Box(modifier = Modifier.fillMaxSize()) {
        var offsetX by remember { mutableStateOf(0f) }
        var offsetY by remember { mutableStateOf(0f) }

        Box(
            Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .size(50.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consumeAllChanges()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                drawOval(
                    color = Color.Gray,
                )
            }
        }
    }
}


fun DrawScope.DrawLapka(state: HandState) {
    return
}

@Composable
fun DrawPaw() {

}


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun Lapka() {
    @SuppressLint("CoroutineCreationDuringComposition")

    var target by remember { mutableStateOf(Offset(0.2f, 0.2f)) }
    var lapkaVisible by remember { mutableStateOf(true) }
    val transition = updateTransition(target, label = "a")
    val currentTarget by transition.animateOffset(
        transitionSpec = {
            spring(
                dampingRatio = 0.7f,
                stiffness = 2500f
            )
        }, label = "b"
    )
    { it }

    val scope = rememberCoroutineScope()



    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInput(key1 = Unit) {
            detectDragGestures(
                onDragStart = { target = it },
                onDrag = { change, _ -> target = change.position }
            )
        }
        .pointerInput(key1 = Unit) {
            detectTapGestures { target = it }
        }) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
//        val centerY = canvasHeight / 2
        val centerY = 50f

        val handThickness = 45f
        val pawRadius = 40f
        val fingerDistance = 38f
        val fingerRadius = 15f

        val clawLen = 25f
        val clawWidth = 1.5f

        val maxHandLength = max(canvasHeight, canvasWidth) / 3

        val from = Offset(centerX, centerY)
        val tapTarget = currentTarget
        val straightDirection = tapTarget - from
        val distanceToTarget = straightDirection.len()

        val reachableTarget =
            if (distanceToTarget > maxHandLength)
                from + (tapTarget - from).normFactor(maxHandLength)
            else tapTarget
        startAnim(scope, { offset, visibility ->
            target = offset
            lapkaVisible = visibility

            Log.w("LAPKATASK", "$target")
        }, from, false)


        val elbowAngle =
            let {
                // distance factor - 0-close, 1-far
                val factor =
                    MathUtils.clamp(
                        distanceToTarget,
                        maxHandLength * 0f,
                        maxHandLength * 1f
                    ) / maxHandLength * 1f
                val angleFactor = -((factor * 2 - 1).toDouble().pow(2.0)) + 1
//                y = -(2 x -1)^2+1
                Log.w("LapkaSin", "${angleFactor}")
                val tempAngle = PI / 2 + PI / 6 + (1 - angleFactor) * PI / 3 - 0.001
                if (reachableTarget.x < centerX) tempAngle else 2 * PI - tempAngle
            }
        // 180 - 90 - 180
        // 0 - 1 - 0
        // 1 - 0 - 1
        // 180 - 110 - 180
        val shoulderAngle = (PI - abs(elbowAngle)) / 2
        // sin theorem
        val halfHandLength = (reachableTarget - from).len() / sin(elbowAngle) * sin(shoulderAngle)

        val handOffset = (reachableTarget - from).normFactor(halfHandLength)
        val firstHandEnd = from + handOffset.rotate(shoulderAngle)
        val lapkaEnd = firstHandEnd + (reachableTarget - firstHandEnd).normFactor(halfHandLength)

        val rightAngle = Offset(
            x = (cos(PI / 2) * (reachableTarget.x - from.x) - sin(PI / 2) * (reachableTarget.y - from.y)).toFloat(),
            y = (sin(PI / 2) * (reachableTarget.x - from.x) + cos(PI / 2) * (reachableTarget.y - from.y)).toFloat()
        )

        val fingerRotated = rightAngle.normFactor(fingerDistance)

        this.DrawLapka(
            state = HandState(
                startPosition = from,
                elbowPosition = firstHandEnd,
                pawPosition = lapkaEnd,
                clawReleaseFactor = 1f,
                handRaiseFactor = 0f,
                pawFistFactor = 0f,
                pawRotation = fingerRotated
            )
        )

        drawLine(
            start = from,
            end = firstHandEnd,
            color = Color.Black,
            strokeWidth = handThickness,
            cap = StrokeCap.Round
        )

        drawLine(
            start = firstHandEnd,
            end = lapkaEnd,
            color = Color.Black,
            strokeWidth = handThickness,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = Color.Black,
            center = reachableTarget,
            radius = pawRadius
        )

        for (i in 0..4) {
            val fingerOffset = fingerRotated.rotate(PI / 4 * i + PI)
            val globalFingerOffset = lapkaEnd + fingerOffset
            drawCircle(
                color = Color.Black,
                center = globalFingerOffset,
                radius = fingerRadius
            )
            val steps = 10
            for (step in 0..steps) {
                drawLine(
                    color = Color.Black,
                    start = globalFingerOffset + fingerOffset.normFactor(clawLen / steps * step),
                    end = globalFingerOffset + fingerOffset.normFactor(clawLen / steps * (step + 1)),
                    strokeWidth = clawWidth * (steps - step)
                )
            }
        }
//            drawRect(
//                color = Color.Black,
//                topLeft = offset,
//                size = Size(clawSize, clawSize)
//            )
//        drawCircle(
//            color = Color.Red,
//            center = Offset(x = canvasWidth / 2, y = canvasHeight / 2),
//            radius = size.minDimension / 4
//        )
    }

}


@Composable
fun Greeting(name: String) {
    Text(text = "Angle $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LapkaTheme {
        Greeting("Android")
    }
}