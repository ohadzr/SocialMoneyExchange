package il.ac.technion.socialmoneyexchange

import android.annotation.SuppressLint
import android.graphics.Color
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


class RequestFragment : Fragment() {
    private val MAX_CURRNECIES = 5F
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

        var myAddedCoins = 0F
        val view = inflater.inflate(R.layout.fragment_request, container, false)
        val layout = view.request_layout as RelativeLayout
        val spinnerList = ArrayList<com.toptoche.searchablespinnerlibrary.SearchableSpinner>()
        val inputTextList = ArrayList<com.google.android.material.textfield.TextInputEditText>()
        val pickedCoin = ArrayList<Currency>()
        for (i in 0..MAX_CURRNECIES.toInt()) {
            val currency = Currency("temp", 0)
            pickedCoin.add(currency)
        }

        val myButtonAdd = com.google.android.material.button.MaterialButton(requireContext())
        myButtonAdd.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val addParam = myButtonAdd.layoutParams as RelativeLayout.LayoutParams
        val addEdgeDist = dpToPx(requireContext(),12).toInt()
        val addTopDist = dpToPx(requireContext(),72).toInt()
        addParam.topMargin = addTopDist
        addParam.marginStart = addEdgeDist
        myButtonAdd.layoutParams = addParam
        myButtonAdd.text = "Add"
        val myButtonRemove = com.google.android.material.button.MaterialButton(requireContext())

        myButtonRemove.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val removeParam = myButtonRemove.layoutParams as RelativeLayout.LayoutParams
        val removeEdgeDist = dpToPx(requireContext(),110).toInt()
        val removeTopDist = dpToPx(requireContext(),72).toInt()
        removeParam.topMargin = removeTopDist
        removeParam.marginStart = removeEdgeDist
        myButtonRemove.layoutParams = removeParam
        myButtonRemove.text = "Remove"
        addButtonClicked(spinnerList,inputTextList,myAddedCoins,coinList,layout,myButtonRemove,pickedCoin)
        myAddedCoins++
//        mySpinner.adapter = adapterTest
        myButtonAdd.setOnClickListener {
            val added = addButtonClicked(spinnerList,inputTextList,myAddedCoins,coinList,layout,myButtonRemove,pickedCoin)
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
        layout.addView(myButtonAdd)

        view.submit_button_request.setOnClickListener {
            var pickedString = ""
            for(i in 0 until myAddedCoins.toInt()){
                pickedString = pickedString + " " + pickedCoin[i].type + " " + pickedCoin[i].amount
            }
            Toast.makeText(requireContext(),pickedString, Toast.LENGTH_SHORT).show()
        }

        return view
    }
    @SuppressLint("RestrictedApi")
    private fun addButtonClicked(spinnerList:ArrayList<com.toptoche.searchablespinnerlibrary.SearchableSpinner>,inputTextList:ArrayList<com.google.android.material.textfield.TextInputEditText>,
                                 addedCoins:Float, coinList:ArrayList<String>, layout:RelativeLayout, removeButton:Button, pickedCoin:ArrayList<Currency>):Boolean{
        if(addedCoins < MAX_CURRNECIES) {
            var tempCoins = addedCoins
            val inputText =com.google.android.material.textfield.TextInputEditText(requireContext())
            inputText.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
            val inputTextParam = inputText.layoutParams as RelativeLayout.LayoutParams
            val inputTextEdgeDist = dpToPx(requireContext(),130).toInt()
            val inputTextTopDist = dpToPx(requireContext(),110+24*tempCoins.toInt()).toInt()
            inputTextParam.topMargin = inputTextTopDist
            inputTextParam.marginStart = inputTextEdgeDist
            inputText.layoutParams = inputTextParam
            inputText.hint="Insert amount"
            inputText.inputType = InputType.TYPE_CLASS_NUMBER
            inputText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(6))
            inputText.setOnEditorActionListener { v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    pickedCoin[tempCoins.toInt()].amount=actionId
                    true
                } else {
                    false
                }
            }
            inputText.setOnClickListener({

            })

            val spinner =
                com.toptoche.searchablespinnerlibrary.SearchableSpinner(requireContext())
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
            val spinnerTopDist = dpToPx(requireContext(),110+24*tempCoins.toInt()).toInt()
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
                        pickedCoin[tempCoins.toInt()].type=tempString
                        Toast.makeText(requireContext(),tempCoins.toString(),Toast.LENGTH_SHORT).show()

                    }
                })
            spinnerList.add(spinner)
            layout.addView(spinner)
            inputTextList.add(inputText)
            layout.addView(inputText)
            return true
        }
        else{
            Toast.makeText(requireContext(), "Reached the limit", Toast.LENGTH_SHORT).show()
            return false
        }
    }


}

data class Currency(var type : String, var amount : Int)
