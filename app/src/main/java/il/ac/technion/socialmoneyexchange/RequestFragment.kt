package il.ac.technion.socialmoneyexchange

import android.annotation.SuppressLint
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
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

//import rx.android.schedulers.AndroidSchedulers

class RequestFragment : Fragment() {
    private val MAX_CURRENECIES = 4F
    private lateinit var database: FirebaseDatabase
    private val MAX_MONEY_DIGITS = 6
    lateinit var inputText : TextInputEditText
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        val coinList = ArrayList<String>()
        coinList.add("Shekel")
        coinList.add("Dollar")
        coinList.add("Euro")
        coinList.add("four")
        coinList.add("five")
        coinList.add("one")


        //internet

//        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
//            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//            .baseUrl("https://api.exchangeratesapi.io/").build()
//        val ratesApi = retrofit.create(RatesAPI::class.java)
//        var response = ratesApi.getRates()
//        response.observeOn(IoScheduler()).subscribeOn(AndroidSchedulers.mainThread()).subscribe(
//
//        )

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
        inputText.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                for(i in 0 until myAddedCoins.toInt()){
                    inputTextList[i].text = inputText.text
                }
                true
            } else {
                false
            }
        }
        layout.addView(inputText)

        val myButtonAdd = MaterialButton(requireContext())
        myButtonAdd.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val addParam = myButtonAdd.layoutParams as RelativeLayout.LayoutParams
        val addEdgeDist = dpToPx(requireContext(),12).toInt()
        val addTopDist = dpToPx(requireContext(),332).toInt()
        addParam.topMargin = addTopDist
        addParam.marginStart = addEdgeDist
        myButtonAdd.layoutParams = addParam
        myButtonAdd.text = "Add"
        layout.addView(myButtonAdd)

        val myButtonRemove = MaterialButton(requireContext())
        myButtonRemove.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val removeParam = myButtonRemove.layoutParams as RelativeLayout.LayoutParams
        val removeEdgeDist = dpToPx(requireContext(),250).toInt()
        val removeTopDist = dpToPx(requireContext(),332).toInt()
//        myButtonRemove.setBackgroundResource(R.color.colorRed)
        removeParam.topMargin = removeTopDist
        removeParam.marginStart = removeEdgeDist
        myButtonRemove.layoutParams = removeParam
        myButtonRemove.text = "Remove"
        addButtonClicked(spinnerList,inputTextList,myAddedCoins,coinList,layout,myButtonRemove,requestedCurrencies)
        myAddedCoins++
        myButtonAdd.setOnClickListener {
            val added = addButtonClicked(spinnerList,inputTextList,myAddedCoins,coinList,layout,myButtonRemove,requestedCurrencies)
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
                val randomId = randomAlphaNumericString(32)
                val transactionRef: DatabaseReference =
                    database.getReference("transactionRequests").child(randomId)
                transactionRef.setValue(newTansactionRequest)
                Toast.makeText(requireContext(),"Successfully submitted your request", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        return view
    }
    @SuppressLint("RestrictedApi")
    private fun addButtonClicked(spinnerList:ArrayList<SearchableSpinner>, inputTextList:ArrayList<MaterialTextView>,
                                 addedCoins:Float, coinList:ArrayList<String>, layout:RelativeLayout, removeButton:Button, requestedCurrencies:ArrayList<String>):Boolean{
        if(addedCoins < MAX_CURRENECIES) {
            var tempCoins = addedCoins

            val amountText = MaterialTextView(requireContext())
            amountText.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
            val amountTextParam = amountText.layoutParams as RelativeLayout.LayoutParams
            val amountTextEdgeDist = dpToPx(requireContext(),130).toInt()
            val amountTextTopDist = dpToPx(requireContext(),384+24*tempCoins.toInt()).toInt()
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
            val spinnerTopDist = dpToPx(requireContext(),370+24*tempCoins.toInt()).toInt()
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

                    }
                })

            spinnerList.add(spinner)
            layout.addView(spinner)
            inputTextList.add(amountText)
            layout.addView(amountText)
            for(i in 0 until addedCoins.toInt()+1){
                inputTextList[i].text = inputText.text
            }
            return true
        }
        else{
            Toast.makeText(requireContext(), "Reached the limit", Toast.LENGTH_SHORT).show()
            return false
        }
    }
    fun randomAlphaNumericString(desiredStrLength: Int): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return (1..desiredStrLength)
            .map{ kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

}

