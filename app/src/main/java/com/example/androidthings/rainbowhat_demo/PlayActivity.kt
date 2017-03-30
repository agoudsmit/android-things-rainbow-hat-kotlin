package com.example.androidthings.rainbowhat_demo

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.firebase.database.*

class PlayActivity : Activity() {


    private val mDatabase by lazy { FirebaseDatabase.getInstance() }
    var fbMode : DatabaseReference? = null

    var otherShipIndex: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        initFirebase()
    }

    fun initFirebase() {
        // Wij zijn p1

        val shots = mDatabase.getReference("p2/shots")
        shots.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
            }

            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {
                val otherShipIndex = otherShipIndex ?: return
                val shotIndex = dataSnapshot.getValue(Int::class.java)
                if (otherShipIndex == shotIndex) {
                    // Hit!
                    Log.i(TAG, "Hit!")
                }
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
            }
        })


        val shipIndexRef = mDatabase.getReference("p2/shipIndex")
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

    companion object {
        private val TAG = MainActivity::class.java!!.getSimpleName()
    }
}
