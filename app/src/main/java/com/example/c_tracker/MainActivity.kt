package com.example.c_tracker

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.http.HttpResponseCache
import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.c_tracker.database.AppDatabase
import com.example.c_tracker.database.ReachedCity
import com.example.c_tracker.database.ReachedPrefecture
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pnikosis.materialishprogress.ProgressWheel
import kotlinx.coroutines.runBlocking
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : WearableActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var db: AppDatabase
    private var processing: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableHttpResponseCache()
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
        db = AppDatabase.getInstance(applicationContext)
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

        this.resetView()

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
//        this.track(35.693693693693698, 139.6586069914854)
//        this.processing = false
        fusedLocationClient.lastLocation.addOnSuccessListener {
            location : Location? ->
                if (location != null) {
                    this.track(location)
                }
                this.processing = false
        }
    }

    fun track(location: Location) {
//    fun track(latitude: Double, longitude: Double) {
        runBlocking {
            try {
                // FIND ADDRESS CODE FROM LOCATION
                val addressRes = Fuel.get(
                    "https://mreversegeocoder.gsi.go.jp/reverse-geocoder/LonLatToAddress",
                    listOf("lat" to location.latitude, "lon" to location.longitude)
//                    listOf("lat" to latitude, "lon" to longitude)
                ).awaitObject(AddressDeserializer)
                val citycode = addressRes.results.muniCd
                val prefcode = citycode.substring(0, 2)
                val prefname = PREF_CODE_NAME[prefcode]

                // FIND ADDRESS NAME FROM CODE
                val addressListRes = Fuel.get(
                    "https://www.land.mlit.go.jp/webland/api/CitySearch",
                    listOf("area" to prefcode)
                ).awaitObject(AddressListDeserializer)
                val cityInfo = addressListRes.data.find { c -> c.id == citycode }

                if (cityInfo != null && prefname != null) {
                    findViewById<ProgressWheel>(R.id.loading).visibility = View.INVISIBLE
                    findViewById<TextView>(R.id.city).text = cityInfo.name
                    findViewById<TextView>(R.id.pref).text = prefname
                    findViewById<ImageButton>(R.id.reload).visibility = View.VISIBLE
                    println("EXIT: COMPLETED RENDERING")

                    saveTrack(
                        ReachedPrefecture(
                            code = prefcode,
                            name = prefname
                        ),
                        ReachedCity(
                            code = citycode,
                            prefectureCode = prefcode,
                            name = cityInfo.name,
                            firstReachedAt = getCurrentDateString()
                        )
                    )
                } else {
                    println("EXIT: COULDN'T FIND PROPER ADDRESS")
                }
            } catch(exception: Exception) {
                println("A network request exception was thrown: ${exception.message}")
            }
        }
    }

    fun resetView() {
        findViewById<ProgressWheel>(R.id.loading).visibility = View.VISIBLE
        findViewById<TextView>(R.id.city).text = ""
        findViewById<TextView>(R.id.pref).text = ""
        findViewById<ImageButton>(R.id.reload).visibility = View.INVISIBLE
    }

    private fun enableHttpResponseCache() {
        try {
            val httpCacheDir = File(this.getCacheDir(), "http")
            val httpCacheSize = 10 * 1024 * 1024.toLong() // 10 MiB
            HttpResponseCache.install(httpCacheDir, httpCacheSize)
        } catch (e: Exception) {
            Log.i("TAG", "HTTP response cache installation failed:$e")
        }
    }

    suspend fun saveTrack(prefecture: ReachedPrefecture, city: ReachedCity) {
        db.reachedPrefectureDao().insertAll(prefecture)
        db.reachedCityDao().insertAll(city)
    }

    fun getCurrentDateString(): String {
        return LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        )
    }
}

