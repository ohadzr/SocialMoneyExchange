package il.ac.technion.socialmoneyexchange

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
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
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.GsonBuilder
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import okhttp3.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATION")
class RequestFragment : Fragment() {

    private val MAX_CURRENECIES = 4F
    private lateinit var database: FirebaseDatabase
    private val MAX_MONEY_DIGITS = 6
    lateinit var inputText: TextInputEditText
    private lateinit var myApi: CurrencyApi
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var radius: Double = 0.0
    var myCurrency = ""
    var myAddedCoins = 0F
    var savedAddedCoins = 0F
    var requestedCurrencies = ArrayList<String>()
    var savedRequestedCurrencies = ArrayList<String>()
    var pickedAmount = "0"
    var savedRequestId: String = ""
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            myCurrency = savedInstanceState.getString("PickedCurrency").toString()
            savedAddedCoins = savedInstanceState.getFloat("myAddedCoins")
            pickedAmount = savedInstanceState.getString("pickedAmount").toString()
            radius = savedInstanceState.getDouble("Radius")
            latitude = savedInstanceState.getDouble("Latitude")
            longitude = savedInstanceState.getDouble("Longitude")
            savedRequestedCurrencies =
                savedInstanceState.getStringArrayList("requestedCurrencies") as ArrayList<String>
            savedRequestId = savedInstanceState.getString("savedRequestId").toString()
        } else if (arguments != null && arguments!!.getString("fromMapOrEdit") == "true") {
            radius = arguments!!.getDouble("Radius")
            latitude = arguments!!.getDouble("Lat")
            longitude = arguments!!.getDouble("Long")
            myCurrency = arguments!!.getString("PickedCurrency").toString()
            savedAddedCoins = arguments!!.getFloat("savedAddedCoins")
            pickedAmount = arguments!!.getString("pickedAmount").toString()
            savedRequestedCurrencies =
                arguments!!.getStringArrayList("savedRequestedCurrencies") as ArrayList<String>
            savedRequestId = arguments!!.getString("savedRequestId").toString()

        }

    }

    @SuppressLint("RestrictedApi", "SimpleDateFormat", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val url = "https://api.exchangeratesapi.io/latest"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        val coinList = ArrayList<String>()
        val view = inflater.inflate(R.layout.fragment_request, container, false)
        val layout = view.request_layout as RelativeLayout
        val spinnerList = ArrayList<SearchableSpinner>()
        val inputTextList = ArrayList<MaterialTextView>()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {

                val body = response.body()?.string()
                val gson = GsonBuilder().create()

                myApi = gson.fromJson(body, CurrencyApi::class.java)
                myApi.rates["EUR"] = 1.0
                if (savedRequestedCurrencies.isNullOrEmpty()) {
                    for (i in 0..MAX_CURRENECIES.toInt()) {
                        val currency = ""
                        requestedCurrencies.add(currency)
                    }

                } else {
                    requestedCurrencies = savedRequestedCurrencies
                }
                coinList.addAll(convertCoinName(myApi.rates.keys.toList()))
                //first spinner - giving currency
                val spinner = SearchableSpinner(requireContext())
                spinner.layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                spinner.adapter = ArrayAdapter<String>(
                    requireContext(), android.R.layout.simple_list_item_1,
                    coinList
                )
                spinner.setTitle("Select a currency")
                val spinnerParam = spinner.layoutParams as RelativeLayout.LayoutParams
                val spinnerEdgeDist = dpToPx(requireContext(), 0).toInt()
                val spinnerTopDist = dpToPx(requireContext(), 70).toInt()
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
                            updateAmount(
                                pickedAmount,
                                myCurrency,
                                requestedCurrencies,
                                inputTextList,
                                myAddedCoins
                            )
                        }
                    })

                //input text for giving
                inputText = TextInputEditText(requireContext())
                inputText.layoutParams = RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val inputTextParam = inputText.layoutParams as RelativeLayout.LayoutParams
                val inputTextEdgeDist = dpToPx(requireContext(), 230).toInt()
                val inputTextTopDist = dpToPx(requireContext(), 70).toInt()
                inputTextParam.topMargin = inputTextTopDist
                inputTextParam.marginStart = inputTextEdgeDist
                inputText.layoutParams = inputTextParam
                inputText.hint = "Insert here"
                inputText.inputType = InputType.TYPE_CLASS_NUMBER
                inputText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(MAX_MONEY_DIGITS))
                inputText.setText(pickedAmount)
                inputText.setOnClickListener { v ->
                    pickedAmount = inputText.text.toString()
                    updateAmount(
                        pickedAmount,
                        myCurrency,
                        requestedCurrencies,
                        inputTextList,
                        myAddedCoins
                    )
                }

                val myButtonAdd = MaterialButton(requireContext())
                myButtonAdd.layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val addParam = myButtonAdd.layoutParams as RelativeLayout.LayoutParams
                val addEdgeDist = dpToPx(requireContext(), 12).toInt()
                val addTopDist = dpToPx(requireContext(), 222).toInt()
                addParam.topMargin = addTopDist
                addParam.marginStart = addEdgeDist
                myButtonAdd.layoutParams = addParam
                myButtonAdd.text = "Add"


                val myButtonRemove = MaterialButton(requireContext())
                myButtonRemove.layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val removeParam = myButtonRemove.layoutParams as RelativeLayout.LayoutParams
                val removeEdgeDist = dpToPx(requireContext(), 250).toInt()
                val removeTopDist = dpToPx(requireContext(), 222).toInt()
                removeParam.topMargin = removeTopDist
                removeParam.marginStart = removeEdgeDist
                myButtonRemove.layoutParams = removeParam
                myButtonRemove.text = "Remove"
                addButtonClicked(
                    spinnerList,
                    inputTextList,
                    myAddedCoins,
                    coinList,
                    layout,
                    myButtonRemove,
                    requestedCurrencies,
                    myCurrency
                )
                myAddedCoins++
                myButtonAdd.setOnClickListener {
                    updateAmount(
                        pickedAmount,
                        myCurrency,
                        requestedCurrencies,
                        inputTextList,
                        myAddedCoins
                    )
                    val added = addButtonClicked(
                        spinnerList,
                        inputTextList,
                        myAddedCoins,
                        coinList,
                        layout,
                        myButtonRemove,
                        requestedCurrencies,
                        myCurrency
                    )
                    if (added)
                        myAddedCoins++
                }

                myButtonRemove.setOnClickListener {
                    if (myAddedCoins.equals(2F)) {
                        requireActivity().runOnUiThread {
                            layout.removeView(myButtonRemove)
                        }

                    }
                    requireActivity().runOnUiThread {
                        layout.removeView(inputTextList[myAddedCoins.toInt() - 1])
                        layout.removeView(spinnerList[myAddedCoins.toInt() - 1])
                    }

                    inputTextList.removeAt(myAddedCoins.toInt() - 1)
                    spinnerList.removeAt(myAddedCoins.toInt() - 1)
                    requestedCurrencies[myAddedCoins.toInt() - 1] = ""
                    myAddedCoins--

                }
                requireActivity().runOnUiThread {
                    layout.addView(spinner)
                    layout.addView(inputText)
                    layout.addView(myButtonAdd)

                }

                if (savedInstanceState != null || (arguments != null && arguments!!.getString("fromMapOrEdit") == "true")) {
                    spinner.setSelection(coinList.indexOf(myCurrency))

                    for (i in 0 until savedAddedCoins.toInt()) {
                        if (i > 0) {
                            addButtonClicked(
                                spinnerList,
                                inputTextList,
                                myAddedCoins,
                                coinList,
                                layout,
                                myButtonRemove,
                                requestedCurrencies,
                                myCurrency
                            )
                            myAddedCoins++
                        }
                        var updated = false
                        while (!updated) {
                            if (spinnerList.size > i && inputTextList.size > i)
                                updated = true
                        }
                        spinnerList[i].setSelection(coinList.indexOf(requestedCurrencies[i]))


                    }
                }


            }

            override fun onFailure(call: Call, e: IOException) {

            }
        })


        //data base insertion

        database = FirebaseDatabase.getInstance()

        val currentFirebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        val userId = currentFirebaseUser!!.uid
        val locationButton: Button = view.limit_distance
        if (savedRequestId != "") {
            view.submit_button_request.text = "Update"
        }
        if (radius != 0.0) {

            locationButton.setBackgroundColor(Color.rgb(139, 195, 74))
            locationButton.text = "Click to change location"

        }
        locationButton.setOnClickListener {
            val intent = Intent(context, MapsActivity::class.java)
            if (radius != 0.0) {
                intent.putExtra("Radius", radius.toString())
                intent.putExtra("Lat", latitude.toString())
                intent.putExtra("Long", longitude.toString())

            }
            intent.putExtra("PickedCurrency", myCurrency)
            intent.putExtra("savedAddedCoins", myAddedCoins.toString())
            intent.putExtra("pickedAmount", pickedAmount)
            intent.putExtra("savedRequestId", savedRequestId)
            intent.putExtra("savedRequestedCurrencies", requestedCurrencies)
            startActivity(intent)
        }

        //Hit SUBMIT
        view.submit_button_request.setOnClickListener {
            var sameCurrency = false
            for (i in 0..myAddedCoins.toInt()) {//checking if want to exchange same currencies
                if (myCurrency == requestedCurrencies[i])
                    sameCurrency = true
            }
            for (i in 0 until myAddedCoins.toInt()) {//checking if want to exchange same currencies
                for (j in (i + 1)..myAddedCoins.toInt()) {//checking if want to exchange same currencies
                    if (requestedCurrencies[i] == requestedCurrencies[j])
                        sameCurrency = true
                }
            }
            if (inputText.text.isNullOrEmpty() || inputText.text.toString() == "0")
                Toast.makeText(
                    requireContext(),
                    "Please insert a requested amount",
                    Toast.LENGTH_SHORT
                ).show()
            else if (sameCurrency)
                Toast.makeText(
                    requireContext(),
                    "Can't exchange same currency",
                    Toast.LENGTH_SHORT
                ).show()
            else if (radius == 0.0)
                Toast.makeText(
                    requireContext(),
                    "Please choose a location",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                val timeStamp =
                    SimpleDateFormat("yyyyMMdd").format(Date())//yyyyMMdd_HHmmss if want more
                val newTransactionRequest = TransactionRequest(
                    userId,
                    convertCoinName(listOf(myCurrency), reverse = true).first(),
                    inputText.text.toString().toInt(),
                    convertCoinName(requestedCurrencies, reverse = true),
                    timeStamp,
                    latitude,
                    longitude,
                    radius
                )
                val randomId: String = if (savedRequestId == "")
                    randomAlphaNumericString()
                else
                    savedRequestId
                val transactionRef: DatabaseReference =
                    database.getReference("transactionRequests").child(randomId)
                transactionRef.setValue(newTransactionRequest)

                if (savedRequestId != "")
                    Toast.makeText(
                        requireContext(),
                        "Successfully updated your request",
                        Toast.LENGTH_SHORT
                    ).show()
                else {
                    val userTransactionRequestsRef: DatabaseReference =
                        database.getReference("users").child(userId).child("transactionRequests")
                            .push()
                    userTransactionRequestsRef.setValue(randomId)
                    Toast.makeText(
                        requireContext(),
                        "Successfully submitted your request",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                findNavController().navigate(R.id.mainFragment)
            }

        }


        return view
    }

    // This function convert a coin name ("CAD") to name and country ("CAD - Canadian Dollar")
    // get a list and return a list
    private fun convertCoinName(
        coinList: List<String>,
        reverse: Boolean = false
    ): ArrayList<String> {
        val newCoinList: ArrayList<String> = arrayListOf()

        val coinMap = mapOf(
            "CAD" to "CAD - Canadian Dollar",
            "HKD" to "HKD - Hong Kong Dollar",
            "ISK" to "ISK - Icelandic Króna",
            "PHP" to "PHP - Philippine Peso",
            "DKK" to "DKK - Danish Krone",
            "HUF" to "HUF - Hungarian Forint",
            "CZK" to "CZK - Czech Koruna",
            "AUD" to "AUD - Australian Dollar",
            "RON" to "RON - Romanian Leu",
            "SEK" to "SEK - Swedish Krona",
            "IDR" to "IDR - Indonesian Rupiah",
            "INR" to "INR - Indian Rupee",
            "BRL" to "BRL - Brazilian Real",
            "RUB" to "RUB - Russian Ruble",
            "HRK" to "HRK - Croatian Kuna",
            "JPY" to "JPY - Japanese Yen",
            "THB" to "THB - Thai Baht",
            "CHF" to "CHF - Swiss Franc",
            "SGD" to "SGD - Singapore Dollar",
            "PLN" to "PLN - Poland Złoty",
            "BGN" to "BGN - Bulgarian Lev",
            "TRY" to "TRY - Turkish Lira",
            "CNY" to "CNY - Chinese Yuan",
            "NOK" to "NOK - Norwegian Krone",
            "NZD" to "NZD - New Zealand Dollar",
            "ZAR" to "ZAR - South African Rand",
            "USD" to "USD - United States Dollar",
            "MXN" to "MXN - Mexican Peso",
            "ILS" to "ILS - Israeli Shekel",
            "GBP" to "GBP - Pound Sterling",
            "KRW" to "KRW - South Korean Won",
            "MYR" to "MYR - Malaysian Ringgit",
            "EUR" to "EUR - European Union Euro"
        )

        // convert European Union Euro to EUR
        if (reverse) {
            for (coinName in coinList) {
                if (coinName.length > 3)
                    newCoinList.add(coinName.substring(0, 3))
            }
        }

        // convert EUR to European Union Euro
        else {
            for (coinName in coinList) {
                if (coinMap.containsKey(coinName))
                    newCoinList.add(coinMap[coinName].toString())
                else
                    newCoinList.add(coinName)
            }
        }

        return newCoinList
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("PickedCurrency", myCurrency)
        outState.putFloat("myAddedCoins", myAddedCoins)
        outState.putStringArrayList("requestedCurrencies", requestedCurrencies)
        outState.putString("pickedAmount", pickedAmount)
        outState.putDouble("Longitude", longitude)
        outState.putDouble("Latitude", latitude)
        outState.putDouble("Radius", radius)
        outState.putString("savedRequestId", savedRequestId)
    }


    @SuppressLint("RestrictedApi")
    private fun addButtonClicked(
        spinnerList: ArrayList<SearchableSpinner>,
        inputTextList: ArrayList<MaterialTextView>,
        addedCoins: Float,
        coinList: ArrayList<String>,
        layout: RelativeLayout,
        removeButton: Button,
        requestedCurrencies: ArrayList<String>,
        myCurrency: String
    ): Boolean {
        if (addedCoins < MAX_CURRENECIES) {

            val amountText = MaterialTextView(requireContext())
            amountText.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val amountTextParam = amountText.layoutParams as RelativeLayout.LayoutParams
            val amountTextEdgeDist = dpToPx(requireContext(), 240).toInt()
            val amountTextTopDist = dpToPx(requireContext(), 274 + 30 * addedCoins.toInt()).toInt()
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
            if (addedCoins.equals(1F)) {
                requireActivity().runOnUiThread {
                    layout.addView(removeButton)
                }
            }

            val spinnerParam = spinner.layoutParams as RelativeLayout.LayoutParams
            val spinnerEdgeDist = dpToPx(requireContext(), 0).toInt()
            val spinnerTopDist = dpToPx(requireContext(), 260 + 30 * addedCoins.toInt()).toInt()
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
                        val tempString = coinList[position]
                        requestedCurrencies[addedCoins.toInt()] = tempString
                        updateAmount(
                            pickedAmount,
                            myCurrency,
                            requestedCurrencies,
                            inputTextList,
                            myAddedCoins
                        )
                    }
                })
            requireActivity().runOnUiThread {
                spinnerList.add(spinner)
                layout.addView(spinner)
                inputTextList.add(amountText)
                layout.addView(amountText)
            }

            return true
        } else {
            Toast.makeText(requireContext(), "Reached the limit", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    private fun randomAlphaNumericString(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return (1..32)
            .map { kotlin.random.Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }

    fun updateAmount(
        requestedAmount: String,
        myCurrency: String,
        requestedCurrencies: ArrayList<String>,
        inputTextList: ArrayList<MaterialTextView>,
        addedCoins: Float
    ) {
        val myRate: Double
        if (myCurrency != null && myCurrency != ""&& requestedAmount!="") {
            if (myApi.rates[myCurrency.substring(0, 3)] != null) {
                myRate = myApi.rates[myCurrency.substring(0, 3)]!!
                for (i in 0 until requestedCurrencies.size) {
                    if (requestedCurrencies[i] != "") {
                        val requestedCurrencyAmount =
                            (myApi.rates[requestedCurrencies[i].substring(0, 3)]!! / myRate) * requestedAmount.toDouble()
                        inputTextList[i].text = "%.3f".format(requestedCurrencyAmount.toFloat())
                    }
                }
            }
        }


    }
}

