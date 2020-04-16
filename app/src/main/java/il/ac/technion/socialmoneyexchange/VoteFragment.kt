package il.ac.technion.socialmoneyexchange

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_vote.view.*


class VoteFragment : Fragment() {

    lateinit var offerId: String
    lateinit var userID1: String
    lateinit var userID2: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            offerId = savedInstanceState.getString("offerId").toString()
            userID1 = savedInstanceState.getString("userID1").toString()
            userID2 = savedInstanceState.getString("userID2").toString()
        } else if (arguments != null) {
            offerId = arguments!!.getString("offerId").toString()
            userID1 = arguments!!.getString("userID1").toString()
            userID2 = arguments!!.getString("userID2").toString()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val database = FirebaseDatabase.getInstance()
        val view = inflater.inflate(R.layout.fragment_vote, container, false)
        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val myUserId = currentFirebaseUser!!.uid
        val otherUserId: String
        otherUserId = if (myUserId == userID1)
            userID2
        else
            userID1
        var votesAmount = 0.0
        var avgVote = 0.0
        var fullName = ""
        val otherUserRef: DatabaseReference = database.getReference("users").child(otherUserId)
        otherUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            // This method is triggered once when the listener is attached
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val amount = dataSnapshot.child("votesAmount").getValue(String::class.java)
                if (!amount.isNullOrEmpty())
                    votesAmount = amount.toDouble()

                val avg = dataSnapshot.child("rate").getValue(String::class.java)
                if (!avg.isNullOrEmpty())
                    avgVote = avg.toDouble()


            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
        var imgUrl = ""
        val myUserRef: DatabaseReference = database.getReference("users").child(myUserId)
        myUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            // This method is triggered once when the listener is attached
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val firstName = dataSnapshot.child("firstName").getValue(String::class.java)
                val lastName = dataSnapshot.child("lastName").getValue(String::class.java)
                 fullName = firstName?.capitalize() + " " + lastName?.capitalize()
                imgUrl = dataSnapshot.child("imgUrl").getValue(String::class.java).toString()


            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })



        view.submit_button.setOnClickListener {

            if(!view.review_text.text.isNullOrEmpty()&&view.review_text.text.toString()!="") {
                database.getReference("users").child(otherUserId).child("reviews")
                    .child(java.lang.String.valueOf(System.currentTimeMillis()))
                    .setValue(Review(view.review_text.text.toString(),imgUrl,fullName))
            }
            val newRate = (avgVote * votesAmount + view.rating_bar.rating.toDouble())/(votesAmount+1)
            database.getReference("users").child(otherUserId).child("rate").setValue(newRate.toString())
            database.getReference("users").child(otherUserId).child("votesAmount").setValue((votesAmount+1).toString())
            database.getReference("offers").child(offerId).child("vote").child(myUserId).setValue("true")
            findNavController().navigate(R.id.offersFragment)

        }
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.offersFragment)

                }
            }
            )
        return view
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("offerId", offerId)
        outState.putString("userID1", userID1)
        outState.putString("userID2", userID2)
    }
}
