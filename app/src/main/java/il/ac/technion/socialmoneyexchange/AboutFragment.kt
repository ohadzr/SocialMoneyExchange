package il.ac.technion.socialmoneyexchange

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_about.view.*


class AboutFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_about, container, false)
        view.debug_button.setOnClickListener {
            Toast.makeText(requireContext(),"Oh no...\nSeems like there is a bug with the debug button :(",Toast.LENGTH_LONG).show()
        }
        return view
    }

}
