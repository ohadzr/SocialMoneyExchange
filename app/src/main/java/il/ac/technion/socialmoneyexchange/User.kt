package il.ac.technion.socialmoneyexchange

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var firstName: String? = "",
    var lastName: String? = ""
)