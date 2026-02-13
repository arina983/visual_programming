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
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import java.io.File

data class forJsonFile (
    val longitude: Double,
    val latitude: Double,
    val altitude: Double,
    val time: String
)

class LocationActivity : AppCompatActivity(), android.location.LocationListener {
    private val log_tag: String = "LOCATION_ACTIVITY"
    private lateinit var tvLon: TextView
    private lateinit var tvLat: TextView
    private lateinit var tvAlt: TextView
    private lateinit var currentTime: TextView
    private lateinit var myLocationManager: LocationManager
    private lateinit var myFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentPosition: Button
    private lateinit var updatePosition: Button
    private val gson = Gson()
    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        private const val PERMISSION_REQUEST_STORAGE = 200
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
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
    private fun checkPermissionStorage(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermissionStorage(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_STORAGE)
    }
    private fun getLocation(userChoice: Boolean){

        if(checkPermissions()){
            if(isLocationEnabled()){
                if (userChoice) {
                    myFusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                        val location: Location? = task.result
                        if (location == null) {
                            Toast.makeText(applicationContext, "problems with signal",Toast.LENGTH_SHORT).show()
                        } else {
                            onLocationChanged(location)
                        }
                    }
                } else {
                    myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 1f, this)
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
                getLocation(false)
            } else {
                Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show()
            }
        }else if (requestCode == PERMISSION_REQUEST_STORAGE){
            if (grantResult.isNotEmpty() && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение дано", Toast.LENGTH_LONG).show()
             } else {
                Toast.makeText(this, "Please grant permission, не удается сохранить файл", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun writeFile(jsonString: String){
        if (!checkPermissionStorage()){
            requestPermissionStorage()
            return
        }
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val file = File(downloadsDir,"DATAAA.json")
        file.appendText("$jsonString\n")

        Log.d("FILE_PATH", "Файл сохранен здесь: ${file.absolutePath}")
        Toast.makeText(this, "Данные сохранены сюда: ${file.absolutePath}", Toast.LENGTH_LONG).show()
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
            myLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            tvAlt = findViewById<TextView>(R.id.Altitude)
            currentTime = findViewById<TextView>(R.id.textTime)
            updatePosition = findViewById<Button>(R.id.buttonUpdate)
    }
    override fun onResume() {
        super.onResume()
         currentPosition.setOnClickListener {
             getLocation(true)
         }
         updatePosition.setOnClickListener {
             getLocation(false)
         }
    }
    override fun onPause(){
        super.onPause()
        myLocationManager.removeUpdates(this)
    }

    override fun onLocationChanged(location: Location) {
        val altitude = location.altitude
        val currTime = location.time
        val timeString = android.text.format.DateFormat.format("dd.MM.yyyy HH:mm:ss", currTime)
        tvAlt.setText(altitude.toString())
        tvLat.setText(location.latitude.toString())
        currentTime.setText(timeString)
        tvLon.setText(location.longitude.toString())
        val objectJson = forJsonFile(location.longitude, location.latitude, location.altitude, currTime.toString())
        val jsonToString = gson.toJson(objectJson)
        writeFile(jsonToString)
    }
}