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
import com.google.gson.JsonArray
import kotlinx.android.synthetic.main.fragment_finished_list.*
import kotlinx.android.synthetic.main.fragment_reading_list.*
import kotlin.collections.ArrayList

class FinishedListFragment : Fragment() {
    private var bookInfoArrayList: ArrayList<BookInfo>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_finished_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getBooksInfo()
        super.onViewCreated(view, savedInstanceState)
    }
    private fun getBooksInfo() {
        progressBarFinishedReading.visibility = View.VISIBLE
        // creating a new array list.
        bookInfoArrayList = ArrayList<BookInfo>()
        val user = FirebaseAuth.getInstance().currentUser
        val database = Firebase.firestore
        val docRef = database.collection("users").document(user!!.uid).collection("finished_reading")
        docRef.get().addOnSuccessListener { documents ->
            // Log.e("docs", documents.toString())
            if(documents != null) {
                for (document in documents) {
                    // Log.e("doc", "${document.id} => ${document.data}")
                    // Log.e("title", "${document.data["title"]}")
                    val data = document.data
                    val title = data["title"].toString()
                    val id = data["id"].toString()
                    val subtitle = data["subtitle"].toString()
                    val authorsArray = data["authors"] as ArrayList<*>
                    val authorsArrayList = ArrayList<String>()
                    for (i in authorsArray) {
                        authorsArrayList.add(i.toString())
                    }
                    Log.e("finished reading =>", authorsArrayList.toString())
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
                    // below line is use to pass our modal
                    // class in our array list.
                    bookInfoArrayList!!.add(bookInfo)
                }

                // below line is use to pass our
                // array list in adapter class.
                val adapter = BookAdapter(bookInfoArrayList!!, context!!)

                // below line is use to add linear layout
                // manager for our recycler view.
                val linearLayoutManager =
                    LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)

                // in below line we are setting layout manager and
                // adapter to our recycler view.
                recyclerViewFinishedList.layoutManager = linearLayoutManager
                recyclerViewFinishedList.adapter = adapter

                progressBarFinishedReading.visibility = View.GONE
            } else {
                Toast.makeText(this.context, "There is no book in this list!", Toast.LENGTH_SHORT).show()
            }

        }.addOnFailureListener { exception ->
            Log.e("failed", "Error getting documents: ", exception)
        }
    }
}