package navjot.com.musicplayerapp.fragments

import android.content.*
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.controls.*
import kotlinx.android.synthetic.main.fragment_music.*
import kotlinx.android.synthetic.main.seek_bar.*

import navjot.com.musicplayerapp.R
import navjot.com.musicplayerapp.adapters.SongsAdapter
import navjot.com.musicplayerapp.helper_classes.*
import navjot.com.musicplayerapp.models.Music
import navjot.com.musicplayerapp.services.PlayerService
import navjot.com.musicplayerapp.viewmodel.MusicViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_INVERTED = "param1"
private const val ARG_ACCENT = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MusicFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MusicFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MusicFragment : Fragment() {

    private lateinit var mActivity: AppCompatActivity

    // TODO: Rename and change types of parameters
    private lateinit var preferences: SharedPreferenceHelper
    private var themeInverted: Boolean = false
    private var accent: Int = R.color.blue
    private var listener: OnFragmentInteractionListener? = null

    //RecyclerViews
    private lateinit var songsRecyclerView: RecyclerView

    //adapters
    private lateinit var songsAdapter: SongsAdapter

    //layout managers
    private lateinit var songsLayoutManager: LinearLayoutManager

    private lateinit var savedSongsRecyclerLayoutState: Parcelable

    //controls/settings
    private lateinit var controlsContainer: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var playingSong: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var songPosition: TextView
    private lateinit var songDuration: TextView
    private lateinit var skipPreviousButton: ImageView
    private lateinit var playPauseButton: ImageView
    private lateinit var skipNextButton: ImageView

    //view model
    private lateinit var viewModel: MusicViewModel
    private lateinit var allDeviceSongs: MutableList<Music>

    //booleans
    private var bound: Boolean = false
    private var userIsSeeking = false

    //player
    private lateinit var playerService: PlayerService
    private lateinit var mediaPlayerHolder: MediaPlayerHolder
    private lateinit var musicNotificationManager: MusicNotificationManager

    override fun onResume() {
        super.onResume()
        if(::savedSongsRecyclerLayoutState.isInitialized) {
            songsLayoutManager.onRestoreInstanceState(savedSongsRecyclerLayoutState)
        }
        if(::mediaPlayerHolder.isInitialized && mediaPlayerHolder.isMediaPlayer) {
            mediaPlayerHolder.onResumeActivity()
        }
    }

    override fun onPause() {
        super.onPause()
        if(::mediaPlayerHolder.isInitialized && mediaPlayerHolder.isMediaPlayer) {
            mediaPlayerHolder.onPauseActivity()
        }
        if(::songsLayoutManager.isInitialized) {
            savedSongsRecyclerLayoutState = songsLayoutManager.onSaveInstanceState()!!
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            themeInverted = it.getBoolean(ARG_INVERTED)
            accent = it.getInt(ARG_ACCENT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mActivity = activity as AppCompatActivity

        initializeViews()
        setUpPlayerControls()
        setUpSettings()
        initializeSeekBar()
        bindService()
    }

    private fun initializeViews() {
        //main
        musicFragment.setBackgroundColor(
            ColorUtils.setAlphaComponent(
                UIHelperClass.getColor(mActivity, accent, R.color.blue),
                if(themeInverted) 10 else 40
            )
        )

        //recycler views
        songsRecyclerView = songs_rv

        //controls
        controlsContainer = controls_container
        bottomSheetBehavior = BottomSheetBehavior.from(design_bottom_sheet)
        seekBar = seekTo
        playingSong = playing_song
        songPosition = song_position
        songDuration = duration
        skipPreviousButton = skip_prev_button
        skipNextButton = skip_next_button
        playPauseButton = play_pause_button

    }

    private fun setUpPlayerControls() {
        skipPreviousButton.setOnClickListener { skipPrev() }
        skipNextButton.setOnClickListener { skipNext() }
        playPauseButton.setOnClickListener { resumeOrPause() }
    }

    private fun setUpSettings() {}

    private fun initializeSeekBar() {
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {

            val defaultPositionColor = songPosition.currentTextColor
            var userSelectedPosition = 0

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    userSelectedPosition = progress
                    songPosition.setTextColor(UIHelperClass.getColor(mActivity, accent, R.color.blue))
                }
                songPosition.text = MusicUtils.formatSongDuration(progress.toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                userIsSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(userIsSeeking) {
                    songPosition.setTextColor(defaultPositionColor)
                }
                userIsSeeking = false
                mediaPlayerHolder.seekTo(userSelectedPosition)
            }
        })
    }

    private fun bindService() {
        val intent = Intent(activity, PlayerService::class.java)
        mActivity.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        bound = true
        mActivity.startService(intent)
    }

    private fun resumeOrPause() {
        if(checkMediaPlayer()) {
            mediaPlayerHolder.resumeOrPause()
        }
    }

    private fun skipNext() {
        if(checkMediaPlayer()) {
            mediaPlayerHolder.skip(true)
        }
    }

    private fun skipPrev() {
        if(checkMediaPlayer()) {
            mediaPlayerHolder.instantReset()
            if(mediaPlayerHolder.isReset) {
                mediaPlayerHolder.reset()
                updateResetStatus(false)
            }
        }
    }

    private fun checkMediaPlayer(): Boolean {
        val isPlayer = mediaPlayerHolder.isMediaPlayer
        if(!isPlayer) {
            Toast.makeText(activity, "Play a song first", Toast.LENGTH_LONG).show()
        }
        return isPlayer
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    private val connection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            playerService = (service as PlayerService.LocalBinder).instance
            mediaPlayerHolder = playerService.mediaPlayerHolder!!
            mediaPlayerHolder.musicFragment = this@MusicFragment
            musicNotificationManager = playerService.musicNotificationManager
            musicNotificationManager.accent = UIHelperClass.getColor(mActivity, accent, R.color.blue)
            loadMusic()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    private fun loadMusic() {
        viewModel = ViewModelProviders.of(this).get(MusicViewModel::class.java)

        viewModel.getListOfSongs(MusicUtils.getMusicCursor(mActivity.contentResolver)!!)
            .observe(this, Observer<MutableList<Music>> { music ->
                allDeviceSongs = music
                setSongsRecyclerView()
                restorePlayerStatus()
            })
    }

    private fun setSongsRecyclerView() {

        if(!::songsAdapter.isInitialized) {
            //one time adapter initialization
            songsRecyclerView.setHasFixedSize(true)
            songsLayoutManager = LinearLayoutManager(activity)
            songsRecyclerView.layoutManager = songsLayoutManager
            songsAdapter = SongsAdapter(allDeviceSongs.toMutableList())
            songsRecyclerView.adapter = songsAdapter
        } else {
            songsAdapter.swapSongs(allDeviceSongs.toMutableList())
        }

        songsRecyclerView.setPadding(0,0,0,-8)
        songsAdapter.onSongClick =  { music ->
            if(!seekBar.isEnabled) seekBar.isEnabled = true
            mediaPlayerHolder.setCurrentSong(music, allDeviceSongs)
            mediaPlayerHolder.initMusicPlayer(music)
        }
    }

    private fun restorePlayerStatus() {
        if(::mediaPlayerHolder.isInitialized) {
            seekBar.isEnabled = mediaPlayerHolder.isMediaPlayer

            //if playing and the activity is restarted then update the control panel
            if(mediaPlayerHolder.isMediaPlayer) {
                mediaPlayerHolder.onResumeActivity()
                updatePlayingInfo(true, false)
            }
        }
    }

    fun onStateChanged() {
        updatePlayingStatus()
        if(mediaPlayerHolder.state != RESUMED && mediaPlayerHolder.state != PAUSED) {
            updatePlayingInfo(false, true)
        }
    }

    fun onPlaybackCompleted() {
        updateResetStatus(true)
    }

    private fun updateResetStatus(playbackCompletion: Boolean) {
        val themeColor = if(themeInverted) R.color.white else R.color.black
        val color = when {
            playbackCompletion -> themeColor
            mediaPlayerHolder.isReset -> accent
            else -> themeColor
        }
        skipPreviousButton.setColorFilter(
            UIHelperClass.getColor(
                mActivity, color, if(playbackCompletion) themeColor else R.color.blue
            ), PorterDuff.Mode.SRC_IN
        )
    }

    private fun updatePlayingInfo(restore: Boolean, startPlay: Boolean) {
        if(startPlay) {
            mediaPlayerHolder.mediaPlayer!!.start()
            playerService.startForeground(NOTIFICATION_ID, musicNotificationManager.createNotification())
        }

        val selectedSong = mediaPlayerHolder.currentSong
        val duration = selectedSong!!.duration
        seekBar.max = duration.toInt()
        songDuration.text = MusicUtils.formatSongDuration(duration)
        playingSong.text = MusicUtils.buildSpanned(getString(R.string.playing_song,
            selectedSong.artist, selectedSong.title))

        if(restore) {
            songPosition.text = MusicUtils.formatSongDuration(mediaPlayerHolder.playerPosition.toLong())
            seekBar.progress = mediaPlayerHolder.playerPosition

            updatePlayingStatus()
            updateResetStatus(false)

            if(playerService.isRestoredFromPause) {
                playerService.stopForeground(false)
                playerService.musicNotificationManager.notificationManager.notify(
                    NOTIFICATION_ID,
                    playerService.musicNotificationManager.notificationBuilder!!.build()
                )
                playerService.isRestoredFromPause = false
            }
        }
    }

    fun onPositionChanged(position: Int) {
        if(!userIsSeeking) {
            seekBar.progress = position
        }
    }

    private fun updatePlayingStatus() {
        val drawable = if (mediaPlayerHolder.state != PAUSED) R.drawable.pause_notification else R.drawable.ic_play_notification
        playPauseButton.setImageResource(drawable)
    }

    override fun onDestroy() {
        super.onDestroy()
        unBindService()
    }

    private fun unBindService() {
        if(bound) {
            mActivity.unbindService(connection)
            bound = false
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MusicFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(themeInverted: Boolean, accent: Int) =
            MusicFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_INVERTED, themeInverted)
                    putInt(ARG_ACCENT, accent)
                }
            }
    }
}
