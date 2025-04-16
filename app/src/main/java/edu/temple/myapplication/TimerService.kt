package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false

    private var timerHandler : Handler? = null

    lateinit var t: TimerThread

    private var paused = false

    private lateinit var sharedPreferences: SharedPreferences //shared preferences
    private var savedValue: Int = 0 //saved value

    companion object {
        const val PREFS_NAME = "TimerPrefs" //shared preferences
        const val COUNTDOWN_KEY = "countdown_value" //value
        const val DEFAULT_VALUE = 100 //default value
    }

    inner class TimerBinder : Binder() {

        // Check if Timer is already running
        val isRunning: Boolean
            get() = this@TimerService.isRunning

        // Check if Timer is paused
        val paused: Boolean
            get() = this@TimerService.paused

        // Start a new timer
        fun start(startValue: Int){

            if (!paused) {
                if (!isRunning) {
                    if (::t.isInitialized) t.interrupt()
                    this@TimerService.start(startValue)
                }
            } else {
                pause()
            }
        }

        // Receive updates from Service
        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        // Stop a currently running timer
        fun stop() {
            if (::t.isInitialized || isRunning) {
                t.interrupt()
            }
        }

        // Pause a running timer
        fun pause() {
            this@TimerService.pause()
        }

    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TimerService status", "Created")
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE) //shared preferences
        savedValue = sharedPreferences.getInt(COUNTDOWN_KEY, DEFAULT_VALUE) //saved value

        Log.d("TimerService status", "Created")
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    fun start(startValue: Int) {
        t = TimerThread(startValue)
        t.start()
    }

    fun pause () {
        if (::t.isInitialized) {
            paused = !paused
            isRunning = !paused
            if (paused) {
                saveCountdownValue(t.currentValue) //saves countdown value
            }
        }
    }

    inner class TimerThread(private val startValue: Int) : Thread() {

        var currentValue: Int = startValue //current value

        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 1)  {
                    currentValue = i //update current value
                    Log.d("Countdown", i.toString())

                    timerHandler?.sendEmptyMessage(i)

                    while (paused);
                    sleep(1000)

                }
                isRunning = false
                saveCountdownValue(DEFAULT_VALUE)
            } catch (e: InterruptedException) {
                Log.d("Timer interrupted", e.toString())
                isRunning = false
                paused = false
                saveCountdownValue(DEFAULT_VALUE)
            }
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (::t.isInitialized) {
            t.interrupt()
        }

        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("TimerService status", "Destroyed")
    }
    private fun saveCountdownValue(value: Int) { //saves countdown value
        with(sharedPreferences.edit()) { //shared preferences
            putInt(COUNTDOWN_KEY, value)
            apply() //saves value
        }
    }


}