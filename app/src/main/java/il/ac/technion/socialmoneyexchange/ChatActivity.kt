package il.ac.technion.socialmoneyexchange

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_chat.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ChatActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var offerId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        offerId = savedInstanceState?.getString("offerId")?.toString() ?: intent.getStringExtra("offerId")
        val otherUser = savedInstanceState?.getString("otherUser")?.toString() ?: intent.getStringExtra("otherUser")
        setTitle("Chat with $otherUser");
        setContentView(R.layout.activity_chat)
        database = FirebaseDatabase.getInstance()
        setupSendButton()
        createFirebaseListener()
    }

    /**
     * OnClick action for the send button
     */
    private fun setupSendButton() {

        mainActivitySendButton.setOnClickListener {
            if (!mainActivityEditText.text.toString().isEmpty()){
                sendData()
            }else{
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Send data to firebase
     */
    private fun sendData(){
        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val userId = currentFirebaseUser!!.uid

        database.getReference("offers").child(offerId).
            child("messages")?.
            child(java.lang.String.valueOf(System.currentTimeMillis()))?.
            setValue(Message(mainActivityEditText.text.toString(),userId))

        //clear the text
        mainActivityEditText.setText("")
    }
    private fun createFirebaseListener(){
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var firstId = ""
                val toReturn: ArrayList<Message> = ArrayList();

                for(data in dataSnapshot.children){
                    val messageData = data.getValue<Message>(Message::class.java)

                    //unwrap
                    val message = messageData?.let { it } ?: continue
                    if(firstId == ""){
                        firstId = message.userID.toString()
                    }
                    if(message.userID.toString().equals(firstId))
                        message.setColorChoose()
                    toReturn.add(message)
                }

                //sort so newest at bottom
                toReturn.sortBy { message ->
                    message.timestamp
                }

                setupAdapter(toReturn)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                //log error
            }
        }
        database.getReference("offers").child(offerId).child("messages")
            .addValueEventListener(postListener)
    }
    private fun setupAdapter(data: ArrayList<Message>){
        val linearLayoutManager = LinearLayoutManager(this)
        mainActivityRecyclerView.layoutManager = linearLayoutManager
        mainActivityRecyclerView.adapter = MessageAdapter(data) {
//            Toast.makeText(this, "${it.text} clicked", Toast.LENGTH_SHORT).show()
        }

        //scroll to bottom
        mainActivityRecyclerView.scrollToPosition(data.size - 1)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("offerId",offerId)

    }
}
