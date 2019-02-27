package navjot.com.musicplayerapp.helper_classes

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.PowerManager
import navjot.com.musicplayerapp.fragments.MusicFragment
import navjot.com.musicplayerapp.models.Music
import navjot.com.musicplayerapp.services.PlayerService
import java.lang.Exception
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

//no audio focus and can't duck(play at low volume)
private const val AUDIO_NO_FOCUS_NO_DUCK = 0

//no audio focus and can duck(play at low volume)
private const val AUDIO_NO_FOCUS_CAN_DUCK = 0

//volume we set the media player when we lose focus but allowed to reduce the volume
private const val VOLUME_DUCK = 0.2f

//the we set the music player when we have focus
private const val VOLUME_NORMAL = 1.0f

// we have full audio focus
private const val AUDIO_FOCUSED = 2

// The headset connection states (0,1)
private const val HEADSET_DISCONNECTED = 0
private const val HEADSET_CONNECTED = 1

// Player playing statuses
const val PLAYING = 0
const val PAUSED = 1
const val RESUMED = 2

class MediaPlayerHolder(private val playerservice:PlayerService): MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener {

    lateinit var musicFragment: MusicFragment

    //media player variable declarations
    var mediaPlayer: MediaPlayer? = null
    private var executer: ScheduledExecutorService? = null
    private var seekBarPositionUpdateTask: Runnable? = null
    private var playingAlbumSongs: List<Music>? = null
    var currentSong: Music? = null
    val playerPosition: Int get() = mediaPlayer!!.currentPosition

    //mediaplayer state
    val isPlaying: Boolean get() = isMediaPlayer && mediaPlayer!!.isPlaying
    val isMediaPlayer: Boolean get() = mediaPlayer != null
    var isReset = false
    var state: Int? = PAUSED

    //audio focus
    private var audioManager: AudioManager = playerservice.getSystemService(AUDIO_SERVICE) as AudioManager
    private lateinit var audioFocusRequestOreo: AudioFocusRequest
    private val handler = Handler()
    private var currentAudiofocusState = AUDIO_NO_FOCUS_NO_DUCK
    private var playOnFocusGain: Boolean = false

    private var notificationActionsReceiver: NotificationReceiver? = null
    private var musicNotificationManager: MusicNotificationManager? = null

    fun setCurrentSong(song: Music, songs: List<Music>) {
        currentSong = song
        playingAlbumSongs = songs
    }

    override fun onCompletion(mp: MediaPlayer?) {
        if(::musicFragment.isInitialized) {
            musicFragment.onStateChanged()
            musicFragment.onPlaybackCompleted()
        }

        if(isReset) {
            if(isMediaPlayer) {
                resetSong()
            }
            isReset = false
        } else {
            skip(true)
        }
    }

    private fun resetSong() {
        mediaPlayer!!.seekTo(0)
        mediaPlayer!!.start()
        setStatus(PLAYING)
    }

    override fun onPrepared(mp: MediaPlayer?) {
        startUpdatingCallbackWithPosition()
        setStatus(PLAYING)
    }

    fun onResumeActivity() {
        startUpdatingCallbackWithPosition()
    }

    fun onPauseActivity() {
        stopUpdatingCallbackWithPosition()
    }

    private fun startUpdatingCallbackWithPosition() {
        if(executer == null) {
            executer = Executors.newSingleThreadScheduledExecutor()
        }

        if(seekBarPositionUpdateTask == null) {
            seekBarPositionUpdateTask = Runnable { this.updateProgressCallbackTask() }
        }

        executer!!.scheduleAtFixedRate(
            seekBarPositionUpdateTask, 0, 1000, TimeUnit.MILLISECONDS
        )
    }

    private fun stopUpdatingCallbackWithPosition() {
        if(executer != null) {
            executer!!.shutdownNow()
            executer = null
            seekBarPositionUpdateTask = null
        }
    }

    private fun updateProgressCallbackTask() {
        if(isMediaPlayer && mediaPlayer!!.isPlaying) {
            val currentPosition = mediaPlayer!!.currentPosition
            if(::musicFragment.isInitialized) {
                musicFragment.onPositionChanged(currentPosition)
            }
        }
    }

    fun registerNotificationActionsReceiver(isReceiver: Boolean) {
        if(isReceiver) {
            registerActionsReceiver()
        } else {
            unregisterActionsReceiver()
        }
    }

    private fun registerActionsReceiver() {
        notificationActionsReceiver = NotificationReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(PREV_ACTION)
        intentFilter.addAction(PLAY_PAUSE_ACTION)
        intentFilter.addAction(NEXT_ACTION)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

        playerservice.registerReceiver(notificationActionsReceiver, intentFilter)
    }

