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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import il.ac.technion.socialmoneyexchange.databinding.FragmentMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_main.view.*


interface TransactionsIDCallback {
    fun onCallback(transactionsID: MutableList<String>)
}

interface TransactionDataCallback {
    fun onCallback(transactionsData: MutableList<TransactionRequest>)
}

class MainFragment : Fragment() {


    // Get a reference to the ViewModel scoped to this Fragment
//    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: FragmentMainBinding
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var database: FirebaseDatabase
    private lateinit var transactionList: MutableList<TransactionRequest>
    private lateinit var adapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

        database = FirebaseDatabase.getInstance()

//        transactionList = mutableListOf<TransactionRequest>()

        // hide the recycler view
        binding.transactionsRecyclerView.visibility = View.GONE

        // init the RecyclerView Adapter
        linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        binding.transactionsRecyclerView.layoutManager = linearLayoutManager
        adapter = TransactionAdapter(requireContext())
        binding.transactionsRecyclerView.adapter = adapter
        binding.transactionsRecyclerView.addItemDecoration(
            DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        )
        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.removeAt(viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.transactionsRecyclerView)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        // if no user is connect
        while (currentFirebaseUser == null) {
            val action = MainFragmentDirections.actionMainFragmentToLogoFragment()
            findNavController().navigate(action)
            return
        }


        // if user is connected but not in database
        val userId = currentFirebaseUser.uid
        val userRef: DatabaseReference = database.getReference("users").child(userId)
        val transactionRef: DatabaseReference = database.getReference("transactionRequests")

        // Bind new request button
        binding.requestButton.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToRequestFragment()
            findNavController().navigate(action)
        }


        // Get IDs of transactions and then data, finally load the data into adapter
        getTranscationsIDFromDB(userRef, transactionRef)

        // Write push notification token to database
        writeTokenToDatabase(userRef)

    }

    private fun writeTokenToDatabase(userRef: DatabaseReference) {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("Ohad", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token
                // Log and toast
                Log.d("Ohad", "device push notification token: $token")
                userRef.child("token").setValue(token)
            })
    }


    private fun loadTransactionsDataFromDB(ids: MutableList<String>, transactionRef: DatabaseReference){
        //dynamically load real transactions history
        val transactionsList = mutableListOf<TransactionRequest>()
        val transactionsIDs = mutableListOf<String>()

        transactionRef.orderByChild("timeStamp").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (transactionSnapshot in dataSnapshot.children) {
                    if (ids.contains(transactionSnapshot.key)) {
                        val transaction: TransactionRequest? = transactionSnapshot.getValue(TransactionRequest::class.java)
                        val transactionsKey = transactionSnapshot.key
//                        Log.d("Ohad", "Loaded transaction: $transaction")
                        if (transaction != null && transactionsKey != null) {
                            transactionsList.add(transaction)
                            transactionsIDs.add(transactionsKey)
                        }
                    }
                }
                // Update view using adapter
                adapter.transactionList = transactionsList
                adapter.transactionIDs = transactionsIDs

                // hide progress bar and show transactions
                binding.transactionsRecyclerView.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Ohad", "Failed to read value.", error.toException())
            }
        })
    }


    private fun getTranscationsIDFromDB(userRef: DatabaseReference, transactionRef: DatabaseReference){
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            // This method is triggered once when the listener is attached
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // first, check if user first name exists
                val firstName = dataSnapshot.child("firstName").getValue(String::class.java)
                if (firstName == null) {
                    val action = MainFragmentDirections.actionMainFragmentToNewUserFragment()
                    findNavController().navigate(action)
                    //                    return // maybe the return here is needed?
                }

                // Load transactions requests IDs from DB
                val ids = mutableListOf<String>()
                val transactions = dataSnapshot.child("transactionRequests").children
                transactions.forEach {
                    ids += it.getValue(String::class.java).toString()
                }

                Log.d("Ohad", "Found IDs: $ids")
                loadTransactionsDataFromDB(ids, transactionRef)

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w("Ohad", "Failed to read value.", error.toException())
            }
        })
    }

}


