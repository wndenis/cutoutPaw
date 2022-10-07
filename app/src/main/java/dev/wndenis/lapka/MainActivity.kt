package dev.wndenis.lapka

import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.DisplayCutout
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposePath
import androidx.fragment.app.FragmentActivity
import dev.wndenis.lapka.compose.Lapka
import dev.wndenis.lapka.utils.area
import kotlin.math.min
import androidx.compose.ui.graphics.Color as ComposeColor


class MainActivity : FragmentActivity() {
    //    @RequiresApi(Build.VERSION_CODES.Q)
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setContentView(R.layout.activity_main)
//        checkOverlayPermission()
//        startService()
//        val handState = HandState()
//        val logic = Logic { handState }
//        logic.start()

        val outMetrics = DisplayMetrics()
        var displayCutout: DisplayCutout? = null

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val localDisplay = display
            localDisplay?.getRealMetrics(outMetrics)
            displayCutout = localDisplay?.getCutout()

        } else {
            @Suppress("DEPRECATION")
            val localDisplay = windowManager.defaultDisplay
            @Suppress("DEPRECATION")
            localDisplay.getMetrics(outMetrics)
            displayCutout = localDisplay.getCutout()
        }

        val screenWidth = outMetrics.widthPixels
        val screenHeight = outMetrics.heightPixels

        var cutoutPosition = Offset(150f, 150f) // Offset(screenWidth / 2f, 0f)
        var cutoutProjection = 20f
        var path: Path? = null
        if (displayCutout != null && displayCutout.boundingRects.size > 0) {
            val boundings: List<Rect> = displayCutout.boundingRects
            path = displayCutout.cutoutPath
            var biggestIdx = 0
            var biggestArea = 0
            for (i in boundings.indices) {
                val area = boundings[i].area()
                if (area > biggestArea) {
                    biggestArea = area
                    biggestIdx = i
                }
            }
            val bounding = boundings[biggestIdx]
            cutoutProjection =
                min(bounding.right - bounding.left, bounding.bottom - bounding.top).toFloat()
            cutoutPosition =
                Offset(bounding.centerX().toFloat(), bounding.centerY().toFloat())
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContent {
            path?.let { Debug(path) }
//            with(WindowCompat.getInsetsController(window, window.decorView)) {
//                this?.systemBarsBehavior =
//                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//                this?.hide(WindowInsetsCompat.Type.systemBars())
//            }
//            Debug(top.toFloat(), left.toFloat(), width.toFloat(), height.toFloat())
            Lapka(screenWidth.toFloat(), screenHeight.toFloat(), cutoutPosition, cutoutProjection)
//            Fish()
        }
//        hideSystemUI()

    }

    fun hideSystemUI() {

        //Hides the ugly action bar at the top
//        actionBar?.hide()

        //Hide the status bars
//        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.insetsController?.apply {
//                hide(WindowInsets.Type.statusBars())
//                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            window.apply {
//                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                addFlags(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES)
//                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                statusBarColor = Color.TRANSPARENT
            }
        }
    }

    @Composable
//    fun Debug(top: Float, left: Float, width: Float, height: Float) {
    fun Debug(path: Path) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawPath(
                path = path.asComposePath(),
                color = ComposeColor.Red
            )
//            drawRect(
//                color = ComposeColor.Cyan,
//                topLeft = Offset(left, top),
//                size = Size(width, height)
//            )
        }
    }

    fun startService() {
        return
        // check if the user has already granted
        // the Draw over other apps permission
        if (Settings.canDrawOverlays(this)) {
            // start the service based on the android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, ForegroundService::class.java))
            } else {
                startService(Intent(this, ForegroundService::class.java))
            }
        }
    }

    // method to ask user to grant the Overlay permission
    fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            // send user to the device settings
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(myIntent)
        }
    }

    // check for permission again when user grants it from
    // the device settings, and start the service
    override fun onResume() {
        super.onResume()
        checkOverlayPermission()
        startService()
    }
}