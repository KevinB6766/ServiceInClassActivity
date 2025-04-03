package edu.temple.myapplication

import android.content.ComponentName
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private var timerBinder: TimerService.TimerBinder? = null
    val timeHandler = Handler(Looper.getMainLooper()){
        findViewById<TextView>(R.id.textView).text = it.what.toString()
        true
    }

    val serviceConnection = object : ServiceConnection { //service connection
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) { //service connected
            timerBinder = service as TimerService.TimerBinder
            timerBinder?.setHandler(timeHandler) //sets handler
        }

        override fun onServiceDisconnected(name: ComponentName?) { //service disconnected
            timerBinder = null
        }
    }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

        bindService(Intent(this, TimerService::class.java), serviceConnection, BIND_AUTO_CREATE) //binds service

            findViewById<Button>(R.id.startButton).setOnClickListener {//start button
                if (timerBinder?.isRunning == false) //if timer is not running
                    timerBinder?.start(10) //starts timer
            }
            findViewById<Button>(R.id.stopButton).setOnClickListener { //stop button
                timerBinder?.stop() //stops timer
            }
        }
}