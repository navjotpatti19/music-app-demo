package navjot.com.musicplayerapp.helper_classes

import android.content.ContentResolver
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.text.Html
import android.text.Spanned
import java.util.*
import java.util.concurrent.TimeUnit

object MusicUtils {

    fun formatSongTrack(trackNumber: Int): Int {
        var formatted = trackNumber
        if(trackNumber >= 1000) {
            formatted = trackNumber % 1000
        }
        return formatted
    }

    fun formatSongDuration(duration: Long): String {
        return String.format(
            Locale.getDefault(), "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(duration),
            TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                TimeUnit.MILLISECONDS.toMinutes(duration)
            )
        )
    }

    fun buildSpanned(res: String): Spanned {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(res, Html.FROM_HTML_MODE_LEGACY)
        else
            Html.fromHtml(res)
    }

    fun getMusicCursor(contentResolver: ContentResolver): Cursor? {
        return contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,null, null, null
        )
    }
}