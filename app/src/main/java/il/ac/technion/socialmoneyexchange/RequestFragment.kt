package il.ac.technion.socialmoneyexchange

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import androidx.databinding.DataBindingUtil.setContentView
import kotlinx.android.synthetic.main.fragment_request.view.*


class RequestFragment : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val coinList = ArrayList<String>()
        coinList.add("one")
        coinList.add("two")
        coinList.add("three")
        coinList.add("four")
        coinList.add("five")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        coinList.add("one")
        val view = inflater.inflate(R.layout.fragment_request, container, false)
        val layout = view.request_layout as RelativeLayout
        val spinnerList = ArrayList<com.toptoche.searchablespinnerlibrary.SearchableSpinner>()
        val myCurrText = TextView(requireContext())

        // setting height and width
        myCurrText.layoutParams= RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        // setting text
        myCurrText.text = "Please choose what currency you want to receive"
        myCurrText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        layout.addView(myCurrText)
        val mySpinner = com.toptoche.searchablespinnerlibrary.SearchableSpinner(requireContext())
        mySpinner.layoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mySpinner.setTitle("Select a currency")
        mySpinner.y = 150F
        val myButtonAdd = Button(requireContext())
        myButtonAdd.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        myButtonAdd.text = "Add"
        myButtonAdd.x = -300F
        myButtonAdd.y = 150F
        val myButtonRemove = Button(requireContext())
        myButtonRemove.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        myButtonRemove.text = "Remove"
        myButtonRemove.x = -600F
        myButtonRemove.y = 150F
        var myAddedCoins = 0F

        val adapterTest = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, coinList)
        mySpinner.adapter = adapterTest
        myButtonAdd.setOnClickListener {
            var added = addButtonClicked(spinnerList,view,myAddedCoins,coinList,layout,myButtonRemove)
            if(added)
                myAddedCoins++
        }
        myButtonRemove.setOnClickListener{
            if(myAddedCoins.equals(1F)){
                layout.removeView(myButtonRemove)
            }
            layout.removeView(spinnerList[myAddedCoins.toInt()-1])
            spinnerList.removeAt(myAddedCoins.toInt()-1)
            myAddedCoins--
        }
        layout.addView(myButtonAdd)
        layout.addView(mySpinner)
        return view
    }
    fun addButtonClicked(spinnerList:ArrayList<com.toptoche.searchablespinnerlibrary.SearchableSpinner>,
                         view:View,addedCoins:Float,coinList:ArrayList<String>,layout:RelativeLayout,removeButton:Button):Boolean{
        if(addedCoins < 5F) {
            var tempCoins = addedCoins
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
            if (tempCoins.equals(0F)) {
                layout.addView(removeButton)
            }
            tempCoins++
            spinner.y = 150F+100F * tempCoins
            spinnerList.add(spinner)
            layout.addView(spinner)
            return true
        }
        else{
            Toast.makeText(requireContext(), "Reached the limit", Toast.LENGTH_SHORT).show()
            return false
        }
    }


}
