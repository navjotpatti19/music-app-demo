package navjot.com.musicplayerapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import navjot.com.musicplayerapp.R
import navjot.com.musicplayerapp.helper_classes.UIHelperClass

class ColorsAdapter(private val context: Context, private val accent: Int) :
    RecyclerView.Adapter<ColorsAdapter.ColorHolder>(){

    var onColorClick: ((Int) -> Unit)? = null

    private val colors = intArrayOf(
        R.color.red,
        R.color.pink,
        R.color.purple,
        R.color.deep_purple,
        R.color.indigo,
        R.color.blue,
        R.color.light_blue,
        R.color.cyan,
        R.color.teal,
        R.color.green,
        R.color.amber,
        R.color.orange,
        R.color.deep_orange,
        R.color.brown,
        R.color.gray,
        R.color.blue_gray
    )

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ColorHolder {
        return ColorHolder(LayoutInflater.from(parent.context).inflate(R.layout.color_option, parent, false))
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    override fun onBindViewHolder(holder: ColorHolder, p1: Int) {
        holder.bindItems(colors[holder.adapterPosition])
    }


    inner class ColorHolder(itemView: View): RecyclerView.ViewHolder(itemView)  {
        fun bindItems(color: Int) {
            val colorOption = itemView as ImageView
            val drawable = if (color != accent) R.drawable.checkbox_blank else R.drawable.checkboc_checked
            val colorFromInt = UIHelperClass.getColor(context, color, R.color.blue)
            colorOption.setImageResource(drawable)
            colorOption.setColorFilter(colorFromInt)
            itemView.setOnClickListener {
                onColorClick?.invoke(color)
            }
        }
    }
}