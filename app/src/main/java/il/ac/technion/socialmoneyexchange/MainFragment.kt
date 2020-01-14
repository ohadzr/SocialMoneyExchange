
package il.ac.technion.socialmoneyexchange

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import il.ac.technion.socialmoneyexchange.databinding.FragmentMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class MainFragment : Fragment() {


    // Get a reference to the ViewModel scoped to this Fragment
//    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: FragmentMainBinding
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        observeAuthenticationState()

        binding.requestButton.setOnClickListener{
            val action = MainFragmentDirections.actionMainFragmentToRequestFragment()
            findNavController().navigate(action)
        }
//        binding.authButton.setOnClickListener { launchSignInFlow() }
//        binding.settingsBtn.setOnClickListener {
//            val action = MainFragmentDirections.actionMainFragmentToSettingsFragment()
//            findNavController().navigate(action)
//        }

        val reviewRecyclerView = binding.reviewRecyclerView
        linearLayoutManager = LinearLayoutManager(requireContext())
        reviewRecyclerView.layoutManager = linearLayoutManager

        //Load review into ArrayList
        // TODO: dynamically load real transactions history
        val reviewList = ArrayList<String>()
        reviewList.add("transactions1")
        reviewList.add("transactions2")
        reviewList.add("transactions3")
        reviewList.add("transactions4")
        reviewList.add("transactions5")
        reviewList.add("transactions6")

        // Access the RecyclerView Adapter and load the data into it
        reviewRecyclerView.adapter = ReviewsAdapter(reviewList, requireContext())
    }


//    /**
//     * Observes the authentication state and changes the UI accordingly.
//     * If there is a logged in user: (1) show a logout item in menu and (2) display their name.
//     * If there is no logged in user: show a login item in menu
//     */
//    private fun observeAuthenticationState() {
//        val factToDisplay = viewModel.getFactToDisplay(requireContext())
//
//        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
//            // in LoginViewModel and change the UI accordingly.
//            when (authenticationState) {
//                // you can customize the welcome message they see by
//                // utilizing the getFactWithPersonalization() function provided
//                LoginViewModel.AuthenticationState.AUTHENTICATED -> {
//                    binding.welcomeText.text = getFactWithPersonalization(factToDisplay)
//                    binding.authButton.text = getString(R.string.logout_button_text)
//                    binding.authButton.setOnClickListener {
//                        AuthUI.getInstance().signOut(requireContext())
//                    }
//                }
//                else -> {
//                    // auth_button should display Login and
//                    // launch the sign in screen when clicked.
//                    binding.welcomeText.text = factToDisplay
//
//                    binding.authButton.text = getString(R.string.login_button_text)
//                    binding.authButton.setOnClickListener {
//                        launchSignInFlow()
//                    }
//                }
//            }
//        })
//    }

//    private fun getFactWithPersonalization(fact: String): String {
//        return String.format(
//            resources.getString(
//                R.string.welcome_message_authed,
//                FirebaseAuth.getInstance().currentUser?.displayName,
//                Character.toLowerCase(fact[0]) + fact.substring(1)
//            )
//        )
//    }


}