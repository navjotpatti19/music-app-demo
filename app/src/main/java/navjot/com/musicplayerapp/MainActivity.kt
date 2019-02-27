package navjot.com.musicplayerapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import navjot.com.musicplayerapp.fragments.MusicFragment
import navjot.com.musicplayerapp.helper_classes.Permissions
import navjot.com.musicplayerapp.helper_classes.SharedPreferenceHelper

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferenceHelper: SharedPreferenceHelper
    private var accent: Int? = R.color.blue
    private var themeInverted:Boolean = false
    private var REQUEST_CODE = 100
    val permissionObj = Permissions()
    val permissionsList = arrayListOf(
        Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE)
    private lateinit var musicFragment: MusicFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferenceHelper = SharedPreferenceHelper(this)

        accent = sharedPreferenceHelper.getAccent()
        themeInverted = sharedPreferenceHelper.isThemeInverted()

        sharedPreferenceHelper.applyTheme(accent!!, themeInverted)

        setContentView(R.layout.activity_main)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           checkRequiredPermissions()
        } else {
            initializeMusicFragment()
        }
    }

    private fun checkRequiredPermissions() {
        if(permissionObj.isPermissionGranted(this, permissionsList, REQUEST_CODE)) {
            initializeMusicFragment()
        }
    }

    private fun initializeMusicFragment() {
        supportFragmentManager.inTransaction {
            musicFragment = MusicFragment.newInstance(themeInverted, accent!!)
            replace(R.id.fragment_container, musicFragment)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                var isGranted = false
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            isGranted = false
                            break
                        } else {
                            isGranted = true
                        }
                    }

                    if (isGranted) {
                        //initialize the map
                        initializeMusicFragment()
                    } else {
                        permissionObj.shouldShowRequestPermissionRationale(this, permissionsList, requestCode)
                    }
                }
            }
        }
    }

    private inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commitNow()
    }
}
