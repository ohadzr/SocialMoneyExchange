package il.ac.technion.socialmoneyexchange

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.internal.ViewUtils.dpToPx
import kotlinx.android.synthetic.main.fragment_request.view.*
import android.text.InputFilter
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import il.ac.technion.socialmoneyexchange.GlobalVariable.apiData
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat

import java.util.*
import kotlin.collections.ArrayList


@Suppress("DEPRECATION")
class RequestFragment : Fragment() {
    private val MAX_CURRENECIES = 4F
    private lateinit var database: FirebaseDatabase
    private var message = ""
    private val MAX_MONEY_DIGITS = 6
    lateinit var inputText : TextInputEditText
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        message = savedInstanceState?.getString("Message1").toString()
        if(arguments!=null)
            message = arguments!!.getString("Message1").toString()
    }
    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment




        val coinList = ArrayList<String>()
        var myAddedCoins = 0F
        var myCurrency = ""
        val view = inflater.inflate(R.layout.fragment_request, container, false)
        val layout = view.request_layout as RelativeLayout
        val spinnerList = ArrayList<SearchableSpinner>()
        val inputTextList = ArrayList<MaterialTextView>()
        val requestedCurrencies = ArrayList<String>()
        for (i in 0..MAX_CURRENECIES.toInt()) {
            val currency = ""
            requestedCurrencies.add(currency)
        }
        coinList.addAll(apiData.rates.keys.toList())
        //first spinner - giving currency
        val spinner = SearchableSpinner(requireContext())
        spinner.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        spinner.adapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1,
            coinList)
        spinner.setTitle("Select a currency")
        val spinnerParam = spinner.layoutParams as RelativeLayout.LayoutParams
        val spinnerEdgeDist = dpToPx(requireContext(),0).toInt()
        val spinnerTopDist = dpToPx(requireContext(),70).toInt()
        spinnerParam.topMargin = spinnerTopDist
        spinnerParam.marginStart = spinnerEdgeDist
        spinner.layoutParams = spinnerParam
        spinner.onItemSelectedListener =
            (object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    myCurrency = coinList[position]
                    if(!inputText.text.isNullOrEmpty())
                        updateAmount(inputText.text.toString().toInt(), myCurrency, requestedCurrencies, inputTextList,myAddedCoins)

                }
            })
        layout.addView(spinner)

        //input text for giving
        inputText = TextInputEditText(requireContext())
        inputText.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        val inputTextParam = inputText.layoutParams as RelativeLayout.LayoutParams
        val inputTextEdgeDist = dpToPx(requireContext(),130).toInt()
        val inputTextTopDist = dpToPx(requireContext(),70).toInt()
        inputTextParam.topMargin = inputTextTopDist
        inputTextParam.marginStart = inputTextEdgeDist
        inputText.layoutParams = inputTextParam
        inputText.hint="Insert amount"
        inputText.inputType = InputType.TYPE_CLASS_NUMBER
        inputText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(MAX_MONEY_DIGITS))
        inputText.setOnClickListener{v ->
            if(!inputText.text.isNullOrEmpty())
                updateAmount(inputText.text.toString().toInt(), myCurrency, requestedCurrencies, inputTextList,myAddedCoins)

        }

        layout.addView(inputText)

        val myButtonAdd = MaterialButton(requireContext())
        myButtonAdd.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val addParam = myButtonAdd.layoutParams as RelativeLayout.LayoutParams
        val addEdgeDist = dpToPx(requireContext(),12).toInt()
        val addTopDist = dpToPx(requireContext(),222).toInt()
        addParam.topMargin = addTopDist
        addParam.marginStart = addEdgeDist
        myButtonAdd.layoutParams = addParam
        myButtonAdd.text = "Add"
        layout.addView(myButtonAdd)

        val myButtonRemove = MaterialButton(requireContext())
        myButtonRemove.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val removeParam = myButtonRemove.layoutParams as RelativeLayout.LayoutParams
        val removeEdgeDist = dpToPx(requireContext(),250).toInt()
        val removeTopDist = dpToPx(requireContext(),222).toInt()
        removeParam.topMargin = removeTopDist
        removeParam.marginStart = removeEdgeDist
        myButtonRemove.layoutParams = removeParam
        myButtonRemove.text = "Remove"
        addButtonClicked(spinnerList,inputTextList,myAddedCoins,coinList,layout,myButtonRemove,requestedCurrencies,myCurrency)
        myAddedCoins++
        myButtonAdd.setOnClickListener {
            if(!inputText.text.isNullOrEmpty())
                updateAmount(inputText.text.toString().toInt(), myCurrency, requestedCurrencies, inputTextList,myAddedCoins)

            val added = addButtonClicked(spinnerList,inputTextList,myAddedCoins,coinList,layout,myButtonRemove,requestedCurrencies,myCurrency)
            if(added)
                myAddedCoins++
        }

        myButtonRemove.setOnClickListener{
            if(myAddedCoins.equals(2F)){
                layout.removeView(myButtonRemove)
            }
            layout.removeView(inputTextList[myAddedCoins.toInt()-1])
            inputTextList.removeAt(myAddedCoins.toInt()-1)
            layout.removeView(spinnerList[myAddedCoins.toInt()-1])
            spinnerList.removeAt(myAddedCoins.toInt()-1)
            myAddedCoins--
        }
        //data base insertion

        database = FirebaseDatabase.getInstance()
        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val userId = currentFirebaseUser!!.uid
        view.limit_distance.setOnClickListener {
            val intent = Intent(context, MapsActivity::class.java)

                intent.putExtra("Message", "Message111")
                startActivity(intent)

            //view.findNavController().navigate(R.id.action_requestFragment_to_locationFragment)
        }

        //Hit SUBMIT
        view.submit_button_request.setOnClickListener {
            var sameCurrency = false
            for (i in 0..MAX_CURRENECIES.toInt()) {//checking if want to exchange same currencies
                if(myCurrency==requestedCurrencies[i])
                    sameCurrency=true
                //TODO don't allow to request same currencies
            }

            if(inputText.text.isNullOrEmpty()||inputText.text.toString()=="0")
                Toast.makeText(requireContext(), "Please insert a requested amount", Toast.LENGTH_SHORT).show()

            else if (sameCurrency)
                Toast.makeText(requireContext(), "Can't exchange same currency", Toast.LENGTH_SHORT).show()
            else {
            val timeStamp = SimpleDateFormat("yyyyMMdd").format(Date())//yyyyMMdd_HHmmss if want more
                val newTansactionRequest = TransactionRequest(
                    userId,
                    myCurrency,
                    inputText.text.toString().toInt(),
                    requestedCurrencies,
                    timeStamp
                )
//                val randomId = randomAlphaNumericString(32)
                val userTransactionRequestsRef: DatabaseReference = database.getReference("users").child(userId).child("transactionRequests").push()
                val transactionKey = userTransactionRequestsRef.key
                if (transactionKey != null){
                    userTransactionRequestsRef.setValue(transactionKey)
                    val transactionRef: DatabaseReference =
                        database.getReference("transactionRequests").child(transactionKey)
                    transactionRef.setValue(newTansactionRequest)
                    Toast.makeText(requireContext(),"Successfully submitted your request", Toast.LENGTH_SHORT).show()
                }
                findNavController().popBackStack()
            }

        }

        return view
    }



    @SuppressLint("RestrictedApi")
    private fun addButtonClicked(spinnerList:ArrayList<SearchableSpinner>, inputTextList:ArrayList<MaterialTextView>,
                                 addedCoins:Float, coinList:ArrayList<String>, layout:RelativeLayout, removeButton:Button, requestedCurrencies:ArrayList<String>,myCurrency:String):Boolean{
        if(addedCoins < MAX_CURRENECIES) {
            var tempCoins = addedCoins

            val amountText = MaterialTextView(requireContext())
            amountText.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
            val amountTextParam = amountText.layoutParams as RelativeLayout.LayoutParams
            val amountTextEdgeDist = dpToPx(requireContext(),130).toInt()
            val amountTextTopDist = dpToPx(requireContext(),274+24*tempCoins.toInt()).toInt()
            amountTextParam.topMargin = amountTextTopDist
            amountTextParam.marginStart = amountTextEdgeDist
            amountText.layoutParams = amountTextParam

            val spinner =
                SearchableSpinner(requireContext())
            spinner.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            spinner.adapter = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                coinList
            )
            spinner.setTitle("Select a currency")
            if (tempCoins.equals(1F)) {
                layout.addView(removeButton)
            }

            val spinnerParam = spinner.layoutParams as RelativeLayout.LayoutParams
            val spinnerEdgeDist = dpToPx(requireContext(),0).toInt()
            val spinnerTopDist = dpToPx(requireContext(),260+24*tempCoins.toInt()).toInt()
            spinnerParam.topMargin = spinnerTopDist
            spinnerParam.marginStart = spinnerEdgeDist
//            spinnerParam.marginEnd = dpToPx(requireContext(),0).toInt()
            spinner.layoutParams = spinnerParam
                        spinner.onItemSelectedListener =
                (object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        val tempString = coinList[position]
                        requestedCurrencies[tempCoins.toInt()]=tempString
                        if(!inputText.text.isNullOrEmpty())
                            updateAmount(inputText.text.toString().toInt(), myCurrency, requestedCurrencies, inputTextList,addedCoins+1)


                    }
                })

            spinnerList.add(spinner)
            layout.addView(spinner)
            inputTextList.add(amountText)
            layout.addView(amountText)
//            for(i in 0 until addedCoins.toInt()+1){
//                inputTextList[i].text = inputText.text
//            }
            return true
        }
        else{
            Toast.makeText(requireContext(), "Reached the limit", Toast.LENGTH_SHORT).show()
            return false
        }
    }
    private fun randomAlphaNumericString(desiredStrLength: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return (1..desiredStrLength)
            .map{ kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    fun updateAmount(requestedAmount:Int, myCurrency:String, requestedCurrencies: ArrayList<String>, inputTextList: ArrayList<MaterialTextView>,addedCoins: Float){
        var myRate: Double
        if (apiData.rates[myCurrency] != null){
            myRate =
                apiData.rates[myCurrency]!!
            for(i in 0 until addedCoins.toInt()){
                val requestedCurrencyAmount = (apiData.rates[requestedCurrencies[i]]!!/myRate)*requestedAmount.toDouble()
                inputTextList[i].text = requestedCurrencyAmount.toString().format("%.3f")// TODO fix it
            }
        }
        

    }
}

