package il.ac.technion.socialmoneyexchange

class Review {
    constructor() //empty for firebase

    constructor(reviewText: String, reviewUserUrl: String, reviewUserName: String){
        text = reviewText
        userUrl = reviewUserUrl
        userName = reviewUserName
    }
    var text: String? = null
    var timestamp: Long = System.currentTimeMillis()
    var userUrl: String? = null
    var userName : String? = null


}