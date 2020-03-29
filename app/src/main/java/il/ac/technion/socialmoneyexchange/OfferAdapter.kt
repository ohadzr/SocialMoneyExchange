package il.ac.technion.socialmoneyexchange

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.offer_list_item.*
import kotlinx.android.synthetic.main.offer_list_item.view.*

class OfferAdapter(val offersList: MutableList<Offer>,
                         val context: Context) : RecyclerView.Adapter<OfferViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        return OfferViewHolder(LayoutInflater.from(context).inflate(R.layout.offer_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return offersList.size
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.user_name_text.text = offersList[position].userID1
        holder.user_name_text2.text = offersList[position].userID2
        holder.coin_name.text = offersList[position].coinName1
        holder.coin_name2.text = offersList[position].coinName2
        holder.coin_amount.text = offersList[position].coinAmount1.toString()
        holder.coin_amount2.text = offersList[position].coinAmount2.toString()
        //holder.rate.text = "rate" TODO: load rate
        holder.status.text = offersList[position].status

    }

    fun updateItems(newListOfItems: MutableList<Offer>) {
        offersList.clear()
        offersList.addAll(newListOfItems)
        this.notifyDataSetChanged()
    }
}

class OfferViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each review to
    val user_name_text = view.user_name_text
    val user_name_text2 = view.user_name_text2
    val coin_name = view.coin_name
    val coin_name2 = view.coin_name2
    val coin_amount = view.coin_amount
    val coin_amount2 = view.coin_amount2
    val rate = view.rate
    val status = view.status
}