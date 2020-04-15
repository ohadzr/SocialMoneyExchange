package il.ac.technion.socialmoneyexchange



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.review_item.view.*

class ReviewAdapter(val review: ArrayList<Review>, val itemClick: (Review) -> Unit) :
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(review[position])
    }

    override fun getItemCount() = review.size

    class ViewHolder(view: View, val itemClick: (Review) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bindForecast(review: Review) {
            itemView.reviewAdapterReviewItem.text = review.text
            itemView.userName.text = review.userName
            bindImage(itemView.user_img,review.userUrl)


//                itemView.setOnClickListener { itemClick(this) }
        }

        private fun bindImage(imgView: ImageView, imgUrl: String?) {
            imgUrl?.let {
                val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
                Glide.with(imgView.context)
                    .load(imgUri)
                    .into(imgView)
            }
        }
    }

}