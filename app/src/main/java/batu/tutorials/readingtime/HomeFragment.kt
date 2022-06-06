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
import batu.tutorials.readingtime.databinding.ActivityMainBinding
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_recommendation.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONException
import java.util.ArrayList

class HomeFragment : Fragment() {
    private lateinit var binding: ActivityMainBinding
    private var mRequestQueue: RequestQueue? = null
    private var bookInfoArrayList: ArrayList<BookInfo>? = null

    // private lateinit var editTextSearch: EditText
    // private lateinit var buttonSearch: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)

        // for the beginning, load the most read books
        getMostReadBooks()

        // initializing on click listener for our button.
        imageButtonSearch.setOnClickListener(View.OnClickListener {
            progressBar.visibility = View.VISIBLE

            // checking if our edittext field is empty or not.
            if (editTextSearchBooks.text.toString().isEmpty()) {
                editTextSearchBooks.error = "Please enter search query"
                progressBar.visibility = View.GONE
                return@OnClickListener
            }
            // if the search query is not empty then we are
            // calling get book info method to load all
            // the books from the API.
            getBooksInfo(editTextSearchBooks.text.toString())
        })


        super.onViewCreated(view, savedInstanceState)
    }

    private fun getBooksInfo(query: String) {
        // creating a new array list.
        bookInfoArrayList = ArrayList<BookInfo>()

        // below line is use to initialize
        // the variable for our request queue.
        mRequestQueue = Volley.newRequestQueue(this.context)

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue!!.cache.clear()

        val apiKey = "AIzaSyAZLJbLIg5C_BCwYprOH8yofjAZt8LKbZY"
        // below is the url for getting data from API in json format.
        val url = "https://www.googleapis.com/books/v1/volumes?q=$query&printType=books&key=$apiKey"

        // below line we are creating a new request queue.
        val queue = Volley.newRequestQueue(this.context)

        // below line is use to make json object request inside that we
        // are passing url, get method and getting json object. .
        val booksRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                progressBar.visibility = View.GONE
                // inside on response method we are extracting all our json data.
                try {
                    val itemsArray = response.getJSONArray("items")
                    for (i in 0 until itemsArray.length()) {
                        val itemsObj = itemsArray.getJSONObject(i)
                        val volumeObj = itemsObj.getJSONObject("volumeInfo")
                        val title = volumeObj.optString("title")
                        val id = itemsObj.getString("id")
                        Log.e("id", id.toString())
                        val subtitle = volumeObj.optString("subtitle")
                        val authorsArray = volumeObj.getJSONArray("authors")
                        val publisher = volumeObj.optString("publisher")
                        val publishedDate = volumeObj.optString("publishedDate")
                        val description = volumeObj.optString("description")
                        val pageCount = volumeObj.optInt("pageCount")
                        val imageLinks = volumeObj.optJSONObject("imageLinks")

                        var thumbnail = ""
                        if (imageLinks == null) thumbnail = ""
                        else {
                            thumbnail = imageLinks.optString("thumbnail")
                            thumbnail = thumbnail.replace("http", "https")
                        }

                        Log.e("authors get request", authorsArray.toString())
                        val authorsArrayList = ArrayList<String>()

                        if (authorsArray.length() != 0) {
                            for (i in 0 until authorsArray.length()) {
                                authorsArrayList.add(authorsArray.optString(i))
                            }
                        }
                        Log.e("authors arraylist", authorsArrayList.toString())
                        // after extracting all the data we are
                        // saving this data in our modal class.
                        val bookInfo = BookInfo(
                            title,
                            id,
                            subtitle,
                            authorsArrayList,
                            publisher,
                            publishedDate,
                            description,
                            pageCount,
                            thumbnail
                        )
                        // below line is use to pass our modal
                        // class in our array list.
                        bookInfoArrayList!!.add(bookInfo)

                    }
                    // below line is use to pass our
                    // array list in adapter class.
                    val adapter = BookAdapter(bookInfoArrayList!!, this.requireContext())

                    // below line is use to add linear layout
                    // manager for our recycler view.
                    val linearLayoutManager =
                        LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
                    val mRecyclerView = recyclerViewHomeMostRead

                    // in below line we are setting layout manager and
                    // adapter to our recycler view.
                    mRecyclerView.layoutManager = linearLayoutManager
                    mRecyclerView.adapter = adapter
                } catch (e: JSONException) {
                    e.printStackTrace()
                    // displaying a toast message when we get any error from API
                    Toast.makeText(this.context, "No Data Found$e", Toast.LENGTH_SHORT).show()
                }
            }) { error -> // also displaying error message in toast.
            Toast.makeText(this.context, "Error found is $error", Toast.LENGTH_SHORT).show()
        }
        // at last we are adding our json object
        // request in our request queue.
        queue.add(booksRequest)
    }

    private fun getMostReadBooks() {
        // loading - starting
        progressBar.visibility = View.VISIBLE

        // creating a new array list.
        bookInfoArrayList = ArrayList<BookInfo>()
        val user = FirebaseAuth.getInstance().currentUser
        val database = Firebase.firestore

        database.collection("books").orderBy("times", Query.Direction.DESCENDING).get()
            .addOnSuccessListener {
                datalist ->
                if(datalist != null) {
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
                    }
                }
                val adapter = BookAdapter(bookInfoArrayList!!, context!!)
                val linearLayoutManager =
                    LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                recyclerViewHomeMostRead.layoutManager = linearLayoutManager
                recyclerViewHomeMostRead.adapter = adapter
            }

        // loading - done
        progressBar.visibility = View.INVISIBLE
    }

}