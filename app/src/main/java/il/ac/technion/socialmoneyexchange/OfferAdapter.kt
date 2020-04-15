package il.ac.technion.socialmoneyexchange

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.offer_list_item.view.*
import okhttp3.*
import java.io.IOException

class OfferAdapter(val context: Context) : RecyclerView.Adapter<OfferViewHolder>() {

    var offersList = mutableListOf<Offer>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var offerIDs = mutableListOf<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        return OfferViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.offer_list_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return offersList.size
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        loadUserFirstAndLastName(offersList[position].userID1.toString(), holder.user_name_text)
        loadUserFirstAndLastName(offersList[position].userID2.toString(), holder.user_name_text2)
        holder.coin_name.text = offersList[position].coinName1
        holder.coin_name2.text = offersList[position].coinName2
        holder.coin_amount.text = String.format("%.3f", offersList[position].coinAmount1)
        holder.status.text = offersList[position].status
        if (offersList[position].status == "CANCELLED")
            holder.status.setTextColor(Color.rgb(244, 67, 54)) // red
        if (offersList[position].status == "CONFIRMED") {
            holder.status.setTextColor(Color.rgb(139, 195, 74)) // green
        }
//        else
//            holder.buttonRate.visibility = View.INVISIBLE


        checkIfVoted(holder.buttonRate,offerIDs[position],offersList[position].userID1,offersList[position].userID2)

        if (offersList[position].status == "PENDING")
            holder.status.setTextColor(Color.rgb(255, 112, 67)) // orange
        holder.buttonChat.setOnClickListener() {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("offerId", offerIDs[position])
            intent.putExtra("otherUser", holder.user_name_text2.text)
            context.startActivity(intent)
        }
        holder.buttonRate.setOnClickListener() {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("vote", "true")
            intent.putExtra("offerId", offerIDs[position])
            intent.putExtra("userID1", offersList[position].userID1)
            intent.putExtra("userID2", offersList[position].userID2)
            context.startActivity(intent)
        }
        // check if no value was set by user. If not, load default coin rate
        if (offersList[position].coinAmount2!!.toInt() == -1) {
            val url = "https://api.exchangeratesapi.io/latest"
            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()
            var myApi: CurrencyApi
            var rate: Double? = null
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {

                    val body = response.body()?.string()
                    val gson = GsonBuilder().create()

                    myApi = gson.fromJson(body, CurrencyApi::class.java)
                    myApi.rates["EUR"] = 1.0
                    val coin1 = offersList[position].coinName1
                    val coin2 = offersList[position].coinName2
                    rate = myApi.rates[coin2]!! / myApi.rates[coin1]!!


                }

                override fun onFailure(call: Call, e: IOException) {

                }
            })
            var notUpdated = true
            while (notUpdated) {
                if (rate != null) {
                    // update rate
                    holder.rate.text = String.format("%.3f", rate)

                    // Update coin 2 value
                    holder.coin_amount2.text =
                        String.format("%.3f", rate!! * offersList[position].coinAmount1!!)

                    notUpdated = false
                }
            }

        }

        // If already set once, load new rate
        else {
            holder.rate.text = String.format(
                "%.3f",
                offersList[position].coinAmount1!! / offersList[position].coinAmount2!!
            )
            holder.coin_amount2.text = String.format("%.3f", offersList[position].coinAmount2)
        }

        holder.itemView.setOnClickListener {
            if (offersList[position].status == "CANCELLED" || offersList[position].status == "CONFIRMED")
                Toast.makeText(context, "Can no longer change the offer", Toast.LENGTH_LONG).show()
            else {
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("fromOffer", "true")
                intent.putExtra("offerId", offerIDs[position])
                intent.putExtra("userID1", offersList[position].userID1)
                intent.putExtra("userID2", offersList[position].userID2)
                intent.putExtra("coinAmount1", offersList[position].coinAmount1.toString())
                intent.putExtra("coinName1", offersList[position].coinName1)
                intent.putExtra("coinName2", offersList[position].coinName2)
                intent.putExtra("lastUpdater", offersList[position].lastUpdater)
                intent.putExtra("status", offersList[position].status)
                intent.putExtra("rate", holder.rate.text)
                intent.putExtra("coinAmount2", holder.coin_amount2.text)
                context.startActivity(intent)
            }
        }
    }

    private fun checkIfVoted(buttonChat: MaterialButton?, offerId: String, userID1: String?, userID2: String?) {
        val database = FirebaseDatabase.getInstance()
        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val myUserId = currentFirebaseUser!!.uid
        val offerRef: DatabaseReference = database.getReference("offers").child(offerId).child("vote")
        offerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            // This method is triggered once when the listener is attached
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                val voted = dataSnapshot.child(myUserId).getValue(String::class.java)
                if (!voted.isNullOrEmpty()) {
                    buttonChat!!.text = "Voted"
                    buttonChat!!.isEnabled = false
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })


    }


    fun removeAt(position: Int) {
        val database = FirebaseDatabase.getInstance()
        val offerId = offerIDs[position]
        val userId1 = offersList[position].userID1
        val userId2 = offersList[position].userID2
        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val myId = currentFirebaseUser!!.uid
        offersList.removeAt(position)
        offerIDs.removeAt(position)
        notifyItemRemoved(position)
        if (myId == userId1) {
            removeOfferFromUser(userId1, userId2!!, offerId, database)
        } else
            removeOfferFromUser(userId2!!, userId1!!, offerId, database)


    }

    private fun removeOfferFromUser(
        myUserId: String,
        userId2: String,
        offerId: String,
        database: FirebaseDatabase
    ) {
        val myOffersRef: DatabaseReference =
            database.getReference("users").child(myUserId).child("offers")
        val otherOffersRef: DatabaseReference =
            database.getReference("users").child(userId2).child("offers")
        myOffersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (data in dataSnapshot.children) {
                    val dbOfferId = data.getValue<String>(String::class.java)
                    if (offerId == dbOfferId) {
                        database.getReference("users").child(myUserId).child("offers")
                            . child(data.key.toString()).removeValue()
                    }
                    break
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
        otherOffersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var foundOffer = false
                for (data in dataSnapshot.children) {
                    val dbOfferId = data.getValue<String>(String::class.java)
                    if (offerId == dbOfferId) {
                        foundOffer = true
                    }
                    break
                }
                if (!foundOffer)
                    database.getReference("offers").child(offerId).removeValue()//both users don't have it so can delete
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
    }

    override fun getItemViewType(position: Int): Int {
        return if (offersList[position].status == "CANCELLED" || offersList[position].status == "CONFIRMED")
            1
        else
            0
    }

    private fun loadUserFirstAndLastName(userID: String, textView: TextView) {

        val database = FirebaseDatabase.getInstance()
        val userRef: DatabaseReference = database.getReference("users").child(userID)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            // This method is triggered once when the listener is attached
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // first, check if user first name exists
                val firstName = dataSnapshot.child("firstName").getValue(String::class.java)
                val lastName = dataSnapshot.child("lastName").getValue(String::class.java)
                textView.text = "$firstName $lastName"
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Ohad", "Failed to read user first and last name.", error.toException())
            }
        })
    }

}


class OfferViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each review to
    val user_name_text = view.user_name_text
    val user_name_text2 = view.user_name_text2
    val coin_name = view.coin_name
    val coin_name2 = view.coin_name2
    val coin_amount = view.coin_amount
    val coin_amount2 = view.coin_amount2
    val rate = view.rate
    val status = view.status
    val buttonChat = view.chat_button
    val buttonRate = view.rate_button
}