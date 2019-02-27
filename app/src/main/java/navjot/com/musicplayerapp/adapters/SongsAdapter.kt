package navjot.com.musicplayerapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.song_item.view.*
import navjot.com.musicplayerapp.R
import navjot.com.musicplayerapp.helper_classes.MusicUtils
import navjot.com.musicplayerapp.helper_classes.UIHelperClass
import navjot.com.musicplayerapp.models.Music

class SongsAdapter(music: MutableList<Music>) : RecyclerView.Adapter<SongsAdapter.SongHolder>() {

    var onSongClick: ((Music) -> Unit)? = null

    private var mMusic = music

    init {
        mMusic.sortBy { it.track }
    }

    fun swapSongs(allSongs: MutableList<Music>) {
        mMusic = allSongs
         mMusic.sortBy { it.track }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): SongHolder {
        return SongHolder(LayoutInflater.from(parent.context).inflate(R.layout.song_item, parent,false))
    }

    override fun getItemCount(): Int {
        return mMusic.size
    }

    override fun onBindViewHolder(holder: SongHolder, p1: Int) {
        val track = mMusic[holder.adapterPosition].track
        val title = mMusic[holder.adapterPosition].title
        val duration = mMusic[holder.adapterPosition].duration

        holder.bindItems(track, title, duration)
    }

    inner class SongHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bindItems(track: Int, title: String, duration: Long) {
            itemView.track.text = MusicUtils.formatSongTrack(track).toString()
            itemView.title.text = title
            itemView.duration.text = MusicUtils.formatSongDuration(duration)
            itemView.setOnClickListener { onSongClick?.invoke(mMusic[adapterPosition]) }
            UIHelperClass.setHorizontalScrollBehaviour(itemView, itemView.title)
        }
    }
}