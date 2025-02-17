package il.ac.technion.socialmoneyexchange

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_offer.view.*
import kotlinx.android.synthetic.main.fragment_user_profile_public.view.*
import kotlinx.android.synthetic.main.fragment_user_profile_public.view.name_text
import kotlinx.android.synthetic.main.fragment_user_profile_public.view.user_img_public
import okhttp3.*
import java.io.IOException


class OfferFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
    lateinit var offerId: String
    lateinit var userID1: String
    lateinit var userID2: String
    lateinit var coinAmount1: String
    lateinit var coinAmount2: String
    lateinit var coinName1: String
    lateinit var coinName2: String
    lateinit var lastUpdater: String
    lateinit var status: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            offerId = savedInstanceState.getString("offerId").toString()
            userID1 = savedInstanceState.getString("userID1").toString()
            userID2 = savedInstanceState.getString("userID2").toString()
            coinAmount1 = savedInstanceState.getString("coinAmount1").toString()
            coinAmount2 = savedInstanceState.getString("coinAmount2").toString()
            coinName1 = savedInstanceState.getString("coinName1").toString()
            coinName2 = savedInstanceState.getString("coinName2").toString()
            lastUpdater = savedInstanceState.getString("lastUpdater").toString()

        } else if (arguments != null) {
            offerId = arguments!!.getString("offerId").toString()
            userID1 = arguments!!.getString("userID1").toString()
            userID2 = arguments!!.getString("userID2").toString()
            coinAmount1 = arguments!!.getString("coinAmount1").toString()
            coinAmount2 = arguments!!.getString("coinAmount2").toString()
            coinName1 = arguments!!.getString("coinName1").toString()
            coinName2 = arguments!!.getString("coinName2").toString()
            lastUpdater = arguments!!.getString("lastUpdater").toString()
            status = arguments!!.getString("status").toString()

        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("offerId", offerId)
        outState.putString("userID1", userID1)
        outState.putString("userID2", userID2)
        outState.putString("coinAmount1", coinAmount1)
        outState.putString("coinAmount2", coinAmount2)
        outState.putString("coinName1", coinName1)
        outState.putString("coinName2", coinName2)
        outState.putString("lastUpdater", lastUpdater)
        outState.putString("status", status)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_offer, container, false)
        database = FirebaseDatabase.getInstance()
        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val userId = currentFirebaseUser!!.uid

        val url = "https://api.exchangeratesapi.io/latest"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        var myApi: CurrencyApi
        var originalRate: Float? = null
        loadUsersData(userID1,userID2,view)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.offersFragment)

                }
            }
            )

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {

                val body = response.body()?.string()
                val gson = GsonBuilder().create()

                myApi = gson.fromJson(body, CurrencyApi::class.java)
                myApi.rates["EUR"] = 1.0
                originalRate = (myApi.rates[coinName2]!! / myApi.rates[coinName1]!!).toFloat()

            }

            override fun onFailure(call: Call, e: IOException) {
                Log.d("Ohad", "Error loading currency API")
            }
        })



        val coinName1TextView: TextView = view.findViewById(R.id.coin_name_text) as TextView
        val coinName2TextView: TextView = view.findViewById(R.id.coin_name_text2) as TextView
        val coinAmount1TextView: TextView = view.findViewById(R.id.coin_value) as TextView
        val coinAmount2TextView: TextView = view.findViewById(R.id.coin_value2) as TextView

        val resetButton = view.findViewById(R.id.reset_button) as Button
        val saveButton = view.findViewById(R.id.accept_button) as Button
        val declineButton = view.findViewById(R.id.decline_button) as Button
        if(userId == userID1) {
            setImgClickable(view.user_img_public2)
            setTxtlickable(view.name_text2)
        }
        else {
            setImgClickable(view.user_img_public)
            setTxtlickable(view.name_text)
        }
        // Load original data to text boxes for the first time
        resetTextValues(
            coinName1, coinAmount1, coinName2, coinAmount2,
            coinName1TextView, coinName2TextView, coinAmount1TextView, coinAmount2TextView
        )

        // Set reset button to load original data
        resetButton.setOnClickListener {
            resetTextValues(
                coinName1, coinAmount1, coinName2, coinAmount2,
                coinName1TextView, coinName2TextView, coinAmount1TextView, coinAmount2TextView
            )
        }

        // Set save button to accept transaction and save it to DB
        saveButton.setOnClickListener {
            saveOrCancelOffer(
                database, userId,
                coinName1, coinAmount1TextView,
                coinName2, coinAmount2TextView, offerId, cancel = false
            )
            findNavController().navigate(R.id.offersFragment)
        }

        // Set decline button to cancel transaction and save it to DB
        declineButton.setOnClickListener {
            saveOrCancelOffer(
                database, userId,
                coinName1, coinAmount1TextView,
                coinName2, coinAmount2TextView, offerId, cancel = true
            )
            findNavController().navigate(R.id.offersFragment)
        }


        updateExchangeRate(view, coinAmount1TextView, coinAmount2TextView, originalRate)

        // Update exchange rate if text is changed
        coinAmount1TextView.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                updateExchangeRate(view, coinAmount1TextView, coinAmount2TextView, originalRate)
            }
        })

        // Update exchange rate if text is changed
        coinAmount2TextView.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                updateExchangeRate(view, coinAmount1TextView, coinAmount2TextView, originalRate)
            }
        })


        return view
    }

    private fun setTxtlickable(nameText: TextView?) {
        nameText?.setOnClickListener{
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("loadProfile", "true")
            intent.putExtra("offerId", offerId)
            intent.putExtra("userID1", userID1)
            intent.putExtra("userID2", userID2)
            intent.putExtra("coinAmount1", coinAmount1)
            intent.putExtra("coinName1", coinName1)
            intent.putExtra("coinName2", coinName2)
            intent.putExtra("lastUpdater", lastUpdater)
            intent.putExtra("status", status)
            intent.putExtra("coinAmount2", coinAmount2)
            startActivity(intent)
        }
    }

    private fun setImgClickable(userImgPublic: ImageView?) {
        userImgPublic?.setOnClickListener{
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("loadProfile", "true")
            intent.putExtra("offerId", offerId)
            intent.putExtra("userID1", userID1)
            intent.putExtra("userID2", userID2)
            intent.putExtra("coinAmount1", coinAmount1)
            intent.putExtra("coinName1", coinName1)
            intent.putExtra("coinName2", coinName2)
            intent.putExtra("lastUpdater", lastUpdater)
            intent.putExtra("status", status)
            intent.putExtra("coinAmount2", coinAmount2)
            startActivity(intent)

        }
    }

    private fun loadUsersData(userID1: String, userID2: String,view: View) {

        val database = FirebaseDatabase.getInstance()
        val user1Ref: DatabaseReference = database.getReference("users").child(userID1)
        val user2Ref: DatabaseReference = database.getReference("users").child(userID2)

        user1Ref.addListenerForSingleValueEvent(object : ValueEventListener {
            // This method is triggered once when the listener is attached
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // first, check if user first name exists
                val firstName = dataSnapshot.child("firstName").getValue(String::class.java)
                val lastName = dataSnapshot.child("lastName").getValue(String::class.java)
                val imgUrl = dataSnapshot.child("imgUrl").getValue(String::class.java)
                view.name_text.text = "$firstName $lastName"
                view.rating_bar1.rating =
                    dataSnapshot.child("rate").getValue(String::class.java)?.toFloat() ?:0F
                bindImage(view.user_img_public,imgUrl )
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
        user2Ref.addListenerForSingleValueEvent(object : ValueEventListener {
            // This method is triggered once when the listener is attached
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // first, check if user first name exists
                val firstName = dataSnapshot.child("firstName").getValue(String::class.java)
                val lastName = dataSnapshot.child("lastName").getValue(String::class.java)
                val imgUrl = dataSnapshot.child("imgUrl").getValue(String::class.java)
                view.name_text2.text = "$firstName $lastName"
                view.rating_bar2.rating =
                    dataSnapshot.child("rate").getValue(String::class.java)?.toFloat() ?:0F
                bindImage(view.user_img_public2,imgUrl)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
    }


    private fun saveOrCancelOffer(
        database: FirebaseDatabase, userId: String,
        coinName1: String, coinAmount1TextView: TextView,
        coinName2: String, coinAmount2TextView: TextView,
        offerID: String, cancel: Boolean
    ) {
        val offerRef: DatabaseReference = database.getReference("offers").child(offerID)
        // cancelling the offer
        if (cancel) {
            offerRef.child("status").setValue("CANCELLED")
            offerRef.child("lastUpdater").setValue(userId)
            return
        }

        offerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val status = dataSnapshot.child("status").getValue(String::class.java)
                val updaterId = dataSnapshot.child("lastUpdater").getValue(String::class.java)

                when (status) {
                    // ACTIVE - first time reviewing offer
                    "ACTIVE" -> {
                        offerRef.child("status").setValue("PENDING")
                        var text: String = coinAmount1TextView.text.toString()
                        val coinAmount1Float: Float = text.toFloat()
                        text = coinAmount2TextView.text.toString()
                        val coinAmount2Float: Float = text.toFloat()
                        offerRef.child("coinAmount1").setValue(coinAmount1Float)
                        offerRef.child("coinAmount2").setValue(coinAmount2Float)
                    }

                    // PENDING - at least one user reviewed offer and accepted it
                    "PENDING" -> {
                        if (updaterId != userId)
                            offerRef.child("status").setValue("CONFIRMED")
                        var text: String = coinAmount1TextView.text.toString()
                        val coinAmount1Float: Float = text.toFloat()
                        text = coinAmount2TextView.text.toString()
                        val coinAmount2Float: Float = text.toFloat()
                        offerRef.child("coinAmount1").setValue(coinAmount1Float)
                        offerRef.child("coinAmount2").setValue(coinAmount2Float)
                    }

                    // CONFIRMED - both users accepted the offer
                    "CONFIRMED" -> offerRef.child("status").setValue("PENDING")

                    // if CANCELLED or DONE - doesn't update anything
                    else -> return
                }
                offerRef.child("lastUpdater").setValue(userId)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("ohad", "Failed to read offer status.", error.toException())
            }
        })


    }


    // Write data to text boxes
    private fun resetTextValues(
        coinName1: String, coinAmount1: String, coinName2: String, coinAmount2: String,
        coinName1TextView: TextView, coinName2TextView: TextView,
        coinAmount1TextView: TextView, coinAmount2TextView: TextView
    ) {
        coinName1TextView.text = coinName1
        coinName2TextView.text = coinName2
        coinAmount1TextView.text = coinAmount1
        coinAmount2TextView.text = coinAmount2
    }


    // update the exchange rate box according to coin amounts
    private fun updateExchangeRate(
        view: View, coinAmount1TextView: TextView,
        coinAmount2TextView: TextView, originalRate: Float?
    ) {

        val rateTextView: TextView = view.findViewById(R.id.exchange_rate2) as TextView
        if (coinAmount1TextView.text.toString() == "" || coinAmount2TextView.text.toString() == "") {
            rateTextView.text = "N/A"
            return
        }

        var newRate: Float? = originalRate
        if (coinAmount1TextView.text.toString() !== "null" && coinAmount2TextView.text.toString() !== "null") {

            newRate = coinAmount2TextView.text.toString().toFloat() /
                    coinAmount1TextView.text.toString().toFloat()
        }

        Log.d("Ohad", "newRate: " + newRate.toString())
        var newText = "%.3f".format(newRate)
        if (newRate != originalRate && originalRate != null)
            newText = "Offer rate:  1 : %.3f      Suggested rate:  1 : %.3f".format(newRate, originalRate)

        rateTextView.text = newText

    }
    private fun bindImage(imgView: ImageView, imgUrl: String?) {
        imgUrl?.let {
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            Glide.with(imgView.context)
                .load(imgUri)
                .into(imgView)
        }
    }


}
