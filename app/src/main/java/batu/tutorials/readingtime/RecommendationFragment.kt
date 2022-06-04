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
    // need to make this <BookInfo>
    private var bookIdList: MutableList<String> = mutableListOf()
    private var countSameBooks: Int = 0
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
        val user = FirebaseAuth.getInstance().currentUser
        val database = Firebase.firestore
        val temp_id = "3Q8KtAEACAAJ"

        // find other users who also read the book
        database.collection("books").document(temp_id).collection("users_read_this")
            .whereNotEqualTo("id", user!!.uid).get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    for (doc in documents) {
                        val data = doc.data
                        val tempUserId = data["id"]
                        // find each user's books where rating > 3
                        database.collection("users").document(tempUserId.toString())
                            .collection("finished_reading")
                            .whereGreaterThanOrEqualTo("userRating", 3)
                            .get().addOnSuccessListener { snapshot ->
                                for (doc in snapshot.documents) {
                                    if (doc["id"].toString() != temp_id) {
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
                                                            for (doc in snapshot.documents) {
                                                                if (doc["id"].toString() != temp_id) {
                                                                    // check if the book is already read by current user or not
                                                                    val ref = database.collection("users")
                                                                        .document(user.uid)
                                                                        .collection("finished_reading")
                                                                        .whereEqualTo(
                                                                            "id",
                                                                            doc["id"]
                                                                        ).get()
                                                                    ref.addOnSuccessListener { q ->
                                                                        if(q.isEmpty) {
                                                                            bookIdList.add(doc["id"].toString())
                                                                            Log.e("marked", "recommend: ${doc["title"]} - ${doc["userRating"]}")
                                                                        }
                                                                        // get unique ones
                                                                        val uniqueBooks = bookIdList.distinct()
                                                                        Log.e("marked", uniqueBooks.toString())
                                                                    }
                                                                }
                                                            }

                                                        }
                                                }
                                            }
                                    }
                                }
                            }
                    }
                }

            }
    }
}