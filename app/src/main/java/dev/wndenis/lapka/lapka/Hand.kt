package dev.wndenis.lapka.compose

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateOffset
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.wndenis.lapka.ui.theme.LapkaTheme
import dev.wndenis.lapka.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.roundToInt


const val PI_HALF = PI / 2


//val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
var job: Job? = null

val minDist = 100f
val maxDist = 1000f

val minDelay = 200f
val maxDelay = 1200f


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
    return
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


fun DrawScope.drawScaledLine(
    start: Offset,
    end: Offset,
    startWidth: Float,
    endWidth: Float,
    steps: Int = 10,
    color: Color,
    cap: StrokeCap
) {
    val dOffset = end - start
    val distance = dOffset.len()
    val direction = dOffset.norm()
    val dWidth = endWidth - startWidth
    var prev = start
    var width = startWidth
    val step = 1f / steps
    for (i in 0 until steps) {
        val next = prev + direction * distance * step
        drawLine(
            start = prev,
            end = next,
            color = color,
            strokeWidth = width,
            cap = cap
        )
//        val furPath = Path()
//        furPath.addRect(Rect(0f, 0f, 10f, 1f))
//        drawLine(
//            start = prev,
//            end = next,
//            color = color,
//            strokeWidth = width,
//            cap = cap,
//            pathEffect = PathEffect.stampedPathEffect(
//                shape=furPath,
//                advance = 1f,
//                phase = 1f,
//                style = StampedPathEffectStyle.Morph)
//        )
        width += dWidth * step
        prev = next
    }
}


fun DrawScope.DrawLapka(state: CatDrawableState, color: Color = Color.Black) {
    val middleWidth = state.firstHandThickness + (state.secondHandThickness - state.firstHandThickness) / 2
    drawScaledLine(
        start = state.startPosition,
        end = state.elbowPosition,
        color = color,
        startWidth = state.firstHandThickness,
        endWidth = middleWidth,
        cap = StrokeCap.Round
    )

    drawScaledLine(
        start = state.elbowPosition,
        end = state.pawPosition,
        color = color,
        startWidth = middleWidth,
        endWidth = state.secondHandThickness,
        cap = StrokeCap.Round
    )

    drawCircle(
        color = color,
        center = state.pawPosition,
        radius = state.pawRadius
    )

    val fingerRotated = state.pawRotation.normFactor(state.fingerOffsetFromPaw)


    for (i in 0..4) {
        val fingerOffset = fingerRotated.rotate(PI / 4 * i + PI)
        val globalFingerOffset = state.pawPosition + fingerOffset
        drawCircle(
            color = color,
            center = globalFingerOffset,
            radius = state.fingerRadius
        )
        val steps = 10
        for (step in 0..steps) {
            drawLine(
                color = color,
                start = globalFingerOffset + fingerOffset.normFactor(state.clawLength / steps * step),
                end = globalFingerOffset + fingerOffset.normFactor(state.clawLength / steps * (step + 1)),
                strokeWidth = state.clawWidth * (steps - step)
            )
        }
    }
}


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
//fun Lapka(target, action) {
fun Lapka(screenWidth: Float, screenHeight: Float, cutoutPosition: Offset, maxHandWidth: Float) {
    @SuppressLint("CoroutineCreationDuringComposition")


    val basicConfig = BasicConfig(
        origin = cutoutPosition,
        screenHeight = screenHeight,
        screenWidth = screenWidth,
        maxHandLength = max(screenHeight, screenWidth) / 3f
    )

    val catRepresenter = CatRepresenter(basicConfig)


    var cat by remember {
        mutableStateOf(
            CatSimpleState(
                target = basicConfig.origin + Offset(0f, 1000f),
            )
        )
    }

//    var target by remember { mutableStateOf(Offset(0.2f, 0.2f)) }
//    var lapkaVisible by remember { mutableStateOf(true) }

//    var animatableHandProperties by remember {
//        mutableStateOf(
//            AnimatableHandProperties()
//        )
//    }
//    val catState = remember { MutableTransitionState(cat) }
//
//    val trans = updateTransition(transitionState = st)
//
    val transition = updateTransition(cat, label = "a")

    val currentTarget by transition.animateOffset(
        transitionSpec = {
            spring(
                dampingRatio = 0.7f,
                stiffness = 2500f
            )
        }, label = "b"
    )
    { it.target }


//    val scope = rememberCoroutineScope()
    Scaffold(
//        backgroundColor = Color.Transparent
        backgroundColor = Color.White
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(key1 = Unit) {
                    detectDragGestures(
                        onDragStart = { cat = cat.copy(target = it) },
                        onDrag = { change, _ -> cat = cat.copy(target = change.position) }
                    )
                }
                .pointerInput(key1 = Unit) {
                    detectTapGestures { cat = cat.copy(target = it) }
                }
        ) {
            val localCat = cat.copy(target = currentTarget)
            val state = catRepresenter.unpack(localCat)
            val shadowState = catRepresenter.unpack(
                localCat.copy(
                    target = localCat.target + Offset(10f, 10f) * localCat.handRaiseFactor
                )
            )
            this.DrawLapka(shadowState, Color(182, 182, 182))
            this.DrawLapka(
                state = state
            )
        }

        Column(
            modifier = Modifier
                .padding(30.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Slider(
                value = cat.pawFistFactor,
                onValueChange = {
                    cat = cat.copy(pawFistFactor = it)
                },
                valueRange = 0f.rangeTo(1f)
            )
            Slider(
                value = cat.handRaiseFactor,
                onValueChange = {
                    cat = cat.copy(handRaiseFactor = it)
                },
                valueRange = 0f.rangeTo(1f)
            )
        }

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