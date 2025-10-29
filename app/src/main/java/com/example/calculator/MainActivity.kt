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

    private fun setupButton(buttonId: Int, color: Int) {
        val button = findViewById<Button>(buttonId)
        button.setOnClickListener {
            button.setTextColor(color)
        }
    }
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
            calculateButton.setTextColor(Color.rgb(255, 0, 255))
            val calculator = Intent(this, CalculatorActivity::class.java)
            startActivity(calculator)
        }
        val mediaPlayerButton = findViewById<Button>(R.id.mediaPlayerButton)
        mediaPlayerButton.setOnClickListener {
            mediaPlayerButton.setTextColor(Color.rgb(255, 0 , 255))
            val mediaPlayer = Intent(this, MediaActivity::class.java)
            startActivity(mediaPlayer)
        }
            setupButton(R.id.locationButton, Color.RED)
            setupButton(R.id.mobileButton, Color.RED)
            setupButton(R.id.button5, Color.RED)
            setupButton(R.id.button6, Color.RED)
    }
}