    private fun unregisterActionsReceiver() {
        if(notificationActionsReceiver != null) {
            try {
                playerservice.unregisterReceiver(notificationActionsReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun instantReset() {
        if(isMediaPlayer) {
            if(mediaPlayer!!.currentPosition < 5000) {
                skip(false)
            }
        }
    }

    fun reset() {
        isReset = !isReset
    }

    fun skip(next: Boolean) {
        getSkipSong(next)
    }

    private fun getSkipSong(next: Boolean) {
        val currentIndex = playingAlbumSongs!!.indexOf(currentSong)
        val index: Int

        try {
            index = if (next) currentIndex + 1 else currentIndex - 1
            currentSong = playingAlbumSongs!![index]
        } catch (e: Exception) {
            currentSong = if (currentIndex != 0) playingAlbumSongs!![0] else
                playingAlbumSongs!![playingAlbumSongs!!.size - 1]
            e.printStackTrace()
        }
        initMusicPlayer(currentSong!!)
    }

    fun initMusicPlayer(currentSong: Music) {

        try {
            if(mediaPlayer != null) {
                mediaPlayer!!.reset()
            } else {
                mediaPlayer = MediaPlayer()
                Equalizer.openAudioEffectSession(playerservice.applicationContext,
                    mediaPlayer!!.audioSessionId)

                mediaPlayer!!.setOnPreparedListener(this)
                mediaPlayer!!.setOnCompletionListener(this)
                mediaPlayer!!.setWakeMode(playerservice, PowerManager.PARTIAL_WAKE_LOCK)
                mediaPlayer!!.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                musicNotificationManager = playerservice.musicNotificationManager
            }
            tryToGetAudioFocus()
            mediaPlayer!!.setDataSource(currentSong.path)
            mediaPlayer!!.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun tryToGetAudioFocus() {
        currentAudiofocusState = when (getAudioFocusResult()) {
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> AUDIO_FOCUSED
            else -> AUDIO_NO_FOCUS_NO_DUCK
        }
    }

    private fun getAudioFocusResult(): Int {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequestOreo = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                })
                setOnAudioFocusChangeListener(onAudioFocusChangeListener, handler)
                    .build()
            }
            audioManager.requestAudioFocus(audioFocusRequestOreo)
        } else {
            audioManager.requestAudioFocus(
                onAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private val onAudioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> currentAudiofocusState = AUDIO_FOCUSED

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                currentAudiofocusState = AUDIO_NO_FOCUS_CAN_DUCK

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                currentAudiofocusState = AUDIO_NO_FOCUS_NO_DUCK
                playOnFocusGain = isMediaPlayer && state == PLAYING || state == RESUMED
            }

            AudioManager.AUDIOFOCUS_LOSS ->
                currentAudiofocusState = AUDIO_NO_FOCUS_NO_DUCK
        }
        if(mediaPlayer != null) {
            //update the player state based on change
            configurePlayerState()
        }
    }

    private fun configurePlayerState() {

        when(currentAudiofocusState) {
            AUDIO_NO_FOCUS_NO_DUCK -> pauseMediaPlayer()
            else -> {
                when (currentAudiofocusState) {
                    AUDIO_NO_FOCUS_CAN_DUCK -> mediaPlayer!!.setVolume(VOLUME_DUCK, VOLUME_DUCK)
                    else -> mediaPlayer!!.setVolume(VOLUME_NORMAL, VOLUME_NORMAL)
                }

                //if we were playing when we lost focus , we need to resume paying
                if(playOnFocusGain) {
                    resumeMediaPlayer()
                    playOnFocusGain = false
                }
            }
        }
    }

    fun resumeOrPause() {
        if(isPlaying) {
            pauseMediaPlayer()
        } else {
            resumeMediaPlayer()
        }
    }

    private fun resumeMediaPlayer() {
        if(!isPlaying) {
            mediaPlayer!!.start()
            setStatus(RESUMED)
            playerservice.startForeground(
                NOTIFICATION_ID, musicNotificationManager!!.createNotification()
            )
        }
    }

    private fun setStatus(status: Int) {
        state = status
        if(::musicFragment.isInitialized) {
            musicFragment.onStateChanged()
        }
    }

    private fun pauseMediaPlayer() {
        setStatus(PAUSED)
        mediaPlayer!!.pause()
        playerservice.stopForeground(false)
        musicNotificationManager!!.notificationManager
            .notify(NOTIFICATION_ID, musicNotificationManager!!.createNotification())
    }

    fun seekTo(position: Int) {
        if(isMediaPlayer) {
            mediaPlayer!!.seekTo(position)
        }
    }

    private inner class NotificationReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action

            if(action != null) {
                when (action) {
                    PREV_ACTION -> instantReset()
                    PLAY_PAUSE_ACTION -> resumeOrPause()
                    NEXT_ACTION -> skip(true)
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> if(currentSong != null) {
                        pauseMediaPlayer()
                    }
                    BluetoothDevice.ACTION_ACL_CONNECTED -> if(currentSong != null && !isPlaying) {
                        resumeMediaPlayer()
                    }
                    Intent.ACTION_HEADSET_PLUG -> if(currentSong != null) {
                        when(intent.getIntExtra("state", -1)) {
                            //0 means disconnected
                            HEADSET_DISCONNECTED -> pauseMediaPlayer()

                            //1 means connected
                            HEADSET_CONNECTED -> if(!isPlaying) {
                                resumeMediaPlayer()
                            }
                        }
                    }
                    AudioManager.ACTION_AUDIO_BECOMING_NOISY -> if(isPlaying) {
                        pauseMediaPlayer()
                    }
                }
            }
        }
    }
}