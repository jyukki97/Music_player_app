package com.example.flo

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.Image
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.net.URL

lateinit var mediaPlayer : MediaPlayer
lateinit var playBtn : ImageButton

open class MainActivity : AppCompatActivity() {
    private lateinit var seekBar : SeekBar
    private lateinit var runnable: Runnable
    private var handler = Handler()
    private val JSON_URL = "https://grepp-programmers-challenges.s3.ap-northeast-2.amazonaws.com/2020-flo/song.json"
    private var music = Music()
    private var lyrics = arrayListOf<Pair<Int, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = Color.parseColor("#262626")

        val linearLayout = findViewById<LinearLayout>(R.id.touch_layout)
        playBtn = findViewById(R.id.play_btn)
        val titleText = findViewById<TextView>(R.id.title_text)
        val singerText = findViewById<TextView>(R.id.singer_text)
        val startTime = findViewById<TextView>(R.id.present_progress)
        val maxTime = findViewById<TextView>(R.id.max_progress)
        val firstLyrics = findViewById<TextView>(R.id.first_lyrics)
        val secondLyrics = findViewById<TextView>(R.id.second_lyrics)

        mediaPlayer = MediaPlayer()
        seekBar = findViewById(R.id.seekbar)
        seekBar.progress = 0

        linearLayout.setOnClickListener{
            val intent = Intent(this, LyricsActivity::class.java).apply {
                putExtra("music", music)
                putExtra("lyrics", lyrics)
            }
            startActivity(intent)
        }

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
            startTime.text = "${String.format("%02d", mediaPlayer.currentPosition/60000)}:${String.format("%02d", mediaPlayer.currentPosition/1000%60)}"

            for (i in 0 until lyrics.size){
                if(lyrics[i].first >= mediaPlayer.currentPosition){
                    when (i) {
                        0 -> {
                            firstLyrics.text = lyrics[i].second
                            secondLyrics.text = lyrics[i + 1].second
                        }
                        else -> {
                            firstLyrics.text = lyrics[i - 1].second
                            secondLyrics.text = lyrics[i].second
                        }
                    }
                    break
                }else if(i == lyrics.size - 1){
                    firstLyrics.text = lyrics[i].second
                    secondLyrics.text = ""
                }
            }

            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
        mediaPlayer.setOnCompletionListener {
            playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            seekBar.progress = 0
        }

        CoroutineScope(Dispatchers.Main).launch {
            music =
                withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                    getMusic(getJsonFromURL(JSON_URL))
                }
            DownloadImageFromInternet(findViewById(R.id.thumbnail)).execute(music.image)

            prepareMediaPlayer(music.file)

            titleText.text = music.title
            singerText.text = music.singer
            maxTime.text = "${mediaPlayer.duration/60000}:${mediaPlayer.duration/1000%60}"

            titleText.isSelected = true
            val lyricsList = music.lyrics.split("[", "]")
            for(i in 1 until lyricsList.size step 2) {
                val time = lyricsList[i].split(":")
                var totalTime = (time[0].toInt() * 60 + time[1].toInt()) * 1000 + time[2].toInt()
                lyrics.add(Pair(totalTime, lyricsList[i + 1]))
            }
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
            val singer = jsonObject.getString("singer")
            val album = jsonObject.getString("album")
            val title = jsonObject.getString("title")
            val duration = jsonObject.getInt("duration")
            val image = jsonObject.getString("image")
            val file = jsonObject.getString("file")
            val lyrics = jsonObject.getString("lyrics")
            return Music(singer, album, title, duration, image, file, lyrics)
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
                val `in` = URL(imageURL).openStream()
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
    val title:String = "",
    val duration:Int = 0,
    val image:String = "",
    val file:String = "",
    val lyrics:String = "",
) : Serializable