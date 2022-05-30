package batu.tutorials.readingtime

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_finished_list.*
import kotlinx.android.synthetic.main.fragment_recommendation.*

class RecommendationFragment : Fragment() {
    private var bookIdArrayList: ArrayList<String>? = null
    private var usersAlsoReadIdArrayList: ArrayList<SimilarUserInfo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recommendation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getBooksAndUsers()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun getBooksAndUsers() {

        bookIdArrayList = ArrayList()
        usersAlsoReadIdArrayList = ArrayList()
        val user = FirebaseAuth.getInstance().currentUser
        val database = Firebase.firestore
        val docRef =
            database.collection("users").document(user!!.uid).collection("finished_reading")
        docRef.get().addOnSuccessListener { documents ->
            // get the books of the current user have read
            if (documents != null) {
                for (document in documents) {
                    val data = document.data
                    val id = data["id"].toString()
                    Log.e("book id", id)
                    bookIdArrayList!!.add(id)
                    textViewRecommendation.text = "${textViewRecommendation.text}" + id + "\n"
                }

                // if not empty, then find other users who also read this
                if (bookIdArrayList!!.isNotEmpty()) {
                    for (bookId in bookIdArrayList!!) {
                        val docRefBooks = database.collection("books").document(bookId)
                            .collection("users_read_this")
                        docRefBooks.get().addOnSuccessListener { documents ->
                            if (documents != null) {
                                var times = 0
                                for (document in documents) {
                                    val data = document.data
                                    val id = data["id"].toString()
                                    val rating = data["rating"].toString()

                                    val info = SimilarUserInfo(id, 0)

                                    // check if the user is not the current user
                                    if (id != user.uid) {
                                        Log.e(
                                            "information",
                                            "user: $id, book: $bookId, rating: $rating"
                                        )
                                        times += 1
                                        textViewUsersAlsoRead.text =
                                            "${textViewUsersAlsoRead.text}" + id + "\n"
                                    }
                                }
                                // if not empty, get other books the new user read

                            }
                        }
                    }
                }
                // progressBarFinishedReading.visibility = View.GONE
            } else {
                Toast.makeText(this.context, "There is no book in this list!", Toast.LENGTH_SHORT)
                    .show()
            }

        }.addOnFailureListener { exception ->
            Log.e("failed", "Error getting documents: ", exception)
        }
    }
}