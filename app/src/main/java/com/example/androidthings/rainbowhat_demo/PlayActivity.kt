package com.example.androidthings.rainbowhat_demo

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import com.google.android.things.contrib.driver.apa102.Apa102
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.firebase.database.*

class PlayActivity : Activity() {

    val player = 1
    val otherPlayer = when(player) {
        1 -> 2
        else -> 1
    }

    private val mDatabase by lazy { FirebaseDatabase.getInstance() }
    private val loseRef by lazy { mDatabase.getReference("loser") }

    var otherShipIndex: Int? = null

    val ledStrip by lazy { RainbowHat.openLedStrip() }
    val leds = IntArray(RainbowHat.LEDSTRIP_LENGTH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        initFirebase()
    }

    fun initFirebase() {
        // Wij zijn p1

        val shots = mDatabase.getReference("p$otherPlayer/shots")
        shots.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                shoot(dataSnapshot)
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
            }
        })


        val shipIndexRef = mDatabase.getReference("p$otherPlayer/shipIndex")
        shipIndexRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    otherShipIndex = dataSnapshot.getValue(Int::class.java)
                    Log.d(TAG, "Other ship index = $otherShipIndex")
                    playStartupSound()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        loseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    val loser = dataSnapshot.getValue(Int::class.java)
                    Log.d(TAG, "Loser = $loser")

                    if (loser == player) {
                        // I lost!
                        loseSequence()
                    } else {
                        // I won!
                        winSequence()
                    }
                }
                else {
                    ledStrip.write( leds.map { Color.BLACK }.toIntArray() )
                    ledStrip.write( leds.map { Color.BLACK }.toIntArray() )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private fun shoot(dataSnapshot: DataSnapshot) {
        val otherShipIndex = otherShipIndex ?: return
        val shotIndex = dataSnapshot.getValue(Int::class.java)
        if (otherShipIndex == shotIndex) {
            // Hit!
            Log.i(TAG, "Hit!")
            lose()
        }
    }

    fun lose() {
        loseRef.setValue(player)
    }

    fun loseSequence() {
        Log.i(TAG, "We lost!")

        ledStrip.write( leds.map { Color.RED }.toIntArray() )
        ledStrip.write( leds.map { Color.RED }.toIntArray() )
    }

    fun winSequence() {
        Log.i(TAG, "We won!")

        ledStrip.write( leds.map { Color.GREEN }.toIntArray() )
        ledStrip.write( leds.map { Color.GREEN }.toIntArray() )
    }

    private fun rgb(R: Int,G: Int,B: Int) = (R and 0xff shl 16) or (G and 0xff shl 16) or (B and 0xff)


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

    companion object {
        private val TAG = MainActivity::class.java!!.getSimpleName()
    }
}
