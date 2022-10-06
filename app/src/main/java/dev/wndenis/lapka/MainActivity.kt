package dev.wndenis.lapka

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import dev.wndenis.lapka.compose.Fish
import dev.wndenis.lapka.compose.Lapka

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setContentView(R.layout.activity_main)
//        checkOverlayPermission()
//        startService()
//        val handState = HandState()
//        val logic = Logic { handState }
//        logic.start()
        setContent {
            Lapka()
            Fish()
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