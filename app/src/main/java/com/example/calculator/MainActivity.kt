package com.example.calculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.graphics.Color

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        calculateButton.setOnClickListener {
            calculateButton.setTextColor(Color.rgb(255,0,255))
            val Calculator = Intent(this, CalculatorActivity::class.java)
            startActivity(Calculator)
        }
        val mediaPlayerButton = findViewById<Button>(R.id.mediaPlayerButton)
        mediaPlayerButton.setOnClickListener {
            mediaPlayerButton.setTextColor(Color.rgb(255,0,0))
        }
        val locationButton = findViewById<Button>(R.id.locationButton)
        locationButton.setOnClickListener {
            locationButton.setTextColor(Color.rgb(255,0,0))
        }
        val mobileButton = findViewById<Button>(R.id.mobileButton)
        mobileButton.setOnClickListener {
            mobileButton.setTextColor(Color.rgb(255,0,0))
        }
        val Button5 = findViewById<Button>(R.id.button5)
        Button5.setOnClickListener {
            Button5.setTextColor(Color.rgb(255,0,0))
        }
        val Button6 = findViewById<Button>(R.id.button6)
        Button6.setOnClickListener {
            Button6.setTextColor(Color.rgb(255,0,0))
        }
    }
}