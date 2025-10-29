package com.example.calculator

import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.media.MediaPlayer
import android.widget.TextView
import android.widget.Button
import android.Manifest
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.File

class MediaActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBarVolume: SeekBar
    private lateinit var seekBarPosition: SeekBar
    private lateinit var textViewVolume: TextView
    private var isPlaying = false

    private fun playMusic() {
        val musicPath: String = Environment.getExternalStorageDirectory().path + "/Music"
        val directory = File(musicPath)
        val musicFile = directory.listFiles()
            ?.filter { file -> file.extension == "mp3" || file.extension == "wav" || file.extension == "aac" }
        if (musicFile.isNullOrEmpty()) {
            println("no files in directory")
        } else {
            val firstMusicFile = musicFile[0]
            mediaPlayer.setDataSource(firstMusicFile.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPlaying = true
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            playMusic()
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show()
        }
    }
    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying){
            mediaPlayer.pause()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_media_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mediaPlayer = MediaPlayer()
        seekBarPosition = findViewById<SeekBar>(R.id.seekBarPosition)
       // слушатель для перемотки трека

        textViewVolume = findViewById(R.id.seekBarValue)
        seekBarVolume = findViewById<SeekBar>(R.id.seekBarVolume)
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100.0f
                mediaPlayer.setVolume(volume, volume)
                textViewVolume.text = "$progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val buttonPlay = findViewById<Button>(R.id.buttonPlayPause)
        buttonPlay.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                buttonPlay.text = "▶"
                isPlaying = false
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                buttonPlay.text = "❚❚"
            }
        }
        val buttonForward = findViewById<Button>(R.id.buttonForward)
        buttonForward.setOnClickListener {
            // логика для кнопки вперед
        }
        val buttonRewind = findViewById<Button>(R.id.buttonRewind)
        buttonRewind.setOnClickListener {
            // логика для кнопки назад
        }
    }
}