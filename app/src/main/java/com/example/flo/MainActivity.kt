package com.example.flo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.net.URL

open class MainActivity : AppCompatActivity() {
    private lateinit var mediaPlayer : MediaPlayer
    private lateinit var seekBar : SeekBar
    lateinit var runnable: Runnable
    private var handler = Handler()
    private val JSON_URL = "https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com/2020-flo/song.json"
    private var music = Music()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playBtn = findViewById<ImageButton>(R.id.play_btn)

        mediaPlayer = MediaPlayer()
        seekBar = findViewById(R.id.seekbar)
        seekBar.progress = 0

        playBtn.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                playBtn.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                mediaPlayer.pause()
                playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, pos: Int, changed: Boolean) {
                if (changed) {
                    mediaPlayer.seekTo(pos)
                }
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        runnable = Runnable {
            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
        mediaPlayer.setOnCompletionListener {
            playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            seekBar.progress = 0
        }

        CoroutineScope(Dispatchers.Main).launch {
            music = CoroutineScope(Dispatchers.IO).async {
                getMusic(getJsonFromURL(JSON_URL))
            }.await()
            DownloadImageFromInternet(findViewById(R.id.thumbnail)).execute(music.image)

            prepareMediaPlayer(music.file)

            seekBar.max = mediaPlayer.duration
        }
    }
    private fun prepareMediaPlayer(url : String) {
        try {
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepare()
        }catch (e : Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getJsonFromURL(wantedURL: String) : String {
        return URL(wantedURL).readText()
    }

    private fun getMusic(s: String): Music {
        try{
            val jsonObject = JSONObject(s)
            val singer = jsonObject.getString("album")
            val album = jsonObject.getString("album")
            val duration = jsonObject.getInt("duration")
            val image = jsonObject.getString("image")
            val file = jsonObject.getString("file")
            val lyrics = jsonObject.getString("lyrics")
            return Music(singer, album, duration, image, file, lyrics)
        }catch (e : JSONException) {
            e.printStackTrace()
        }
        return Music()
    }
    private inner class DownloadImageFromInternet(var imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
        override fun doInBackground(vararg urls: String): Bitmap? {
            val imageURL = urls[0]
            var image: Bitmap? = null
            try {
                val `in` = java.net.URL(imageURL).openStream()
                image = BitmapFactory.decodeStream(`in`)
            }
            catch (e: Exception) {
                e.printStackTrace()
            }
            return image
        }
        override fun onPostExecute(result: Bitmap?) {
            imageView.setImageBitmap(result)
        }
    }
}

data class Music(
    val singer:String = "",
    val album:String = "",
    val duration:Int = 0,
    val image:String = "",
    val file:String = "",
    val lyrics:String = "",
)