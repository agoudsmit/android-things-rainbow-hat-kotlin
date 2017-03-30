package com.example.androidthings.rainbowhat_demo

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import com.google.android.things.contrib.driver.bmx280.Bmx280
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.ht16k33.Ht16k33
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import com.google.firebase.database.*


class MainActivity : Activity() {
    var redLed: Gpio? = null


    var greenLed : Gpio? = null
    var blueLed : Gpio? = null


    var buttonA: Button? = null
    var buttonB: Button? = null
    var buttonC: Button? = null

    var position: Int = 0

    val shots = IntArray(RainbowHat.LEDSTRIP_LENGTH)
    val game = PlayActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        try {
            bootSequence()
            initButtons()
            for (i in shots.indices) {
                shots[i] = 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, e.message)
        }

        game.initFirebase()
    }


    fun bootSequence() {
        Log.d(TAG, "Hello!!! from Android Things in Kotlin!!!!")

        val manager = PeripheralManagerService()
        val portList = manager.getGpioList()
        if (portList.isEmpty()) {
            Log.i(TAG, "No GPIO port available on this device.")
        } else {
            Log.i(TAG, "List of available ports: " + portList)
        }

        playStartupSound()
        updateDisplay()
        //rainbow(on = false)
        // mode = Kitt("    ", 0)
    }

    fun playStartupSound() {
        // Play a note on the buzzer.
        val buzzer = RainbowHat.openPiezo()
        buzzer.play(880.0)

        Thread.sleep(500)

        // Stop the buzzer.
        buzzer.stop()
        // Close the device when done.
        buzzer.close()
    }

    fun initButtons() {
        redLed = RainbowHat.openLed(RainbowHat.LED_RED)
        redLed!!.value = false

        greenLed = RainbowHat.openLed(RainbowHat.LED_GREEN)
        greenLed!!.value = false

        blueLed = RainbowHat.openLed(RainbowHat.LED_BLUE)
        blueLed!!.value = false

        buttonA = RainbowHat.openButton(RainbowHat.BUTTON_A)
        buttonA!!.setOnButtonEventListener { button, pressed ->
            redLed!!.value = pressed

            if (pressed) {
                moveRight()
                //fbMode!!.setValue(2)
            }
        }

        buttonB = RainbowHat.openButton(RainbowHat.BUTTON_B)
        buttonB!!.setOnButtonEventListener { button, pressed ->
            greenLed!!.value = pressed
//
            if (pressed) {
                fireAtPosition()
                //fbMode!!.setValue(1)
            }
        }

        buttonC = RainbowHat.openButton(RainbowHat.BUTTON_C)
        buttonC!!.setOnButtonEventListener { button, pressed ->
            blueLed!!.value = pressed
            if (pressed) {
                //  fbMode!!.setValue(0)
            }
        }
    }

    private fun fireAtPosition() {
        Log.d(TAG, "firing at: " + position)
        shots[position] = Color.BLUE
        updateDisplay()

        game.fire(position)
    }

    private fun updateDisplay() {
        val ledstrip = RainbowHat.openLedStrip()
        ledstrip.setBrightness(1)
        val display = IntArray(RainbowHat.LEDSTRIP_LENGTH)
        for (i in display.indices) {
            display[i] = shots[i]
        }
        display[position] = Color.YELLOW
        ledstrip.write(display)
        ledstrip.close()
    }

    fun moveRight() {
        Log.d(TAG, "moving to: " + position)
        position++

        if (position >= RainbowHat.LEDSTRIP_LENGTH) {
            position = 0
        }

        updateDisplay();
    }

    // temp sensor always seems to report the same value of 26.711567
    // TODO: test using the python lib
//    fun displayCurrentTemp() {
//        // Log the current temperature
//        val sensor = RainbowHat.openSensor()
//        sensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X)
//        Log.d(TAG, "temperature:" + sensor.readTemperature())
//        // Close the device when done.
//
//        // Display a string on the segment display.
//        val segment = RainbowHat.openDisplay()
//        segment.setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX)
//        segment.display(Math.round(sensor.readTemperature()))
//        segment.setEnabled(true)
//        // Close the device when done.
//        segment.close()
//        sensor.close()
//    }


    override fun onStop() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        buttonA!!.close()
        buttonB!!.close()
        buttonC!!.close()
    }

    companion object {
        private val TAG = MainActivity::class.java!!.getSimpleName()
    }
}

