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
import android.media.AudioManager
import android.content.Context
import android.os.Handler
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView

class MediaActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private var log_tag : String = "MediaPlayer"
    private lateinit var seekBarVolume: SeekBar
    private lateinit var seekBarPosition: SeekBar
    private lateinit var textViewVolume: TextView
    private lateinit var audioManager: AudioManager
    private lateinit var handler: Handler
    private lateinit var updateSeekBar: Runnable
    private lateinit var musicList: ListView
    private var musicFile: List<File> = emptyList()
    private lateinit var textViewVolumeAll: TextView
    private var currentMusicFile = 0

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            playMusic()
            //Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Please grant permission", Toast.LENGTH_LONG).show()
        }
    }
    private fun playMusic(position: Int = 0) {
        try{
            if (::mediaPlayer.isInitialized){
                if (mediaPlayer.isPlaying){
                    mediaPlayer.stop()
                }
            }
            mediaPlayer.reset()
        } catch (e: Exception){
            Log.e(log_tag, "error in PlayMusic", e)
        }
        if (musicFile.isEmpty()) {
            Log.w(log_tag, "No music files in directory")
        } else {
            val textViewTitle = findViewById<TextView>(R.id.textViewTitle)
            val firstMusicFile = musicFile[position]
            val fileName = firstMusicFile.name
            textViewTitle.text = fileName
            mediaPlayer.setDataSource(firstMusicFile.absolutePath)
            mediaPlayer.prepare()
            seekBarPosition.max = mediaPlayer.duration
            val musicLen = mediaPlayer.duration
            val minutesAll = musicLen /  60000
            val secondsAll = (musicLen / 1000) % 60
            textViewVolumeAll.text = "$minutesAll:${ if (secondsAll < 10) "0$secondsAll" else "$secondsAll"}"
            mediaPlayer.start()
            handler.postDelayed(updateSeekBar,0)
        }
    }
    private fun musicList() {
        val musicPath: String = Environment.getExternalStorageDirectory().path + "/Music"
        val directory = File(musicPath)
        musicFile = directory.listFiles()
            ?.filter { file -> file.extension == "mp3" || file.extension == "wav" || file.extension == "aac" } ?: emptyList()
        if (musicFile.isEmpty()) {
            Log.w(log_tag, "no files in directory")
            return
        }
        val musicArray = mutableListOf<String>()
        for (i in musicFile){
            musicArray.add(i.name)
        }
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, musicArray)
        musicList.adapter = adapter

    }
    override fun onPause() {
        super.onPause()
        if (mediaPlayer.isPlaying){
            Log.d(log_tag, "pausing MediaPlayer")
            mediaPlayer.pause()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized){
            Log.d(log_tag, "Releasing MediaPlayer")
            handler.removeCallbacks(updateSeekBar)
            mediaPlayer.release()
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
        handler = Handler()
        updateSeekBar = object : Runnable {
            override fun run(){
                seekBarPosition.progress = mediaPlayer.currentPosition
                handler.postDelayed(this, 100)
            }
        }

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val textViewTime = findViewById<TextView>(R.id.textViewTime)
        seekBarPosition = findViewById(R.id.seekBarPosition)
        seekBarPosition.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val minutes = progress / 60000
                val seconds = (progress / 1000) % 60
                textViewTime.text = "$minutes:${ if (seconds < 10) "0$seconds" else "$seconds"}"
                if (fromUser && mediaPlayer.isPlaying){
                    mediaPlayer.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        textViewVolume = findViewById(R.id.seekBarValue)
        textViewVolumeAll = findViewById(R.id.textViewVolumeAll)
        seekBarVolume = findViewById(R.id.seekBarVolume)

        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val initialProgress = (currentVolume * 100) / maxVolume

        seekBarVolume.max = 100
        seekBarVolume.progress = initialProgress
        textViewVolume.text = "$initialProgress"

        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val systemVolume = (progress * maxVolume) / 100
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, systemVolume, 0)
                    val volume = progress / 100.0f
                    mediaPlayer.setVolume(volume, volume)
                    textViewVolume.text = "$progress"
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val buttonPlay = findViewById<Button>(R.id.buttonPlayPause)
        buttonPlay.setOnClickListener {
            try{
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                buttonPlay.text = "▶"
            } else {
                if (mediaPlayer.currentPosition > 0) {
                    mediaPlayer.start()
                    handler.postDelayed(updateSeekBar, 0)
                    buttonPlay.text = "❚❚"
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    buttonPlay.text = "❚❚"
                }
            }
        } catch (e: IllegalStateException) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        val buttonStop = findViewById<Button>(R.id.buttonStop)
        buttonStop.setOnClickListener {
            try {
                if (::mediaPlayer.isInitialized) {
                    mediaPlayer.stop()
                    handler.removeCallbacks(updateSeekBar)
                    mediaPlayer.reset()
                    buttonPlay.text = "▶"
                    seekBarPosition.setProgress(0);
                    textViewTime.text = "0"
                }
            } catch (e: IllegalStateException) {
            }
        }
        musicList = findViewById(R.id.musicListView)
        musicList.setOnItemClickListener(AdapterView.OnItemClickListener { parent, view, position, id ->
            playMusic(position)
            buttonPlay.text = "❚❚"
        })
        musicList()

        val buttonForward = findViewById<Button>(R.id.buttonForward)
        buttonForward.setOnClickListener {
            currentMusicFile = currentMusicFile + 1
            if (currentMusicFile >= musicFile.size){
                currentMusicFile = 0
            }
            playMusic(currentMusicFile)
        }
        val buttonRewind = findViewById<Button>(R.id.buttonRewind)
        buttonRewind.setOnClickListener {
            currentMusicFile = currentMusicFile - 1
            if (currentMusicFile < 0){
                currentMusicFile = musicFile.size - 1
            }
            playMusic(currentMusicFile)
        }
    }
}