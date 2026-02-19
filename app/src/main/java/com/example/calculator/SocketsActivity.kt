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
import android.widget.Toast
import androidx.core.app.ActivityCompat

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
    private var currentLocation: Location? = null
    private val gson = Gson()

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
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

    fun startClient() {
        if(checkPermissions()) {
            if (isLocationEnabled()) {
                currentLocation = myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: myLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (currentLocation == null) {
                    handler.post {
                        Toast.makeText(
                            applicationContext,
                            "Location not available. Try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                        return
                }
            } else {
                handler.post {
                    Toast.makeText(
                        applicationContext,
                        "Enable location in settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
                return
            }
        } else {
            Log.w(log_tag, "location permission is not allowed")
            requestPermissions()
            return
        }

        var zmqContext = ZContext(1)
        var socket = zmqContext.createSocket(SocketType.REQ)
        socket.connect("tcp://172.20.10.2:5555")
        var counter: Int = 0

        while (!Thread.currentThread().isInterrupted) {
            try {
                currentLocation = myLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: myLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                if (currentLocation != null) {
//                    val objectJson = forJsonString(currentLocation!!.longitude, currentLocation!!.latitude, currentLocation!!.altitude, currentLocation!!.time.toString()
//                    )
//                    val jsonToString = gson.toJson(objectJson)
                    val dataString = String.format("%.6f,%.6f,%.2f,%d", currentLocation!!.longitude, currentLocation!!.latitude, currentLocation!!.altitude, currentLocation!!.time)
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
                socket.connect("tcp://172.20.10.2:5555")
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

    override fun onPause() {
        super.onPause()
        stopCommunication()
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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