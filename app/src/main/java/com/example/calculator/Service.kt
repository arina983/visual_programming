package com.example.calculator

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.TrafficStats
import android.os.IBinder
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import org.zeromq.ZMQException
import java.util.Locale
import java.io.File

data class NetworkTrafficData(
    val totalRxBytes: Long,
    val totalTxBytes: Long,
    val mobileRxBytes: Long,
    val mobileTxBytes: Long,
    val wifiRxBytes: Long,
    val wifiTxBytes: Long
)

class Service : Service() {

    private val LOG_TAG = "BG_SERVICE"
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var locationManager: LocationManager
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var locationListener: LocationListener
    private var currentLocation: Location? = null
    private var zmqContext: ZContext? = null
    private var socket: ZMQ.Socket? = null
    private var counter = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        locationListener = LocationListener {
            currentLocation = it
            Log.d(LOG_TAG, "New location: $it")
        }
    }

    private fun saveToJson(data: String) {
        try {
            val file = File(filesDir, "data3.json")
            file.appendText(data + "\n")
            Log.d(LOG_TAG, "Saved to file: ${file.absolutePath} (${file.length()} bytes)")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "File write error: ${e.message}")
        }
    }
    private fun buildJson(location: Location?): String {
        return if (location != null) {
            val cellInfo = getCellInfo()
            val traffic = getNetworkTrafficInfo()

            """
        {
            "longitude": ${location.longitude},
            "latitude": ${location.latitude},
            "altitude": ${location.altitude},
            "accuracy": ${location.accuracy},
            "time": ${location.time},
            "cell": "${cellInfo.replace("\"", "'")}",
            "rx": ${traffic.totalRxBytes},
            "tx": ${traffic.totalTxBytes}
        }
        """.trimIndent()
        } else {
            """{ "error": "NO_LOCATION", "time": ${System.currentTimeMillis()} }"""
        }
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOG_TAG, "Service started")
        if (!checkPermissions()) {
            Log.e(LOG_TAG, "Missing permissions")
            sendMessageToActivity("Нет разрешений")
            stopSelf()
            return START_NOT_STICKY
        }

        serviceScope.launch {
            startLocationUpdates()
            initZMQ()

            while (isActive) {
                try {
                    //val location = currentLocation
                    /*
                    if (socket != null) {
                        socket!!.send(dataString.toByteArray(ZMQ.CHARSET), 0)
                    }
                    socket = zmqContext!!.createSocket(SocketType.DEALER)
                    if (reply != null) {
                        Log.d(LOG_TAG, "Reply[$counter]: ${String(reply, ZMQ.CHARSET)}")
                    } else {
                        Log.d(LOG_TAG, "No reply")
                    }
                    */
                    val dataString = buildJson((currentLocation))
                    saveToJson(dataString)

                    Log.d(LOG_TAG, "FILE WRITE: $dataString")

                    socket?.send(dataString.toByteArray(ZMQ.CHARSET), 0)
                    val reply = socket?.recv(0)
                    if (reply !=null) {
                        Log.d(LOG_TAG, "REPLY: ${String(reply)}")
                    } else {
                        Log.d(LOG_TAG, "NO REPLY")
                    }
                    counter++
                    sendMessageToActivity("Отправлено: $counter")

                    delay(3000)

                } catch (e: ZMQException) {
                    Log.e(LOG_TAG, "ZMQ Error (${e.errorCode}): ${e.message}")
                    initZMQ()
                    delay(3000)

                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Error: ${e.message}")
                    delay(3000)
                }
            }
        }
        return START_STICKY
    }

    private fun checkPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val phoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

        return (fineLocation || coarseLocation) && phoneState
    }

    private fun initZMQ() {
        try {
            socket?.close()
            zmqContext?.close()

            zmqContext = ZContext(1)
            socket = zmqContext!!.createSocket(SocketType.REQ)
            socket!!.setReceiveTimeOut(3000)
            socket!!.setSendTimeOut(3000)
            socket!!.connect("tcp://192.168.0.103:5551")
            Log.d(LOG_TAG, "ZMQ connected")

        } catch (e: Exception) {
            Log.e(LOG_TAG, "ZMQ init error: ${e.message}")
            socket = null
            zmqContext = null
        }
    }

    private fun startLocationUpdates() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0f, locationListener, Looper.getMainLooper())
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0f, locationListener, Looper.getMainLooper())
                Log.d(LOG_TAG, "Location updates started")
            }
        } catch (e: SecurityException) {
            Log.e(LOG_TAG, "Location permission missing")
        }
    }

    private fun getCellInfo(): String {
        return try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                val cellInfoList = telephonyManager.allCellInfo
                if (!cellInfoList.isNullOrEmpty()) {
                    val activeCell = cellInfoList.find { it.isRegistered }
                    activeCell?.toString() ?: cellInfoList[0].toString()
                } else "No cell info"
            } else {
                "No permission for cell info"
            }
        } catch (e: Exception) {
            "CellInfo error: ${e.message}"
        }
    }

    private fun getNetworkTrafficInfo(): NetworkTrafficData {
        return try {
            val totalRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0)
            val totalTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0)
            val mobileRx = TrafficStats.getMobileRxBytes().coerceAtLeast(0)
            val mobileTx = TrafficStats.getMobileTxBytes().coerceAtLeast(0)

            NetworkTrafficData(totalRx, totalTx, mobileRx, mobileTx, (totalRx - mobileRx).coerceAtLeast(0), (totalTx - mobileTx).coerceAtLeast(0))
        } catch (e: Exception) {
            NetworkTrafficData(0, 0, 0, 0, 0, 0)
        }
    }

    private fun sendMessageToActivity(msg: String) {
        val intent = Intent("BackGroundUpdate")
        intent.putExtra("Status", msg)
        intent.putExtra("Counter", counter)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()

        try {
            locationManager.removeUpdates(locationListener)
            socket?.close()
            zmqContext?.close()
        } catch (e: Exception) {}

        Log.d(LOG_TAG, "Service destroyed")
    }
}