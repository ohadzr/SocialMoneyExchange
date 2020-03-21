package il.ac.technion.socialmoneyexchange
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TransactionRequest (
    var userId: String? = "",
    var myCurrency: String? = "",
    var requestedAmount: Int? = 0,
    var requestedCurrencies : ArrayList<String>?=null,
    var timeStamp : String? = "",
    var latitude: Double? = null,
    var longitude: Double? = null,
    var radius: Double? = null
)