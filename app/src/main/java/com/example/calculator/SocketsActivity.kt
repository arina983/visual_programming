package com.example.calculator

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import com.google.gson.Gson
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.collections.forEachIndexed
import android.location.LocationListener

data class forJsonString (
    val longitude: Double,
    val latitude: Double,
    val altitude: Double,
    val time: String
)

class SocketsActivity : AppCompatActivity() {
    private var log_tag : String = "Sockets"
    private lateinit var tvSockets: TextView
    private lateinit var buttonStart: Button
    private lateinit var buttonStop: Button

    private lateinit var handler: Handler
    private var userChoice: Boolean = false
    private lateinit var clientThread: Thread
    private lateinit var myLocationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var currentLocation: Location? = null
    private val gson = Gson()
    private lateinit var telephonyManager: TelephonyManager
    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        private const val PERMISSION_REQUEST_CELL_INFO = 1
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

    private fun checkPermissionsAndGetCellInfo() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(log_tag, "Нет разрешений READ_PHONE_STATE или ACCESS_COARSE_LOCATION для получения cell info")

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_PHONE_STATE,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CELL_INFO
            )
        } else {
            getCellInfo()
        }
    }

    private fun getCellInfo(): String {
        return try {
            val cellInfoList = telephonyManager.allCellInfo
            if (cellInfoList != null && cellInfoList.isNotEmpty()) {
                val activeCell = cellInfoList.find { it.isRegistered }
                if (activeCell != null) {
                    activeCell.toString()
                } else {
                    cellInfoList[0].toString()
                }
            } else {
                "No cell info available"
            }
        } catch (e: SecurityException) {
            "SecurityException: ${e.message}"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    fun startClient() {

        var zmqContext = ZContext(1)
        var socket = zmqContext.createSocket(SocketType.REQ)
        socket.connect("tcp://172.20.10.2:5556")
        var counter: Int = 0

        while (!Thread.currentThread().isInterrupted) {
            try {
                if (currentLocation != null) {
//                    val objectJson = forJsonString(currentLocation!!.longitude, currentLocation!!.latitude, currentLocation!!.altitude, currentLocation!!.time.toString()
//                    )
//                    val jsonToString = gson.toJson(objectJson)
                    val cellInfo = getCellInfo()
                    val dataString = String.format("%.6f,%.6f,%.2f,%d | %s", currentLocation!!.longitude, currentLocation!!.latitude, currentLocation!!.altitude, currentLocation!!.time, cellInfo)
                    socket.send(dataString.toByteArray(ZMQ.CHARSET), 0)
                    counter++

                    handler.post {
                        tvSockets.text = "Отправлено: $counter сообщений"
                    }

                    Log.d(log_tag, "[CLIENT] Send: $dataString")

                    val reply = socket.recv(0)
                    Log.d(log_tag, "[CLIENT] Received: " + String(reply, ZMQ.CHARSET))

                    Thread.sleep(2000)
                } else {
                    Log.w(log_tag, "Location is null, skipping...")
                    Thread.sleep(2000)
                }

            } catch(e: org.zeromq.ZMQException) {
                Log.e(log_tag, "ZMQ Error: ${e.message}")
                try{
                    socket.close()
                    zmqContext.close()
                } catch (_: Exception){}
                Thread.sleep(1000)
                zmqContext = ZContext(1)
                socket = zmqContext.createSocket(SocketType.REQ)
                socket.connect("tcp://172.20.10.2:5556")
                continue
            } catch (e: InterruptedException) {
                Log.d(log_tag, "Thread interrupted")
                break
            } catch (e: Exception) {
                Log.e(log_tag, "General error: ${e.message}")
                break
            }
        }

        try {
            socket.close()
            zmqContext.close()
            Log.d(log_tag, "ZMQ resources closed")
        } catch (e: Exception) {
            Log.e(log_tag, "Error closing resources: ${e.message}")
        }
    }

    fun startCommunication() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            checkPermissionsAndGetCellInfo()
            return
        }
        startLocationUpdates()
        clientThread = Thread {
            try {
                Thread.sleep(1000)
                startClient()
            } catch (e: InterruptedException) {
                Log.d(log_tag, "Start communication interrupted")
            }
        }
        clientThread.start()
    }

    fun stopCommunication() {
        stopLocationUpdates()
        if (::clientThread.isInitialized) {
            clientThread.interrupt()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sockets)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvSockets = findViewById(R.id.tvSockets)
        handler = Handler(Looper.getMainLooper())
        myLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        locationListener = LocationListener { location ->
            currentLocation = location
            Log.d(log_tag, "Получено новое местоположение: $location")
        }

        buttonStart = findViewById(R.id.buttonStartData)
        buttonStart.setOnClickListener {
            userChoice = true
            startCommunication()
        }

        buttonStop = findViewById(R.id.buttonStopData)
        buttonStop.setOnClickListener {
            userChoice = false
            stopCommunication()
        }
    }

    private fun startLocationUpdates() {
        if (!checkPermissions()) {
            requestPermissions()
            return
        }

        if (!isLocationEnabled()) {
            handler.post {
                Toast.makeText(applicationContext, "Включите геолокацию", Toast.LENGTH_SHORT).show()
            }
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        try {
            myLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2000,
                0f,
                locationListener,
                Looper.getMainLooper()
            )

            myLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                2000,
                0f,
                locationListener,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(log_tag, "Security exception: ${e.message}")
        }
    }

    private fun stopLocationUpdates() {
        myLocationManager.removeUpdates(locationListener)
    }

    override fun onPause() {
        super.onPause()
        stopCommunication()
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == PERMISSION_REQUEST_CELL_INFO) {
                if (grantResults.isNotEmpty() &&
                    grantResults.size == 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(log_tag, "Разрешения получены")
                    if (userChoice) {
                        startCommunication()
                    }
                } else {
                    Log.d(log_tag, "Разрешения не получены")
                }
            }
        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
                if (userChoice) {
                    startCommunication()
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}