package batu.tutorials.readingtime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_recommendation.*
import kotlinx.android.synthetic.main.fragment_home.recyclerViewHomeMostRead

class RecommendationActivity : AppCompatActivity() {
    var title: String? = null
    var bookId: String? = null

    private var bookInfoArrayList: ArrayList<BookInfo>? = null
    // need to make this <BookInfo>
    private var bookIdList: MutableList<String> = mutableListOf()
    private var countSameBooks: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendation)
        title = intent.getStringExtra("title")
        bookId = intent.getStringExtra("bookId")
        Log.e("activity recommend", "$title, $bookId")
        if(bookId.isNullOrEmpty()) textViewRecommended.text = "No book found!"
        else {
            getBooksAndUsers(bookId!!)
            textViewRecommended.text = "Recommendations for: $title"
        }
    }
    private fun getBooksAndUsers(id: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val database = Firebase.firestore
        val temp_id = id

        // find other users who also read the book
        database.collection("books").document(temp_id).collection("users_read_this")
            .whereNotEqualTo("id", user!!.uid).get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    var sizeFirst = documents.size()
                    Log.e("counter", "size first: $sizeFirst")
                    for (doc in documents) {
                        sizeFirst -= 1
                        val data = doc.data
                        val tempUserId = data["id"]
                        // find each user's books where rating > 3
                        database.collection("users").document(tempUserId.toString())
                            .collection("finished_reading")
                            .whereGreaterThanOrEqualTo("userRating", 3)
                            .get().addOnSuccessListener { snapshot ->
                                var sizeSecond = snapshot.documents.size
                                for (doc in snapshot.documents) {
                                    if (doc["id"].toString() != temp_id) {
                                        sizeSecond -= 1
                                        Log.e("counter", "size second: $sizeSecond")
                                        Log.e("marked", "different book than temp_id: ${doc["id"]}")
                                        // check if the current user and the other user has a similar taste
                                        val user = FirebaseAuth.getInstance().currentUser
                                        database.collection("users").document(user!!.uid)
                                            .collection("finished_reading")
                                            .whereGreaterThanOrEqualTo("userRating", 3).get()
                                            .addOnSuccessListener { snap ->
                                                for (currentUsersDoc in snap) {
                                                    if (doc["id"] == currentUsersDoc.data["id"]) {
                                                        Log.e(
                                                            "marked",
                                                            "found a same book: ${currentUsersDoc.data["id"]}"
                                                        )
                                                        countSameBooks += 1
                                                    }
                                                }
                                                Log.e("marked", countSameBooks.toString())
                                                // normalde burada if
                                                if (countSameBooks >= 2) {
                                                    Log.e("marked", "similar taste: $tempUserId")
                                                    database.collection("users")
                                                        .document(tempUserId.toString())
                                                        .collection("finished_reading")
                                                        .whereGreaterThanOrEqualTo("userRating", 3)
                                                        .get().addOnSuccessListener { snapshot ->
                                                            var sizeThird = snapshot.documents.size
                                                            for (doc in snapshot.documents) {
                                                                if (doc["id"].toString() != temp_id) {
                                                                    sizeThird -= 1
                                                                    // check if the book is already read by current user or not
                                                                    val ref =
                                                                        database.collection("users")
                                                                            .document(user.uid)
                                                                            .collection("finished_reading")
                                                                            .whereEqualTo(
                                                                                "id",
                                                                                doc["id"]
                                                                            ).get()
                                                                    ref.addOnSuccessListener { q ->
                                                                        if (q.isEmpty) {
                                                                            bookIdList.add(doc["id"].toString())
                                                                            Log.e(
                                                                                "marked",
                                                                                "recommend: ${doc["title"]} - ${doc["userRating"]}"
                                                                            )
                                                                        }
                                                                        // get unique ones if all the process is done
                                                                        if(sizeFirst == 0 && sizeSecond == 0 && sizeThird == 0) {
                                                                            val uniqueBooks =
                                                                                bookIdList.distinct()
                                                                            loadBooks(uniqueBooks)
                                                                        }
                                                                    }
                                                                } else sizeThird -= 1
                                                            }
                                                        }
                                                }
                                            }
                                    } else sizeSecond -= 1
                                }
                            }
                    }

                }

            }

    }

    private fun loadBooks(idList: List<String>) {
        Log.e("incoming list", idList.toString())

        val user = FirebaseAuth.getInstance().currentUser
        val database = Firebase.firestore

        if(idList.isNotEmpty()) {
            database.collection("books").whereIn("id", idList).get().addOnSuccessListener {
                    datalist ->
                // reset and update the recycler view
                bookInfoArrayList = ArrayList<BookInfo>()
                bookInfoArrayList!!.clear()
                recyclerViewRecommendations.adapter?.notifyDataSetChanged()
                // Log.e("datalist size:", datalist.documents.size.toString())
                for(data in datalist) {
                    val title = data["title"].toString()
                    val id = data["id"].toString()
                    val subtitle = data["subtitle"].toString()
                    val authorsArray = data["authors"] as ArrayList<*>
                    val authorsArrayList = ArrayList<String>()
                    for (i in authorsArray) {
                        authorsArrayList.add(i.toString())
                    }
                    val publisher = data["publisher"].toString()
                    val publishedDate = data["publishedDate"].toString()
                    val description = data["description"].toString()
                    val pageCount = data["pageCount"] as Long
                    val thumbnail = data["thumbnail"].toString()

                    val bookInfo = BookInfo(
                        title,
                        id,
                        subtitle,
                        authorsArrayList,
                        publisher,
                        publishedDate,
                        description,
                        pageCount.toInt(),
                        thumbnail
                    )
                    bookInfoArrayList!!.add(bookInfo)
                    Log.e("arraylistSize++", bookInfoArrayList!!.size.toString())
                }
                Log.e("arraylistSize after", bookInfoArrayList!!.size.toString())
                val adapter = BookAdapter(bookInfoArrayList!!, applicationContext!!)
                val linearLayoutManager =
                    LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
                recyclerViewRecommendations.layoutManager = linearLayoutManager
                recyclerViewRecommendations.adapter = adapter
            }
        }




    }
}