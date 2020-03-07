package il.ac.technion.socialmoneyexchange

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Offer(
    var userID1: String? = "",
    var coinName1: String? = "",
    var coinAmount1: Float?,
    var userID2: String? = "",
    var coinName2: String? = "",
    var coinAmount2: Float?,
    var status: String? = ""
    )