package il.ac.technion.socialmoneyexchange

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import il.ac.technion.socialmoneyexchange.databinding.FragmentOffersBinding


class OffersFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentOffersBinding
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: OfferAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_offers, container, false)

        database = FirebaseDatabase.getInstance()

        // init the RecyclerView Adapter
        val context = requireContext()
        linearLayoutManager = LinearLayoutManager(context)
        binding.offersRecyclerView.layoutManager = linearLayoutManager
        adapter = OfferAdapter(context)
        binding.offersRecyclerView.adapter = adapter
        binding.offersRecyclerView.addItemDecoration(
            DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        // if no user is connect
        while (currentFirebaseUser == null) { return }

        // if user is connected
        val userId = currentFirebaseUser.uid
        val userRef: DatabaseReference = database.getReference("users").child(userId)
        val offersRef: DatabaseReference = database.getReference("offers")

        // Get IDs of offers and then data, finally load the data into adapter
        getOffersIDFromDB(userRef, offersRef)
    }


    private fun loadOffersDataFromDB(ids: MutableList<String>, offersRef: DatabaseReference){
        //dynamically load real transactions history
        val offerList = mutableListOf<Offer>()
        val offerIDs = mutableListOf<String>()
        offersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (offerSnapshot in dataSnapshot.children) {
                    if (ids.contains(offerSnapshot.key)) {
                        val offer: Offer? = offerSnapshot.getValue(Offer::class.java)
                        val offerKey = offerSnapshot.key
                        Log.d("Ohad", "Loaded transaction: $offer")

                        if (offer != null && offerKey != null) {
                            offerList.add(offer)
                            offerIDs.add(offerKey)
                        }
                    }
                }
                // Update view using adapter
                adapter.offersList = offerList
                adapter.offerIDs = offerIDs
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Ohad", "Failed to read value.", error.toException())
            }
        })
    }


    private fun getOffersIDFromDB(userRef: DatabaseReference, offersRef: DatabaseReference){
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            // This method is triggered once when the listener is attached
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Load transactions requests IDs from DB
                val ids = mutableListOf<String>()
                val offers = dataSnapshot.child("offers").children
                offers.forEach {
                    ids += it.getValue(String::class.java).toString()
                }

                Log.d("Ohad", "Found IDs: $ids")
                loadOffersDataFromDB(ids, offersRef)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Ohad", "Failed to read value.", error.toException())
            }
        })
    }


}
