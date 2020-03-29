package il.ac.technion.socialmoneyexchange
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
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_logo.*
import kotlinx.android.synthetic.main.fragment_user_profile_public.view.*
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import java.net.URLConnection


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
        reviewRecyclerView.layoutManager = linearLayoutManager as RecyclerView.LayoutManager?

        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val userId = currentFirebaseUser!!.uid
        val userRef : DatabaseReference = database.getReference("users").child(userId)
        var photoUrl : Uri?
        val userInstance = FirebaseAuth.getInstance().currentUser
        if (userInstance != null) {
            bindImage(view.user_img_public,userInstance.photoUrl.toString())
        }
        // Read from the database
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun
                    onDataChange(dataSnapshot: DataSnapshot) {
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

//        //Load review into ArrayList
//        // TODO: dynamically load real reviews
//        val reviewList = ArrayList<ArrayList<String>>()
//        val item = ArrayList<String>()
//        item.add("USD")
//        item.add("100")
//        item.add("EUR")
//        item.add("200")
//
//        for (i in 0..100)
//            reviewList.add(item)
//
//        // Access the RecyclerView Adapter and load the data into it
//        reviewRecyclerView.adapter = ReviewsAdapter(reviewList, requireContext())

        return view
    }
    fun bindImage(imgView: ImageView, imgUrl: String?) {
        imgUrl?.let {
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            Glide.with(imgView.context)
                .load(imgUri)
                .into(imgView)
        }
    }

}
