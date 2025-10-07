package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private var currentNumber = ""
    private var firstNumber = 0.0
    private var currentOperator = ""

    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        resultText = findViewById(R.id.resultText)

        val button0 = findViewById<Button>(R.id.button0)
        button0.setOnClickListener{
            currentNumber += "0"
            resultText.text = currentNumber
        }
        val button1 = findViewById<Button>(R.id.button1)
        button1.setOnClickListener{
            currentNumber += "1"
            resultText.text = currentNumber
        }
        val button2 = findViewById<Button>(R.id.button2)
        button2.setOnClickListener{
            currentNumber += "2"
            resultText.text = currentNumber
        }
        val button3 = findViewById<Button>(R.id.button3)
        button3.setOnClickListener{
            currentNumber += "3"
            resultText.text = currentNumber
        }
        val button4 = findViewById<Button>(R.id.button4)
        button4.setOnClickListener{
            currentNumber += "4"
            resultText.text = currentNumber
        }
        val button5 = findViewById<Button>(R.id.button5)
        button5.setOnClickListener{
            currentNumber += "5"
            resultText.text = currentNumber
        }
        val button6 = findViewById<Button>(R.id.button6)
        button6.setOnClickListener{
            currentNumber += "6"
            resultText.text = currentNumber
        }
        val button7 = findViewById<Button>(R.id.button7)
        button7.setOnClickListener{
            currentNumber += "7"
            resultText.text = currentNumber
        }
        val button8 = findViewById<Button>(R.id.button8)
        button8.setOnClickListener{
            currentNumber += "8"
            resultText.text = currentNumber
        }
        val button9 = findViewById<Button>(R.id.button9)
        button9.setOnClickListener{
            currentNumber += "9"
            resultText.text = currentNumber
        }
        val buttonPoint = findViewById<Button>(R.id.buttonPoint)
        buttonPoint.setOnClickListener{
            currentNumber += "."
            resultText.text = currentNumber
        }
        val buttonPlus = findViewById<Button>(R.id.buttonPlus)
        buttonPlus.setOnClickListener{
            firstNumber = currentNumber.toDouble()
            currentOperator = "+"
            currentNumber = ""
        }
        val buttonMinus = findViewById<Button>(R.id.buttonMinus)
        buttonMinus.setOnClickListener{
            firstNumber = currentNumber.toDouble()
            currentOperator = "-"
            currentNumber = ""
        }
        val buttonMultiply = findViewById<Button>(R.id.buttonMultiply)
        buttonMultiply.setOnClickListener{
            firstNumber = currentNumber.toDouble()
            currentOperator = "×"
            currentNumber = ""
        }
        val buttonDivision = findViewById<Button>(R.id.buttonDivision)
        buttonDivision.setOnClickListener{
            firstNumber = currentNumber.toDouble()
            currentOperator = "÷"
            currentNumber = ""
        }
        val buttonEquals = findViewById<Button>(R.id.buttonEquals)
        buttonEquals.setOnClickListener {
           val secondNumber = currentNumber.toDouble()
           var result = 0.0
            when(currentOperator) {
                "+" -> result = firstNumber + secondNumber
                "-" -> result = firstNumber - secondNumber
                "×" -> result = firstNumber * secondNumber
                "÷" -> result = firstNumber / secondNumber
            }
            resultText.text = result.toString()
            currentNumber = result.toString()

        }
        val buttonC = findViewById<Button>(R.id.buttonC)
        buttonC.setOnClickListener {
            currentNumber = ""
            firstNumber = 0.0
            currentOperator = ""
            resultText.text = "0"

        }
    }
}