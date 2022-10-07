package dev.wndenis.lapka.lapka

import androidx.compose.ui.geometry.Offset
import ru.nsk.kstatemachine.createStateMachine

class CatBrain {

}

val machine = createStateMachine(name = "CatBrain") { }


enum class Actions{
    nearPatOnceSlow,
    nearPatOnceFast,
    nearPatMulti,
    farPatOnceFast,
    tapSlow,
    tapFast,
    grabSlow,
    grabFast
}

class Animations{
    fun nearPatOnceSlow(target: Offset) {

    }
}

// todo: important
//val s = LaunchedEffect(key1 = Unit){
//
//}

//fun anim(scope: CoroutineScope){
//    scope.launch {
//        animate(1f, 0.9f) { value: Float, _: Float ->
//            scale = value
//        }
//    }
//}


/*
    nearPatOnceSlow
    nearPatOnceFast
    nearPatMulti
    farPatOnceFast
    tapSlow





 */

//class AnimTask(
//    handState: HandState
//)