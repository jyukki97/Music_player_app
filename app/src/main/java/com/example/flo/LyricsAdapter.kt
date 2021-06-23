package com.example.flo

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LyricsAdapter(private val context: Context, private val lyrics: ArrayList<Pair<Int, String>>) : RecyclerView.Adapter<LyricsAdapter.ViewHolder>() {
    private var selected = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.item_text)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.lyrics_item, viewGroup, false)

        return ViewHolder(view)
    }

    fun setSelected(position: Int) {
        selected = position
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.textView.text = lyrics[position].second

        viewHolder.textView.setOnFocusChangeListener { v, hasFocus ->
            println("포커스온")
        }

        viewHolder.textView.setOnClickListener {
            if(toggle){
                selected = position
                mediaPlayer.seekTo(lyrics[position].first)
                notifyDataSetChanged()
            }
        }

        if (position == selected) {
            viewHolder.textView.setTextColor(Color.parseColor("#FFFFFFFF"))
        } else {
            viewHolder.textView.setTextColor(Color.parseColor("#FFA0A0A0"))
        }

        var layoutParams = viewHolder.itemView.layoutParams
        layoutParams.height = 80
        viewHolder.itemView.requestLayout()
    }

    override fun getItemCount() = lyrics.size
}