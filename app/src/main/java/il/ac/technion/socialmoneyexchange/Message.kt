package il.ac.technion.socialmoneyexchange

class Message {
    constructor() //empty for firebase

    constructor(messageText: String, ID: String){
        text = messageText
        userID = ID
    }
    var text: String? = null
    var timestamp: Long = System.currentTimeMillis()
    var userID: String? = null
    var colorChoose : Boolean = true

    fun setColorChoose(){
        colorChoose = false
    }
}