package batu.tutorials.readingtime

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_book_detail.*
import java.util.*


class BookAdapter     // creating constructor for array list and context.
    (// creating variables for arraylist and context.
    private val bookInfoArrayList: ArrayList<BookInfo>, private val mcontext: Context
) :
    RecyclerView.Adapter<BookAdapter.BookViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        // inflating our layout for item of recycler view item.
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.book_rv_item, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {

        // inside on bind view holder method we are
        // setting ou data to each UI component.
        val bookInfo = bookInfoArrayList[position]
        holder.textViewTitle.text = bookInfo.title
        holder.textViewPublisher.text = bookInfo.publisher
        holder.textViewPageCount.text = "No of Pages : " + bookInfo.pageCount
        holder.textViewDate.text = bookInfo.publishedDate

        // below line is use to set image from URL in our image view.
        if (bookInfo.thumbnail.isEmpty()) Picasso.get().load(R.drawable.book)
            .into(holder.imageViewThumbnail)
        else Picasso.get().load(bookInfo.thumbnail).into(holder.imageViewThumbnail)

        // below line is use to add on click listener for our item of recycler view.
        holder.itemView.setOnClickListener { // inside on click listener method we are calling a new activity
            // and passing all the data of that item in next intent.
            val user = FirebaseAuth.getInstance().currentUser
            val database = Firebase.firestore
            var avgRating : Double = 0.0
            var userRating : Double = 0.0
            var times : Int = 0
            database.collection("books").document(bookInfo.id).addSnapshotListener { value, error ->
                if (value != null && value.exists()) {
                    if(value["avg_rating"] != null) {
                        val tempAvg: Any? = value["avg_rating"]
                        val tempTimes: Any? = value["times"]

                        avgRating = 0.0
                        times = 0
                        if (tempAvg is Long) {
                            avgRating = tempAvg.toDouble()
                        } else if (tempAvg is Double) {
                            avgRating = tempAvg
                        }

                        if (tempTimes is Long) {
                            times = tempTimes.toInt()
                        } else if (tempTimes is Double) {
                            times = tempTimes.toInt()
                        } else if (tempTimes is Int) {
                            times = tempTimes
                        }
                    }


                }
            }

            database.collection("users").document(user!!.uid).collection("finished_reading")
                .document(bookInfo.id).get().addOnSuccessListener { snapshot ->
                    if(snapshot["userRating"] != null) userRating = snapshot["userRating"] as Double
            }


            val i = Intent(mcontext, BookDetailActivity::class.java)
            i.putExtra("title", bookInfo.title)
            i.putExtra("bookId", bookInfo.id)
            i.putExtra("subtitle", bookInfo.subtitle)
            i.putExtra("authors", bookInfo.authors)
            i.putExtra("publisher", bookInfo.publisher)
            i.putExtra("publishedDate", bookInfo.publishedDate)
            i.putExtra("description", bookInfo.description)
            i.putExtra("pageCount", bookInfo.pageCount)
            i.putExtra("thumbnail", bookInfo.thumbnail)
            i.putExtra("avgRating", avgRating)
            i.putExtra("userRating", userRating)
            i.putExtra("times", times)
            Log.e("authors in adapter", bookInfo.authors.toString())


            // after passing that data we are
            // starting our new intent.
            mcontext.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        // inside get item count method we
        // are returning the size of our array list.
        return bookInfoArrayList.size
    }

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // below line is use to initialize
        // our text view and image views.
        var textViewTitle: TextView
        var textViewPublisher: TextView
        var textViewPageCount: TextView
        var textViewDate: TextView
        var imageViewThumbnail: ImageView

        init {
            textViewTitle = itemView.findViewById(R.id.idTVBookTitle)
            textViewPublisher = itemView.findViewById(R.id.textViewPublisherDetail)
            textViewPageCount = itemView.findViewById(R.id.idTVPageCount)
            textViewDate = itemView.findViewById(R.id.idTVDate)
            imageViewThumbnail = itemView.findViewById(R.id.imageViewBookDetail)
        }
    }
}
