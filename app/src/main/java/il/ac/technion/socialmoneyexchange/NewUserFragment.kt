package il.ac.technion.socialmoneyexchange

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import il.ac.technion.socialmoneyexchange.databinding.FragmentNewUserBinding


class NewUserFragment : Fragment() {

    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = FirebaseDatabase.getInstance()

    }

    private lateinit var binding: FragmentNewUserBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_user, container, false)

        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()

        binding.nextButton.setOnClickListener{

            hideKeyboard()

            val firstName = binding.firstNameText.text.toString()
            val lastName = binding.lastNameText.text.toString()
            if (firstName == "" || lastName == "") {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
            else {

                val userInfo = User(firstName=firstName, lastName=lastName)
                val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                val userId = currentFirebaseUser!!.uid
                val userRef: DatabaseReference = database.getReference("users").child(userId)
                userRef.setValue(userInfo)
                findNavController().popBackStack()

            }
        }
    }

}
