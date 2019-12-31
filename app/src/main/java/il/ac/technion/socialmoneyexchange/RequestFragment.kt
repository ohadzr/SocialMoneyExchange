package il.ac.technion.socialmoneyexchange

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import kotlinx.android.synthetic.main.fragment_request.view.*


class RequestFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val numbers = ArrayList<String>()
        numbers.add("one")
        numbers.add("one")
        numbers.add("one")
        numbers.add("one")
        numbers.add("one")
        numbers.add("one")
        numbers.add("one")
        numbers.add("one")
        numbers.add("one")
        numbers.add("one")

        val adapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, numbers)
        val view = inflater.inflate(R.layout.fragment_request, container, false)
        view.searchable_coin_mine.adapter = adapter
        return view
    }


}
