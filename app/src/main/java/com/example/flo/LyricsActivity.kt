package com.example.flo

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

var toggle = false

class LyricsActivity : AppCompatActivity() {
    private lateinit var seekBar : SeekBar
    private lateinit var runnable: Runnable
    private var handler = Handler()
    private var music = Music()
    private var lyrics = arrayListOf<Pair<Int, String>>()
    private var focus = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lyrics)
        window.statusBarColor = Color.parseColor("#262626")

        music = intent.getSerializableExtra("music") as Music
        lyrics = intent.getSerializableExtra("lyrics") as ArrayList<Pair<Int, String>>

        val clearBtn = findViewById<ImageButton>(R.id.clearBtn)
        val playBtnLyrics = findViewById<ImageButton>(R.id.play_btn_lyrics)
        val focusBtn = findViewById<ImageButton>(R.id.focus_btn_lyrics)
        val toggleBtn = findViewById<ImageButton>(R.id.toggle_btn_lyrics)
        val titleText = findViewById<TextView>(R.id.title_text_lyrics)
        val singerText = findViewById<TextView>(R.id.singer_text_lyrics)
        val lyricsText = findViewById<RecyclerView>(R.id.lyrics_text)
        val startTime = findViewById<TextView>(R.id.present_progress_lyrics)
        val maxTime = findViewById<TextView>(R.id.max_progress_lyrics)
        val lyricsAdapter = LyricsAdapter(this, lyrics)
        lyricsText.adapter = lyricsAdapter

        val lm = LinearLayoutManager(this)
        lyricsText.layoutManager = lm
        lyricsText.setHasFixedSize(true)

        seekBar = findViewById(R.id.seekbar_lyrics)
        seekBar.progress = 0
        seekBar.max = mediaPlayer.duration

        if(mediaPlayer.isPlaying) {
            playBtn.setImageResource(R.drawable.ic_baseline_pause_24)
        }else {
            playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }

        lyricsText.setOnTouchListener { _, _ ->
            focus = true
            focusBtn.setColorFilter(Color.parseColor("#FFFFFFFF"))
            return@setOnTouchListener false
        }

        clearBtn.setOnClickListener {
            finish()
        }

        toggleBtn.setOnClickListener {
            if(toggle) {
                toggle = false
                toggleBtn.setColorFilter(Color.parseColor("#FFA0A0A0"))
            } else {
                toggle = true
                toggleBtn.setColorFilter(Color.parseColor("#FFFFFFFF"))
            }
        }

        focusBtn.setOnClickListener {
            if(focus) {
                focus = false
                focusBtn.setColorFilter(Color.parseColor("#FFA0A0A0"))
            }
        }

        playBtnLyrics.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                playBtnLyrics.setImageResource(R.drawable.ic_baseline_pause_24)
                playBtn.setImageResource(R.drawable.ic_baseline_pause_24)
            } else {
                mediaPlayer.pause()
                playBtnLyrics.setImageResource(R.drawable.ic_baseline_play_arrow_24)
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
                            if(!focus){
                                val smoothScroller = CenterSmoothScroller(lyricsText.context)
                                smoothScroller.targetPosition = i
                                lm.startSmoothScroll(smoothScroller)
                            }

                            lyricsAdapter.setSelected(i)
                        }
                        else -> {
                            if(!focus){
                                val smoothScroller = CenterSmoothScroller(lyricsText.context)
                                smoothScroller.targetPosition = i - 1
                                lm.startSmoothScroll(smoothScroller)
                            }
                            lyricsAdapter.setSelected(i - 1)
                        }
                    }
                    break
                }else if(i == lyrics.size - 1){
                    if(!focus){
                        val smoothScroller = CenterSmoothScroller(lyricsText.context)
                        smoothScroller.targetPosition = i
                        lm.startSmoothScroll(smoothScroller)
                    }

                    lyricsAdapter.setSelected(i)
                }
            }

            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed(runnable, 500)
        }
        handler.postDelayed(runnable, 1000)

        titleText.text = music.title
        singerText.text = music.singer
        maxTime.text = "${mediaPlayer.duration/60000}:${mediaPlayer.duration/1000%60}"
    }
}

class CenterSmoothScroller(context: Context) : LinearSmoothScroller(context) {
    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int {
        return boxStart + (boxEnd - boxStart) / 2 - (viewStart + (viewEnd - viewStart) / 2)
    }
}