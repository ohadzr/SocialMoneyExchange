package il.ac.technion.socialmoneyexchange

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class OfferFragment : Fragment() {

    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_offer, container, false)

        //TODO: uncomment and read data from database
        database = FirebaseDatabase.getInstance()
        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val userId = currentFirebaseUser!!.uid

        val offerID = "randomOfferID"
        // TODO: load real data from database using offerID
        val coinName1 = "CAD"
        val coinAmount1 = 100
        val coinName2 = "USD"
        val coinAmount2 = 300
        // TODO: get rate from API
        val originalRate = coinAmount1.toFloat() / coinAmount2.toFloat()

        val offersRef: DatabaseReference = database.getReference("offers").child(offerID)
        val userOfferRef: DatabaseReference = database.getReference("users").child(userId).child("offers")
//        offersRef.setValue(Offer("ohad",coinName1, coinAmount1.toFloat(), "tamir",coinName2, coinAmount2.toFloat(),"active"))
//        userOfferRef.setValue(offerID)

        val coinName1TextView: TextView = view.findViewById(R.id.coin_name_text) as TextView
        val coinName2TextView: TextView = view.findViewById(R.id.coin_name_text2) as TextView
        val coinAmount1TextView: TextView = view.findViewById(R.id.coin_value) as TextView
        val coinAmount2TextView: TextView = view.findViewById(R.id.coin_value2) as TextView

        val resetButton = view.findViewById(R.id.reset_button) as Button
        val saveButton = view.findViewById(R.id.accept_button) as Button
        val declineButton = view.findViewById(R.id.decline_button) as Button

        // Load original data to text boxes for the first time
        resetTextValues(view, coinName1, coinAmount1, coinName2, coinAmount2,
            coinName1TextView, coinName2TextView, coinAmount1TextView, coinAmount2TextView)

        // Set reset button to load original data
        resetButton.setOnClickListener {
            resetTextValues(view, coinName1, coinAmount1, coinName2, coinAmount2,
                coinName1TextView, coinName2TextView, coinAmount1TextView, coinAmount2TextView)
        }

        // Set save button to accept transaction and save it to DB
        saveButton.setOnClickListener {
            saveOffer(view, coinName1, coinAmount1TextView,   coinName2, coinAmount2TextView)
        }

        // Set decline button to cancel transaction and save it to DB
        declineButton.setOnClickListener {
            declineOffer(view, coinName1, coinAmount1TextView, coinName2, coinAmount2TextView)
        }


        updateExchangeRate(view, coinAmount1TextView, coinAmount2TextView, originalRate)

        // Update exchange rate if text is changed
        coinAmount1TextView.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                updateExchangeRate(view, coinAmount1TextView, coinAmount2TextView, originalRate)
            }
        })

        // Update exchange rate if text is changed
        coinAmount2TextView.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                updateExchangeRate(view, coinAmount1TextView, coinAmount2TextView, originalRate)
            }
        })




        return view
    }


//    private fun saveOffer(view: View?, coinName1: String, coinAmount1TextView: TextView, coinName2: String, coinAmount2TextView: TextView) {
//
//    }
//
//    private fun declineOffer(view: View?, coinName1: String, coinAmount1TextView: TextView, coinName2: String, coinAmount2TextView: TextView) {
//
//    }

    // Write data to text boxes
    private fun resetTextValues(
        view: View?,
        coinName1: String, coinAmount1: Int, coinName2: String, coinAmount2: Int,
        coinName1TextView: TextView, coinName2TextView: TextView,
        coinAmount1TextView: TextView, coinAmount2TextView: TextView
    ) {
        coinName1TextView.text = coinName1
        coinName2TextView.text = coinName2
        coinAmount1TextView.text = coinAmount1.toString()
        coinAmount2TextView.text = coinAmount2.toString()
    }


    // update the exchange rate box according to coin amounts
    private fun updateExchangeRate(view : View, coinAmount1TextView: TextView,
                                   coinAmount2TextView: TextView, originalRate: Float) {
        val rateTextView: TextView = view.findViewById(R.id.exchange_rate2) as TextView
        if (coinAmount1TextView.text.toString() == "" || coinAmount2TextView.text.toString() == ""){
            rateTextView.text = "N/A"
            return
        }
        val new_rate = coinAmount1TextView.text.toString().toFloat() /
                       coinAmount2TextView.text.toString().toFloat()
        var newText = "%.3f".format(new_rate)
        if (new_rate != originalRate)
            newText = "Offer rate: %.3f    Suggested rate: %.3f".format(new_rate, originalRate)

        rateTextView.text = newText

    }

}
