package navjot.com.musicplayerapp.helper_classes

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import navjot.com.musicplayerapp.R
import java.lang.Exception

private const val TAG_ACCENT = "accent"
private const val TAG_ACCENT_VALUE = "accent_value"
private const val TAG_THEME = "theme"
private const val TAG_THEME_VALUE = "theme_value"
private const val TAG_SESRCH = "search"
private const val TAG_SERCH_VALUE = "search_value"

class SharedPreferenceHelper(private val activity: Activity) {

    private fun getPreference(key: String): SharedPreferences {
        return activity.getSharedPreferences(key, Context.MODE_PRIVATE)
    }

    fun invertTheme() {
        val value = !isThemeInverted()
        getPreference(TAG_THEME).edit().putBoolean(TAG_THEME_VALUE, value).apply()
        activity.recreate()
    }

    fun isThemeInverted(): Boolean {
        return getPreference(TAG_THEME).getBoolean(TAG_THEME_VALUE, false)
    }

    fun applyTheme(accent: Int, isThemeInverted: Boolean) {
        val theme = resolveTheme(isThemeInverted, accent)
        activity.setTheme(theme)
    }

    private fun resolveTheme(themeInverted: Boolean, accent: Int): Int {
        return when (accent) {

            R.color.red -> if(themeInverted) R.style.AppThemeRedInverted else R.style.AppThemeRed

            R.color.pink -> if(themeInverted) R.style.AppThemePinkInverted else R.style.AppThemePink

            R.color.purple -> if (themeInverted) R.style.AppThemePurpleInverted else R.style.AppThemePurple

            R.color.deep_purple -> if (themeInverted) R.style.AppThemeDeepPurpleInverted else R.style.AppThemeDeepPurple

            R.color.indigo -> if (themeInverted) R.style.AppThemeIndigoInverted else R.style.AppThemeIndigo

            R.color.blue -> if (themeInverted) R.style.AppThemeBlueInverted else R.style.AppThemeBlue

            R.color.light_blue -> if (themeInverted) R.style.AppThemeLightBlueInverted else R.style.AppThemeLightBlue

            R.color.cyan -> if (themeInverted) R.style.AppThemeCyanInverted else R.style.AppThemeCyan

            R.color.teal -> if (themeInverted) R.style.AppThemeTealInverted else R.style.AppThemeTeal

            R.color.green -> if (themeInverted) R.style.AppThemeGreenInverted else R.style.AppThemeGreen

            R.color.amber -> if (themeInverted) R.style.AppThemeAmberInverted else R.style.AppThemeAmber

            R.color.orange -> if (themeInverted) R.style.AppThemeOrangeInverted else R.style.AppThemeOrange

            R.color.deep_orange -> if (themeInverted) R.style.AppThemeDeepOrangeInverted else R.style.AppThemeDeepOrange

            R.color.brown -> if (themeInverted) R.style.AppThemeBrownInverted else R.style.AppThemeBrown

            R.color.gray -> if (themeInverted) R.style.AppThemeGrayLightInverted else R.style.AppThemeGrayLight

            R.color.blue_gray -> if (themeInverted) R.style.AppThemeBlueGrayInverted else R.style.AppThemeBlueGray

            else -> R.color.blue
        }
    }

    fun getAccent(): Int {
        return try {
            getPreference(TAG_ACCENT).getInt(TAG_ACCENT_VALUE, R.color.blue)
        } catch (e: Exception) {
            R.color.blue
        }
    }
}