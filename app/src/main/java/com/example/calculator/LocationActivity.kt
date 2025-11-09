package com.example.calculator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationActivity : AppCompatActivity() {
    private val log_tag: String = "LOCATION_ACTIVITY"
    private lateinit var tvLon: TextView
    private lateinit var tvLat: TextView
    private lateinit var tvAlt: TextView
    private lateinit var currentTime: TextView
    private lateinit var myFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentPosition: Button
    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }
    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun checkPermissions(): Boolean{
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    private fun getCurrentLocation(){

        if(checkPermissions()){
            if(isLocationEnabled()){
                myFusedLocationProviderClient.lastLocation.addOnCompleteListener(this){ task->
                    val location: Location?=task.result
                    if(location == null){
                        Toast.makeText(applicationContext, "problems with signal", Toast.LENGTH_SHORT).show()
                    } else {
                        val altitude = location.altitude
                        val currTime = location.time
                        val timeString = android.text.format.DateFormat.format("dd.MM.yyyy HH:mm:ss", currTime)
                        tvAlt.setText(altitude.toString())
                        tvLat.setText(location.latitude.toString())
                        tvLon.setText(location.longitude.toString())
                        currentTime.setText(timeString)
                    }
                }

            } else{
                Toast.makeText(applicationContext, "Enable location in settings", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            Log.w(log_tag, "location permission is not allowed");
            tvLat.setText("Permission is not granted")
            tvLon.setText("Permission is not granted")
            requestPermissions()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResult: IntArray){
        super.onRequestPermissionsResult(requestCode, permissions, grantResult)
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResult.isNotEmpty() && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        currentPosition = findViewById<Button>(R.id.currentPositionButton)
        tvLon = findViewById<TextView>(R.id.textViewLongitude)
        tvLat = findViewById<TextView>(R.id.textViewLatitude)
        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        tvAlt = findViewById<TextView>(R.id.Altitude)
        currentTime = findViewById<TextView>(R.id.textTime)
    }
    override fun onResume() {
        super.onResume()
         currentPosition.setOnClickListener {
             getCurrentLocation()
         }
    }
}