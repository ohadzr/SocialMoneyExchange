package il.ac.technion.socialmoneyexchange

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.transaction_list_item.view.*

class TransactionAdapter(val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    var transactionList = mutableListOf<TransactionRequest>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    var transactionIDs = mutableListOf<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.transaction_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return transactionList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.coin_text.text = transactionList[position].myCurrency
        holder.coin_text2.text = "Date"
        holder.coin_value.text = transactionList[position].requestedAmount.toString()
        val timeStamp = transactionList[position].timeStamp
        var date = timeStamp!!.substring(6,8)+"/"+timeStamp!!.substring(4,6)+"/"+timeStamp!!.substring(0,4)
        holder.coin_value2.text = date
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("Radius",transactionList[position].radius.toString())
            intent.putExtra("Lat",transactionList[position].latitude.toString())
            intent.putExtra("Long",transactionList[position].longitude.toString())
            intent.putExtra("PickedCurrency",transactionList[position].myCurrency)
            var myAddedCoins = 0F
            for(i in 0 until transactionList[position].requestedCurrencies!!.size){
                if(transactionList[position].requestedCurrencies!![i]!="")
                    myAddedCoins++
            }
            intent.putExtra("savedAddedCoins",myAddedCoins.toString())
            intent.putExtra("pickedAmount",transactionList[position].requestedAmount.toString())
            intent.putExtra("savedRequestId",transactionIDs[position])
            intent.putExtra("savedRequestedCurrencies",transactionList[position].requestedCurrencies)
            intent.putExtra("fromEdit","true")
            context.startActivity(intent)
        }
        holder.deleteBtn.setOnClickListener(){
            holder
        }

    }
    fun removeAt(position: Int) {
        val database = FirebaseDatabase.getInstance()
        database.getReference("transactionRequests").child(transactionIDs[position]).removeValue()
        transactionList.removeAt(position)
        transactionIDs.removeAt(position)
        notifyItemRemoved(position)
    }

//    fun updateItems(newListOfItems: MutableList<TransactionRequest>) {
//        transactionList.clear()
//        transactionList.addAll(newListOfItems)
//        this.notifyDataSetChanged()
//    }

}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each review to
    val coin_text = view.coin_name_text
    val coin_text2 = view.coin_name_text2
    val coin_value = view.coin_value
    val coin_value2 = view.coin_value2
    val deleteBtn = view.delete_button
}
