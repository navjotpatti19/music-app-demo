package navjot.com.musicplayerapp.helper_classes

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat

object UIHelperClass {

    fun getColor(context: Context, color: Int, emergencyColor: Int): Int {
        return try {
            ContextCompat.getColor(context, color)
        } catch (e :Exception) {
            ContextCompat.getColor(context, emergencyColor)
        }
    }

    fun setHorizontalScrollBehaviour(parentView: View, vararg textViews: TextView) {
        var isLongPressed = false

        parentView.setOnLongClickListener {
            if(!isLongPressed) {
                textViews.forEachIndexed {_, textView ->
                    textView.isSelected = true
                }
                isLongPressed = true
            }
            return@setOnLongClickListener true
        }

        parentView.setOnTouchListener { _, e ->
            if(isLongPressed && e.action == MotionEvent.ACTION_UP ||
                    e.action == MotionEvent.ACTION_OUTSIDE || e.action == MotionEvent.ACTION_MOVE) {

                textViews.forEach {
                    it.isSelected = false
                }
                isLongPressed = true
            }
            return@setOnTouchListener false
        }
    }
}