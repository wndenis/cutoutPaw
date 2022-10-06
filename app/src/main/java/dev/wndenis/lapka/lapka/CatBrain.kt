package dev.wndenis.lapka.lapka

import dev.wndenis.lapka.compose.HandState
import ru.nsk.kstatemachine.createStateMachine
import ru.nsk.kstatemachine.state

class CatBrain {

}

val machine = createStateMachine(name = "CatBrain") { }


/*
    nearPatOnceSlow
    nearPatOnceFast
    nearPatMulti
    farPatOnceFast
    tapSlow





 */

class AnimTask(
    handState: HandState
)