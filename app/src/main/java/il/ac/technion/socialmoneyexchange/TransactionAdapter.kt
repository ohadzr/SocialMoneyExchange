package il.ac.technion.socialmoneyexchange

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.transaction_list_item.view.*

class TransactionAdapter(val transactionList: MutableList<TransactionRequest>,
                         val context: Context) : RecyclerView.Adapter<ViewHolder>() {

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
        holder.coin_value2.text = transactionList[position].timeStamp
    }

    fun updateItems(newListOfItems: MutableList<TransactionRequest>) {
        transactionList.clear()
        transactionList.addAll(newListOfItems)
        this.notifyDataSetChanged()
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each review to
    val coin_text = view.coin_name_text
    val coin_text2 = view.coin_name_text2
    val coin_value = view.coin_value
    val coin_value2 = view.coin_value2
}