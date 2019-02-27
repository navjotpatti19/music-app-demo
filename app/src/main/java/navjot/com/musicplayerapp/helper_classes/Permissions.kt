package navjot.com.musicplayerapp.helper_classes

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import navjot.com.musicplayerapp.R

class Permissions {
    private var activity: Activity? = null
    fun isPermissionGranted(activity: Activity, permissionsList: ArrayList<String>, requestCode: Int): Boolean {
        this.activity = activity
        for(permission in permissionsList) {
            if(ContextCompat.checkSelfPermission(activity,permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(activity, permissionsList, requestCode)
                return false
            }
        }
        return true
    }

    private fun requestPermissions(activity: Activity, permissionsList: ArrayList<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissionsList.toTypedArray(), requestCode)
    }

    fun shouldShowRequestPermissionRationale(activity: Activity,
                                             permissionsList: ArrayList<String>,
                                             requestCode: Int): Boolean {
        if(!permissionsList.isEmpty()) {
            for(permission in permissionsList) {
                val isPermissionTrue = ActivityCompat.
                        shouldShowRequestPermissionRationale(activity, permission)
                if(!isPermissionTrue) {
                    openSettingsForPermission(activity, requestCode)
                    return false
                } else {
                    requestPermissions(activity, permissionsList, requestCode)
                }
            }
        }
        return true
    }

    private fun openSettingsForPermission(activity: Activity, requestCode: Int) {
        val alertDialog = AlertDialog.Builder(activity).create()
        alertDialog.setTitle(activity.getString(R.string.app_name))
        alertDialog.setMessage(activity.getString(R.string.setting_permission))
        alertDialog.setCancelable(false)
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK"
        ) { _, _ ->
            activity.finish()
            activity.finishAffinity()

        }
        alertDialog.show()
    }

    private fun openPermissionDialog(activity: Activity, permissionsList: ArrayList<String>,
                                     requestCode: Int) {
        val alertDialog = AlertDialog.Builder(activity).create()
        alertDialog.setTitle(activity.getString(R.string.app_name))
        alertDialog.setMessage(activity.getString(R.string.permission))
        alertDialog.setCancelable(false)
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "OK"
        ) { dialog, _ ->
            dialog.cancel()
//                requestPermissions(activity, permissionsList, requestCode)
        }
        alertDialog.show()
    }
}