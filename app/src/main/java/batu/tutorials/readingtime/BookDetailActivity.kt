package batu.tutorials.readingtime

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_book_detail.*
import java.util.*
import kotlin.collections.ArrayList


class BookDetailActivity : AppCompatActivity() {
    // creating variables for strings,text view, image views and button.
    var title: String? = null
    var bookId: String? = null
    var subtitle: String? = null
    var publisher: String? = null
    var publishedDate: String? = null
    var description: String? = null
    var thumbnail: String? = null
    var pageCount = 0
    var authors: ArrayList<String>? = null
    var ratingAvg: Double? = null
    var userRating: Double? = null
    var times: Int? = null

    private lateinit var textViewTitle: TextView
    private lateinit var textViewSubtitle: TextView
    private lateinit var textViewPublisher: TextView
    private lateinit var textViewDetail: TextView
    private lateinit var textViewPageCount: TextView
    private lateinit var textViewDate: TextView
    private lateinit var buttonAddToRead: Button
    private lateinit var buttonFinished: Button
    private lateinit var imageViewBook: ImageView
    private lateinit var textViewAuthors: TextView
    private lateinit var ratingBarAvg: RatingBar
    private lateinit var ratingBarUser: RatingBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)

        // initializing our views..
        textViewTitle = findViewById(R.id.textViewTitleDetail)
        textViewSubtitle = findViewById(R.id.textViewSubtitleDetail)
        textViewPublisher = findViewById(R.id.textViewPublisherDetail)
        textViewAuthors = findViewById(R.id.textViewAuthorsDetail)
        textViewDetail = findViewById(R.id.textViewDescriptionDetail)
        textViewPageCount = findViewById(R.id.textViewPageCountDetail)
        textViewDate = findViewById(R.id.textViewDateDetail)
        buttonAddToRead = findViewById(R.id.buttonAddToReadingList)
        buttonFinished = findViewById(R.id.buttonFinished)
        imageViewBook = findViewById(R.id.imageViewBookDetail)
        ratingBarAvg = findViewById(R.id.ratingBarAvg)
        ratingBarUser = findViewById(R.id.ratingBarUser)

        // getting the data which we have passed from our adapter class.
        title = intent.getStringExtra("title")
        bookId = intent.getStringExtra("bookId")
        subtitle = intent.getStringExtra("subtitle")
        publisher = intent.getStringExtra("publisher")
        publishedDate = intent.getStringExtra("publishedDate")
        description = intent.getStringExtra("description")
        pageCount = intent.getIntExtra("pageCount", 0)
        thumbnail = intent.getStringExtra("thumbnail")
        authors = intent.getStringArrayListExtra("authors")
        ratingAvg = intent.getDoubleExtra("ratingAvg", 0.0)
        userRating = intent.getDoubleExtra("userRating", 0.0)
        times = intent.getIntExtra("times", 0)

        // after getting the data we are setting
        // that data to our text views and image view.
        textViewTitle.setText(title)
        textViewSubtitle.setText(subtitle)
        textViewPublisher.setText(publisher)
        textViewDate.setText("Published On : $publishedDate")
        textViewDetail.setText(description)
        textViewPageCount.setText("No Of Pages : $pageCount")
        ratingBarAvg.rating = ratingAvg!!.toFloat()
        ratingBarUser.rating = userRating!!.toFloat()
        Log.e("authors detail =>", authors.toString())

        textViewAuthors.text = "Authors: "
        for (i in authors!!) {
            if (i != authors!!.last()) textViewAuthors.text =
                textViewAuthors.text.toString() + "$i, "
            else textViewAuthors.text = textViewAuthors.text.toString() + "$i"
        }


        if (thumbnail.isNullOrEmpty()) Picasso.get().load(R.drawable.book).into(imageViewBookDetail)
        else Picasso.get().load(thumbnail).into(imageViewBookDetail)


        Log.e("thumbnail detail", thumbnail.toString())

        // adding on click listener for our preview button.
        buttonAddToRead.setOnClickListener(View.OnClickListener {
            if (bookId!!.isEmpty()) {
                // below toast message is displayed when book id is empty.
                Toast.makeText(
                    this@BookDetailActivity,
                    "An error occurred. No such book.",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }
            // if the id is not empty
            // add this book to the shelf.
            val user = FirebaseAuth.getInstance().currentUser
            val database = Firebase.firestore
            val book = hashMapOf(
                "title" to title,
                "id" to bookId,
                "subtitle" to subtitle,
                "publisher" to publisher,
                "publishedDate" to publishedDate,
                "pageCount" to pageCount,
                "thumbnail" to thumbnail,
                "authors" to authors,
                "description" to description,
            )
            database.collection("users").document(user!!.uid).collection("reading_list")
                .document(bookId.toString()).set(book).addOnSuccessListener {
                    Log.d(
                        "Success",
                        "DocumentSnapshot successfully written!"
                    )
                    Toast.makeText(this, "Added to your reading list.", Toast.LENGTH_SHORT).show()
                    // TODO: when a book is added to reading list, it needs to be deleted from the finished list
                    // and the amount of time needs to be decreased
                }.addOnFailureListener { e ->
                    Log.w("Error", "Error writing document", e)
                }


            /* val uri = Uri.parse(previewLink)
            val i = Intent(Intent.ACTION_VIEW, uri)
            startActivity(i) */
        })

        // initializing on click listener for buy button.
        buttonFinished.setOnClickListener(View.OnClickListener {
            if (bookId!!.isEmpty()) {
                // below toast message is displayed when book id is empty.
                Toast.makeText(
                    this@BookDetailActivity,
                    "An error occurred. No such book.",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }
            // if the id is not empty
            // add this book to the shelf.
            val user = FirebaseAuth.getInstance().currentUser
            val database = Firebase.firestore

            // user rating
            val rating = ratingBarUser.rating.toDouble()
            val book = hashMapOf(
                "title" to title,
                "id" to bookId,
                "subtitle" to subtitle,
                "publisher" to publisher,
                "publishedDate" to publishedDate,
                "pageCount" to pageCount,
                "thumbnail" to thumbnail,
                "authors" to authors,
                "description" to description,
                "userRating" to rating
            )

            // add to finished_reading collection
            database.collection("users").document(user!!.uid).collection("finished_reading")
                .document(bookId.toString()).set(book).addOnSuccessListener {
                    Log.d(
                        "Success",
                        "DocumentSnapshot successfully written!"
                    )
                    Toast.makeText(this, "Added to your finished list.", Toast.LENGTH_SHORT).show()
                    // for now, only average speed of reading is used
                    database.collection("users").document(user.uid)
                        .update(
                            "total_time",
                            FieldValue.increment(pageCount.toDouble() * 300 / 200)
                        )
                    database.collection("users").document(user.uid).collection("reading_list")
                        .document(bookId.toString()).delete().addOnSuccessListener {
                            Log.e("Read and deleted", "$bookId deleted from the reading list")
                        }
                }.addOnFailureListener { e ->
                    Log.w("Error", "Error writing document", e)
                }

            database.collection("books").document(bookId.toString()).get()
                .addOnSuccessListener { docSnap ->
                    if (docSnap.exists()) {
                        database.collection("books").document(bookId.toString()).get()
                            .addOnSuccessListener { snapshot ->
                                Log.e("Nesin snapshot success", snapshot["avg_rating"].toString())
                                if (snapshot["avg_rating"] != null && snapshot["times"] != null) {
                                    val tempAvg: Any? = snapshot["avg_rating"]
                                    val tempTimes: Any? = snapshot["times"]

                                    var avg = 0.0
                                    var times = 0
                                    if (tempAvg is Long) {
                                        avg = tempAvg.toDouble()
                                    } else if (tempAvg is Double) {
                                        avg = tempAvg
                                    }

                                    if (tempTimes is Long) {
                                        times = tempTimes.toInt()
                                    } else if (tempTimes is Double) {
                                        times = tempTimes.toInt()
                                    } else if (tempTimes is Int) {
                                        times = tempTimes
                                    }
                                    times += 1
                                    avg = (avg + rating)
                                    Log.e("nesin rating now", avg.toString())
                                    database.collection("books").document(bookId.toString()).update(
                                        mapOf(
                                            "avg_rating" to avg,
                                            "times" to times
                                        )
                                    )
                                }

                            }.addOnFailureListener {
                                Log.e("avg rating", "not found!")
                            }

                        val userData = hashMapOf("id" to user.uid, "rating" to rating)
                        database.collection("books").document(bookId.toString())
                            .collection("users_read_this").document(user.uid)
                            .set(userData)

                    } else {
                        val userData = hashMapOf("id" to user.uid, "rating" to rating)
                        val bookForAverage = hashMapOf(
                            "title" to title,
                            "id" to bookId,
                            "subtitle" to subtitle,
                            "publisher" to publisher,
                            "publishedDate" to publishedDate,
                            "pageCount" to pageCount,
                            "thumbnail" to thumbnail,
                            "authors" to authors,
                            "description" to description,
                            "average_rating" to 0.0,
                            "times" to 0
                        )
                        database.collection("books").document(bookId.toString()).set(bookForAverage)
                            .addOnCompleteListener {
                                database.collection("books").document(bookId.toString())
                                    .collection("users_read_this").document(user.uid)
                                    .set(userData)
                            }
                    }
                }
        })

        val user = FirebaseAuth.getInstance().currentUser
        val database = Firebase.firestore

        // set ratingBar for average rating
        database.collection("books").document(bookId.toString())
            .addSnapshotListener { value, error ->
                Log.e("Current nesin =>", value!!["avg_rating"].toString())
                if (value["avg_rating"] != null) {
                    val tempAvg: Any? = value["avg_rating"]
                    val tempTimes: Any? = value["times"]

                    var avg = 0.0
                    var times = 0
                    if (tempAvg is Long) {
                        avg = tempAvg.toDouble()
                    } else if (tempAvg is Double) {
                        avg = tempAvg
                    }

                    if (tempTimes is Long) {
                        times = tempTimes.toInt()
                    } else if (tempTimes is Double) {
                        times = tempTimes.toInt()
                    } else if (tempTimes is Int) {
                        times = tempTimes
                    }
                    textViewAvgRating.text = "$times time(s) rated"
                    ratingBarAvg.rating = avg.toFloat() / times

                } else {
                    textViewAvgRating.text = "Never rated before!"
                }
            }

        // set ratingBar for user rating
        database.collection("books").document(bookId.toString())
            .collection("users_read_this").document(user!!.uid)
            .addSnapshotListener { value, error ->
                if (value!!.get("rating") != null) {
                    val tempRating: Any? = value["rating"]
                    var rating = 0.0
                    if (tempRating is Long) {
                        rating = tempRating.toDouble()
                    } else if (tempRating is Double) {
                        rating = tempRating
                    }

                    ratingBarUser.rating = rating.toFloat()
                }
            }

    }
}
