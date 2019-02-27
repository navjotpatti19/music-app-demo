package navjot.com.musicplayerapp.viewmodel

import android.database.Cursor
import android.os.AsyncTask
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import navjot.com.musicplayerapp.models.Music

class MusicViewModel: ViewModel() {

    private class LoadMusicTask(private val musicCursor: Cursor) :
    AsyncTask<Void, Void, MutableList<Music>>() {

        private var allDeviceSongs = mutableListOf<Music>()
        init {
            execute()
        }

        override fun doInBackground(vararg params: Void?): MutableList<Music> {
            if(musicCursor.moveToFirst()) {
                val artist = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val year = musicCursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val track = musicCursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val title = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val duration = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val album = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val path = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                //Now loop through the music files
                do {
                    val audioArtist = musicCursor.getString(artist)
                    val audioYear = musicCursor.getInt(year)
                    val audioTrack = musicCursor.getInt(track)
                    val audioTitle = musicCursor.getString(title)
                    val audioDuration = musicCursor.getLong(duration)
                    val audioAlbum = musicCursor.getString(album)
                    val audioPath = musicCursor.getString(path)

                    // add the current music to the list
                    allDeviceSongs.add(
                        Music(audioArtist, audioYear, audioTrack, audioTitle, audioDuration,
                            audioAlbum, audioPath)
                    )
                } while (musicCursor.moveToNext())
                musicCursor.close()
            }
            return allDeviceSongs
        }
    }

    class MusicLiveData(private val musicCursor: Cursor):
        MutableLiveData<MutableList<Music>>() {

        init {
            loadMusic()
        }

        private fun loadMusic() {
            value = LoadMusicTask(musicCursor).get()
        }
    }

    fun getListOfSongs(musicCursor: Cursor):
            MutableLiveData<MutableList<Music>> {
        return MusicLiveData(musicCursor)
    }
}