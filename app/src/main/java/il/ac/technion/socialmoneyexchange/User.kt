package il.ac.technion.socialmoneyexchange

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.Transaction

@IgnoreExtraProperties
data class User(
    var firstName: String? = "",
    var lastName: String? = "",
    var imgUrl: String? = "",
    var transactions : ArrayList<String>?=null,
    var offers : ArrayList<String>?=null,
    val token: String? = ""
)