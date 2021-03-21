package com.example.c_tracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pnikosis.materialishprogress.ProgressWheel
import kotlinx.coroutines.runBlocking


class MainActivity : WearableActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var processing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        findViewById<ImageButton>(R.id.reload).setOnClickListener { v -> this.run() }
        this.run()
    }

    override fun onResume() {
        super.onResume()
        this.run()
    }

    private fun run() {
        if (this.processing) {
            println("PROCESSING, EXIT")
            return
        }

        this.initialize()

        if (
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("EXIT: NO PERMISSION")
            finish()
        }

        println("START: FETCH LOCATION")
        this.processing = true
        fusedLocationClient.lastLocation.addOnSuccessListener {
            location : Location? ->
                if (location != null) {
                    this.render(location)
                }
                this.processing = false
        }
    }

    fun render(location: Location) {
        runBlocking {
            try {
                // FIND ADDRESS CODE FROM LOCATION
                val addressRes = Fuel.get(
                    "https://mreversegeocoder.gsi.go.jp/reverse-geocoder/LonLatToAddress",
                    listOf("lat" to location.latitude, "lon" to location.longitude)
                ).awaitObject(AddressDeserializer)
                val citycode = addressRes.results.muniCd
                val prefcode = citycode.substring(0, 2)

                // FIND ADDRESS NAME FROM CODE
                val addressListRes = Fuel.get(
                    "https://www.land.mlit.go.jp/webland/api/CitySearch",
                    listOf("area" to prefcode)
                ).awaitObject(AddressListDeserializer)
                val cityInfo = addressListRes.data.find { c -> c.id == citycode }

                if (cityInfo != null) {
                    findViewById<ProgressWheel>(R.id.loading).visibility = View.INVISIBLE
                    findViewById<TextView>(R.id.city).text = cityInfo.name
                    findViewById<TextView>(R.id.pref).text = PREF_CODE_NAME[prefcode]
                    findViewById<ImageButton>(R.id.reload).visibility = View.VISIBLE
                    println("EXIT: COMPLETED RENDERING")
                } else {
                    println("EXIT: COULDN'T FIND PROPER ADDRESS")
                }
            } catch(exception: Exception) {
                println("A network request exception was thrown: ${exception.message}")
            }
        }
    }

    fun initialize() {
        findViewById<ProgressWheel>(R.id.loading).visibility = View.VISIBLE
        findViewById<TextView>(R.id.city).text = ""
        findViewById<TextView>(R.id.pref).text = ""
        findViewById<ImageButton>(R.id.reload).visibility = View.INVISIBLE
    }
}

