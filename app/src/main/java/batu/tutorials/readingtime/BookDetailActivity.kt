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
    private lateinit var buttonRecommendation: Button

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
        buttonRecommendation = findViewById(R.id.buttonRecommend)

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
                    // check if the book exists in the user's finished list
                    database.collection("users").document(user.uid).collection("finished_reading")
                        .document(bookId.toString()).get().addOnSuccessListener { docSnap ->
                            if (docSnap.exists()) {
                                // it exists, then delete it from the finished list and decrease the total_time
                                database.collection("users").document(user.uid)
                                    .collection("finished_reading")
                                    .document(bookId.toString()).delete().addOnSuccessListener {
                                        Log.e(
                                            "Read and deleted",
                                            "$bookId deleted from the finished list"
                                        )
                                        // for now, only average speed of reading is used
                                        database.collection("users").document(user.uid)
                                            .update(
                                                "total_time",
                                                FieldValue.increment(-pageCount.toDouble() * 300 / 200)
                                            )
                                    }
                            }
                        }
                }.addOnFailureListener { e ->
                    Log.w("Error", "Error writing document", e)
                }
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


            // check if the book is already in the user's list or not
            database.collection("users").document(user!!.uid).collection("finished_reading")
                .document(bookId.toString()).get().addOnSuccessListener { docSnap ->
                    if (docSnap.exists()) {
                        // already exists in the finished reading list
                        Toast.makeText(
                            this,
                            "This book is already in your finished list!",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        // not exists
                        // add to finished_reading collection
                        database.collection("users").document(user.uid)
                            .collection("finished_reading")
                            .document(bookId.toString()).set(book).addOnSuccessListener {
                                Log.d(
                                    "Success",
                                    "DocumentSnapshot successfully written!"
                                )
                                Toast.makeText(
                                    this,
                                    "Added to your finished list.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // for now, only average speed of reading is used
                                database.collection("users").document(user.uid)
                                    .update(
                                        "total_time",
                                        FieldValue.increment(pageCount.toDouble() * 300 / 200)
                                    )
                                // also delete from the reading_list if exists
                                database.collection("users").document(user.uid)
                                    .collection("reading_list")
                                    .document(bookId.toString()).delete().addOnSuccessListener {
                                        Log.e(
                                            "Read and deleted",
                                            "$bookId deleted from the reading list"
                                        )
                                    }
                            }.addOnFailureListener { e ->
                                Log.w("Error", "Error writing document", e)
                            }
                    }
                    // add to books collection and beyond (users_read_this collection and check if rated)
                    database.collection("books").document(bookId.toString()).get()
                        .addOnSuccessListener { docSnap ->
                            if (docSnap.exists()) {
                                database.collection("books").document(bookId.toString()).get()
                                    .addOnSuccessListener { snapshot ->
                                        // if avg_rating already defined
                                        if (snapshot["avg_rating"] != null && snapshot["times"] != null) {
                                            // check if the user already rated
                                            database.collection("books").document(bookId.toString())
                                                .collection("users_read_this")
                                                .whereEqualTo("id", user.uid)
                                                .get().addOnSuccessListener {
                                                    // already rated, (avg - oldRating) and then (avg + new rating)
                                                    database.collection("books")
                                                        .document(bookId.toString())
                                                        .collection("users_read_this")
                                                        .document(user.uid)
                                                        .update("rating", rating)
                                                    Log.e(
                                                        "already rated enter",
                                                        "new rating: $rating"
                                                    )
                                                    // update the userRating in users collection
                                                    database.collection("users").document(user.uid)
                                                        .collection("finished_reading")
                                                        .document(bookId.toString())
                                                        .update("userRating", rating)

                                                    // sum up all the ratings
                                                    var finalRating = 0.0
                                                    var finalTimes = 0
                                                    database.collection("books")
                                                        .document(bookId.toString())
                                                        .collection("users_read_this")
                                                        .whereGreaterThanOrEqualTo("rating", 0.0)
                                                        .get()
                                                        .addOnSuccessListener { documents ->
                                                            for (document in documents) {
                                                                val tempRating =
                                                                    document.data["rating"]
                                                                if (tempRating is Long) {
                                                                    finalRating += tempRating.toDouble()
                                                                    finalTimes += 1
                                                                } else if (tempRating is Double) {
                                                                    finalRating += tempRating
                                                                    finalTimes += 1
                                                                }
                                                            }
                                                            // update the avg_rating
                                                            database.collection("books")
                                                                .document(bookId.toString())
                                                                .update(
                                                                    mapOf(
                                                                        "avg_rating" to finalRating,
                                                                        "times" to finalTimes
                                                                    )
                                                                )

                                                        }

                                                    Toast.makeText(
                                                        this,
                                                        "Your rating is updated!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }.addOnFailureListener {
                                                    // not rated before, set the user data
                                                    database.collection("books")
                                                        .document(bookId.toString())
                                                        .collection("users_read_this")
                                                        .document(user.uid)
                                                        .update("rating", rating)

                                                    // sum up all the ratings
                                                    var finalRating = 0.0
                                                    var finalTimes = 0
                                                    database.collection("books")
                                                        .document(bookId.toString())
                                                        .collection("users_read_this")
                                                        .whereGreaterThanOrEqualTo("rating", 0.0)
                                                        .get()
                                                        .addOnSuccessListener { documents ->
                                                            for (document in documents) {
                                                                val tempRating =
                                                                    document.data["rating"]
                                                                if (tempRating is Long) {
                                                                    finalRating += tempRating.toDouble()
                                                                    finalTimes += 1
                                                                } else if (tempRating is Double) {
                                                                    finalRating += tempRating
                                                                    finalTimes += 1
                                                                }
                                                            }
                                                            // finally, update the avg_rating
                                                            database.collection("books")
                                                                .document(bookId.toString())
                                                                .update(
                                                                    mapOf(
                                                                        "avg_rating" to finalRating,
                                                                        "times" to finalTimes
                                                                    )
                                                                )
                                                        }


                                                }
                                        }
                                    }.addOnFailureListener {
                                        Log.e("avg rating", "not found!")
                                    }

                                val userData = hashMapOf("id" to user.uid, "rating" to rating)
                                database.collection("books").document(bookId.toString())
                                    .collection("users_read_this").document(user.uid)
                                    .set(userData)
                            } else {
                                Log.e("book first time", user.uid)
                                // set the book to books collection for the first time
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
                                    "avg_rating" to 0.0,
                                    "times" to 0
                                )
                                database.collection("books").document(bookId.toString())
                                    .set(bookForAverage)
                                    .addOnCompleteListener {
                                        database.collection("books").document(bookId.toString())
                                            .collection("users_read_this").document(user.uid)
                                            .set(userData)
                                    }

                                // finally, update the avg_rating
                                database.collection("books")
                                    .document(bookId.toString())
                                    .update(
                                        mapOf(
                                            "avg_rating" to rating,
                                            "times" to 1
                                        )
                                    )
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

        // recommendations
        buttonRecommendation.setOnClickListener {
            val i = Intent(applicationContext, RecommendationActivity::class.java)
            Log.e("activity", "$title, $bookId")
            i.putExtra("title", title)
            i.putExtra("bookId", bookId)
            startActivity(i)
        }
    }
}
