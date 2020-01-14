package il.ac.technion.socialmoneyexchange

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_user_profile_public.*
import kotlinx.android.synthetic.main.fragment_user_profile_public.view.*

class UserProfilePublicFragment : Fragment() {

    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
