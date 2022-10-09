package dev.wndenis.lapka.lapka

import dev.wndenis.lapka.utils.weightedMapOf


enum class Actions{
    nearPat,
    tap,
    grab,
    idle
}

enum class TargetTypes{
    near,
    medium,
    far
}


val actionWeighted =  weightedMapOf(
    Actions.nearPat to 4f,
    Actions.tap to 1f,
    Actions.grab to 0f,
    Actions.idle to 10f
)

data class NearPatConfig(
    val timesMin: Int = 1,
    val timesMax: Int = 5,
    val speed: Float = 10f
)


class Logic(
    val actionCooldownMsMin: Long = 2000L,
    val actionCooldownMsMax: Long = 8000L,
    ){
    val nearPatConfig = NearPatConfig()
    fun nextState(): Actions {
        return actionWeighted.pickWeighted()
    }

//    fun getNearPat(currentAnimTarget: AnimTarget, tapPoint: Offset): KeyframesSpec<AnimTarget> {
//        return keyframes<AnimTarget> {
//            currentAnimTarget.copy(animatableProperties = currentAnimTarget.animatableProperties)
//        }
//    }
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

//class AnimTask(
//    handState: CatDrawableState
//)