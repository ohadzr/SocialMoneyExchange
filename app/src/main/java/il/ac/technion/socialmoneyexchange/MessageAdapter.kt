package il.ac.technion.socialmoneyexchange


import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Layout
import android.view.Gravity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.solver.widgets.ConstraintWidget
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.internal.ViewUtils.dpToPx
import kotlinx.android.synthetic.main.message_item.view.*

class MessageAdapter(
    val messages: ArrayList<Message>,
    val itemClick: (Message) -> Unit
) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_item, parent, false)

        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(messages[position])
    }

    override fun getItemCount() = messages.size

    class ViewHolder(view: View, val itemClick: (Message) -> Unit) : RecyclerView.ViewHolder(view) {

        @SuppressLint("WrongConstant")
        fun bindForecast(message: Message) {
            if(message.currentUser!!) {
                itemView.messageAdapterMessageItem.setTextColor(Color.rgb(0,204,255))
//                itemView.messageAdapterMessageItem.setBackgroundResource(R.drawable.rounded_corner_mine)


            }
            else {
//                itemView.messageAdapterMessageItem.setBackgroundResource(R.drawable.rounded_corner_other)
                itemView.messageAdapterMessageItem.gravity = Gravity.END            }
//            itemView.messageAdapterMessageItem.text = " "+message.text+" "
        }
    }
}