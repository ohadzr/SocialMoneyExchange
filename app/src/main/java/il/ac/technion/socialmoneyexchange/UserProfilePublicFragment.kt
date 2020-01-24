package il.ac.technion.socialmoneyexchange

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_user_profile_public.view.*

class UserProfilePublicFragment : Fragment() {

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_user_profile_public, container, false)
        val reviewRecyclerView = view.reviewRecyclerView

        // Creates a vertical Layout Manager
        linearLayoutManager = LinearLayoutManager(requireContext())
        reviewRecyclerView.layoutManager = linearLayoutManager

        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val userId = currentFirebaseUser!!.uid
        val userRef : DatabaseReference = database.getReference("users").child(userId)

        // Read from the database
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is triggered once when the listener is attached and again
                // every time the data, including children, changes.
                // value can be String, Long, Double, Boolean, Map<String, Object>, List<Object>
                val firstName = dataSnapshot.child("firstName").getValue(String::class.java)
                val lastName = dataSnapshot.child("lastName").getValue(String::class.java)
                val fullName = firstName?.capitalize() + " " + lastName?.capitalize()
                Log.d("ohad", "name is: $fullName")

                val nameTextView: TextView = view.findViewById(R.id.name_text) as TextView
                nameTextView.text = fullName
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("ohad", "Failed to read value.", error.toException())
            }
        })

        //Load review into ArrayList
        // TODO: dynamically load real reviews
        val reviewList = ArrayList<String>()
        reviewList.add("review1")
        reviewList.add("review2")
        reviewList.add("review3")
        reviewList.add("review4")
        reviewList.add("review5")
        reviewList.add("review6")

        // Access the RecyclerView Adapter and load the data into it
        reviewRecyclerView.adapter = ReviewsAdapter(reviewList, requireContext())

        return view
    }

}
