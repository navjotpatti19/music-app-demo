package navjot.com.musicplayerapp.helper_classes

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import navjot.com.musicplayerapp.MainActivity
import navjot.com.musicplayerapp.R
import navjot.com.musicplayerapp.services.PlayerService

//Notification params
private const val CHANNEL_ID = "CHANNEL_ID"
private const val REQUEST_CODE = 100
const val NOTIFICATION_ID = 101

//notifications actions
const val PLAY_PAUSE_ACTION = "PLAY_PAUSE"
const val NEXT_ACTION = "NEXT"
const val PREV_ACTION = "PREV"

class MusicNotificationManager(private val playerService: PlayerService) {

    //accent
    var accent: Int = R.color.blue

    val notificationManager: NotificationManager =
            playerService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    var notificationBuilder: NotificationCompat.Builder? = null

    fun createNotification(): Notification {
        val song = playerService.mediaPlayerHolder!!.currentSong

        notificationBuilder = NotificationCompat.Builder(playerService, CHANNEL_ID)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val openPlayerIntent = Intent(playerService, MainActivity::class.java)
        openPlayerIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val contentIntent = PendingIntent.getActivity(playerService, REQUEST_CODE,
            openPlayerIntent, 0)

        val artist = song!!.artist
        val songTitle = song.title

        val spanned = MusicUtils.buildSpanned(playerService.getString(R.string.playing_song, artist, songTitle))

        notificationBuilder!!.setShowWhen(false)
            .setSmallIcon(R.drawable.music_notification)
            .setColor(accent)
            .setContentTitle(spanned)
            .setContentText(song.album)
            .setContentIntent(contentIntent)
            .addAction(notificationAction(PREV_ACTION))
            .addAction(notificationAction(PLAY_PAUSE_ACTION))
            .addAction(notificationAction(NEXT_ACTION))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        notificationBuilder!!.setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
        return notificationBuilder!!.build()
    }

    private fun notificationAction(action: String): NotificationCompat.Action {
        var icon: Int =
                if(playerService.mediaPlayerHolder!!.state != PAUSED)
                    R.drawable.pause_notification
                else
                    R.drawable.ic_play_notification

        when (action) {
            PREV_ACTION -> icon = R.drawable.ic_skip_previous_notification
            NEXT_ACTION -> icon = R.drawable.ic_skip_next_notification
        }

        return NotificationCompat.Action.Builder(icon, action, playerAction(action)).build()
    }

    private fun playerAction(action: String): PendingIntent? {
        val pausedIntent = Intent()
        pausedIntent.action = action

        return PendingIntent.getBroadcast(playerService, REQUEST_CODE, pausedIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                val notificationChannel = NotificationChannel(CHANNEL_ID, "Music Player",
                    NotificationManager.IMPORTANCE_LOW)
                notificationChannel.description = "MusicPlayer"
                notificationChannel.enableLights(false)
                notificationChannel.enableVibration(false)
                notificationChannel.setShowBadge(false)
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }
    }
}