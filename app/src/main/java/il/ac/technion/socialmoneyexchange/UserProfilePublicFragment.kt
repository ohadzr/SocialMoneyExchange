package il.ac.technion.socialmoneyexchange
import android.content.Intent
import com.bumptech.glide.Glide
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import il.ac.technion.socialmoneyexchange.databinding.FragmentUserProfilePublicBinding


class UserProfilePublicFragment : Fragment() {
    lateinit var offerId: String
    lateinit var userID1: String
    lateinit var userID2: String
    lateinit var coinAmount1: String
    lateinit var coinAmount2: String
    lateinit var coinName1: String
    lateinit var coinName2: String
    lateinit var lastUpdater: String
    lateinit var status: String
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: ReviewAdapter
    private lateinit var binding: FragmentUserProfilePublicBinding


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
        database = FirebaseDatabase.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_profile_public, container, false)
//        val view = inflater.inflate(R.layout.fragment_user_profile_public, container, false)
//        val reviewRecyclerView = view.reviewRecyclerView

        // Creates a vertical Layout Manager
        linearLayoutManager = LinearLayoutManager(requireContext())
//        reviewRecyclerView.layoutManager = linearLayoutManager
        val userId : String

        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        userId = if(::userID1.isInitialized) {
            if(currentFirebaseUser!!.uid==userID1)
                userID2
            else
                userID1
        } else
            currentFirebaseUser!!.uid
        val userRef : DatabaseReference = database.getReference("users").child(userId)
        // Read from the database
        binding.reviewRecyclerView.visibility = View.GONE


        // hide the recycler view
        binding.reviewRecyclerView.visibility = View.GONE

        // init the RecyclerView Adapter
        val context = requireContext()
        linearLayoutManager = LinearLayoutManager(context)
        binding.reviewRecyclerView.layoutManager = linearLayoutManager


        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun
                    onDataChange(dataSnapshot: DataSnapshot) {
                val firstName = dataSnapshot.child("firstName").getValue(String::class.java)
                val lastName = dataSnapshot.child("lastName").getValue(String::class.java)
                val fullName = firstName?.capitalize() + " " + lastName?.capitalize()
                val url = dataSnapshot.child("imgUrl").getValue(String::class.java)
                val userRate = dataSnapshot.child("rate").getValue(String::class.java)
                if(!userRate.isNullOrEmpty())
                    binding.ratingBar.rating = userRate.toFloat()
                bindImage(binding.userImgPublic,url)
                binding.nameText.text = fullName


                val toReturn: ArrayList<Review> = ArrayList();

                for(data in dataSnapshot.child("reviews").children){
                    val reviewData = data.getValue<Review>(Review::class.java)

                    //unwrap
                    val review = reviewData?.let { it } ?: continue
                    toReturn.add(review)
                }

                //sort so newest at bottom
                toReturn.sortBy { review ->
                    review.timestamp
                }
                adapter = ReviewAdapter(toReturn){}
                binding.reviewRecyclerView.adapter = adapter
                binding.reviewRecyclerView.addItemDecoration(
                    DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
                )
                binding.reviewRecyclerView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                if(toReturn.size==0) {
                    binding.reviewsText.text = "User has no reviews"

                }


            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })





        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if(coinAmount1.isNullOrEmpty())
                        findNavController().navigate(R.id.mainFragment)
                    else{
                        val intent = Intent(context, MainActivity::class.java)
                        intent.putExtra("fromOffer", "true")
                        intent.putExtra("offerId", offerId)
                        intent.putExtra("userID1", userID1)
                        intent.putExtra("userID2", userID2)
                        intent.putExtra("coinAmount1",coinAmount1)
                        intent.putExtra("coinName1", coinName1)
                        intent.putExtra("coinName2", coinName2)
                        intent.putExtra("lastUpdater", lastUpdater)
                        intent.putExtra("status", status)
                        intent.putExtra("coinAmount2", coinAmount2)
                        startActivity(intent)
                    }

                }
            }
            )

        return binding.root
    }


    private fun bindImage(imgView: ImageView, imgUrl: String?) {
        imgUrl?.let {
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            Glide.with(imgView.context)
                .load(imgUri)
                .into(imgView)
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(::userID1.isInitialized) {
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
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}
