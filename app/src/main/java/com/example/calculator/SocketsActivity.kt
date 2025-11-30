package com.example.calculator

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

class SocketsActivity : AppCompatActivity() {
    private var log_tag : String = "Sockets"
    private lateinit var tvSockets: TextView
    private lateinit var buttonStart: Button
    private lateinit var buttonStop: Button

    private lateinit var handler: Handler
    private var userChoice: Boolean = false
    private lateinit var serverThread: Thread
    private lateinit var clientThread: Thread

    fun startServer() {
        val context = ZMQ.context(1)
        val socket = ZContext().createSocket(SocketType.REP)
        socket.bind("tcp://*:2222")
        var counter: Int = 0

        while(true) {
            if (Thread.currentThread().isInterrupted) {
                break
            }
            try {
                counter++
                val requestBytes = socket.recv(0)
                val request = String(requestBytes, ZMQ.CHARSET)
                println("[SERVER] Received request: [$request]")

                handler.postDelayed({
                    tvSockets.text = "Received MSG from Client = $counter" }, 0)
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    break
                }
                val response = "Hello from Android ZMQ Server!"
                socket.send(response.toByteArray(ZMQ.CHARSET), 0)
                println("[SERVER] Sent reply: [$response]")
            } catch (e: org.zeromq.ZMQException){
                break
            }
        }
        try {
            socket.close();
            context.close();
        } catch (e: Exception){

        }
    }

    fun startClient() {
        val context = ZMQ.context(1)
        val socket = ZContext().createSocket(SocketType.REQ)
        socket.connect("tcp://localhost:2222")
        val request = "Hello from Android client!"
        for(i in 0..10){
            if (Thread.currentThread().isInterrupted) {
                break
            }
            try {
                socket.send(request.toByteArray(ZMQ.CHARSET), 0)
                Log.d(log_tag, "[CLIENT] SendT: $request")

                val reply = socket.recv(0)
                Log.d(log_tag, "[CLIENT] Received: " + String(reply, ZMQ.CHARSET))
            } catch(e: org.zeromq.ZMQException){
                break
            } catch (e: InterruptedException){
                break
            }
        }
        try {
            socket.close()
            context.close()
        } catch (e: Exception){

        }
    }
    fun startCommunication(){
        serverThread = Thread {startServer()}
        serverThread.start()

        clientThread = Thread{
            try {
                Thread.sleep(1000)
                startClient()
            } catch (e: InterruptedException){

            }
        }
        clientThread.start()
    }

    fun stopCommunication(){
        serverThread.interrupt()
        clientThread.interrupt()
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
        serverThread.interrupt()
        clientThread.interrupt()
    }
}