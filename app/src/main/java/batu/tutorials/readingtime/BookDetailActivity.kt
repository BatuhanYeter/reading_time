package batu.tutorials.readingtime

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
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

        Log.e("authors", authors.toString())
        // after getting the data we are setting
        // that data to our text views and image view.
        textViewTitle.setText(title)
        textViewSubtitle.setText(subtitle)
        textViewPublisher.setText(publisher)
        textViewDate.setText("Published On : $publishedDate")
        textViewDetail.setText(description)
        textViewPageCount.setText("No Of Pages : $pageCount")

        textViewAuthors.text = "Authors: "
        for (i in authors!!) {
            if (i != authors!!.last()) textViewAuthors.text =
                textViewAuthors.text.toString() + "$i, "
            else textViewAuthors.text = textViewAuthors.text.toString() + "$i"
        }

        if (thumbnail.isNullOrEmpty()) Picasso.get().load(R.drawable.book).into(imageViewBook)
        else Picasso.get().load(thumbnail).into(imageViewBook)

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


        })
    }
}
