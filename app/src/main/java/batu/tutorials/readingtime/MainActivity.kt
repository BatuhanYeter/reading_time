package batu.tutorials.readingtime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import batu.tutorials.readingtime.databinding.ActivityMainBinding
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_loading.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    private lateinit var binding: ActivityMainBinding
    private var mRequestQueue: RequestQueue? = null
    private var bookInfoArrayList: ArrayList<BookInfo>? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var editTextSearch: EditText
    private lateinit var buttonSearch: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // initializing our views.
        progressBar = findViewById(R.id.progressBar)
        editTextSearch = findViewById(R.id.editTextSearchBooks)
        buttonSearch = findViewById(R.id.imageButtonSearch)


        // initializing on click listener for our button.
        buttonSearch.setOnClickListener(View.OnClickListener {
            progressBar.visibility = View.VISIBLE

            // checking if our edittext field is empty or not.
            if (editTextSearch.text.toString().isEmpty()) {
                editTextSearch.error = "Please enter search query"
                progressBar.visibility = View.GONE
                return@OnClickListener
            }
            // if the search query is not empty then we are
            // calling get book info method to load all
            // the books from the API.
            getBooksInfo(editTextSearch.text.toString())
        })


        buttonSignOut.setOnClickListener {
            signOut()
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(applicationContext, 2)
            adapter = CardAdapter(bookList, this@MainActivity)
        }
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            getBooks()
        } else {
            createSignInIntent()
        }
    }

    private fun getBooksInfo(query: String) {

        // creating a new array list.
        bookInfoArrayList = ArrayList<BookInfo>()

        // below line is use to initialize
        // the variable for our request queue.
        mRequestQueue = Volley.newRequestQueue(this@MainActivity)

        // below line is use to clear cache this
        // will be use when our data is being updated.
        mRequestQueue!!.cache.clear()

        // below is the url for getting data from API in json format.
        val url = "https://www.googleapis.com/books/v1/volumes?q=$query"

        // below line we are creating a new request queue.
        val queue = Volley.newRequestQueue(this@MainActivity)

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
                        if(imageLinks == null) thumbnail = ""
                        else thumbnail = imageLinks.optString("thumbnail")
                        Log.e("thumbnail", thumbnail)

                        val previewLink = volumeObj.optString("previewLink")
                        val infoLink = volumeObj.optString("infoLink")
                        val saleInfoObj = itemsObj.optJSONObject("saleInfo")
                        val buyLink = saleInfoObj.optString("buyLink")
                        val authorsArrayList = ArrayList<String>()
                        if (authorsArray.length() != 0) {
                            for (j in 0 until authorsArray.length()) {
                                authorsArrayList.add(authorsArray.optString(i))
                            }
                        }
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
                            thumbnail,
                            previewLink,
                            infoLink,
                            buyLink
                        )
                        // below line is use to pass our modal
                        // class in our array list.
                        bookInfoArrayList!!.add(bookInfo)

                    }
                    // below line is use to pass our
                    // array list in adapter class.
                    val adapter = BookAdapter(bookInfoArrayList!!, this@MainActivity)

                    // below line is use to add linear layout
                    // manager for our recycler view.
                    val linearLayoutManager =
                        LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
                    val mRecyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView

                    // in below line we are setting layout manager and
                    // adapter to our recycler view.
                    mRecyclerView.layoutManager = linearLayoutManager
                    mRecyclerView.adapter = adapter
                } catch (e: JSONException) {
                    e.printStackTrace()
                    // displaying a toast message when we get any error from API
                    Toast.makeText(this@MainActivity, "No Data Found$e", Toast.LENGTH_SHORT).show()
                }
            }) { error -> // also displaying error message in toast.
            Toast.makeText(this@MainActivity, "Error found is $error", Toast.LENGTH_SHORT).show()
        }
        // at last we are adding our json object
        // request in our request queue.
        queue.add(booksRequest)
    }

    private fun getBooks() {
        // loading - starting
        progressBar.visibility = View.VISIBLE

        val apiKey = "AIzaSyAZLJbLIg5C_BCwYprOH8yofjAZt8LKbZY"
        var searchKeywords = "sherlock holmes"
        var splitKeywords = searchKeywords.replace(" ", "+")

        val baseUrl =
            "https://www.googleapis.com/books/v1/volumes?q=flowers+inauthor:keyes&key=$apiKey"
        val searchUrl = "https://www.googleapis.com/books/v1/volumes?q=$splitKeywords"

        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)

        // jsonRequest
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, baseUrl, null,
            { response ->
                val items = response.getJSONArray("items")
                val gson = GsonBuilder().registerTypeAdapter(Book::class.java, BookDeserializer()).create()
                for (i in 0 until items.length()) {
                    val book = gson.fromJson(items.get(i).toString(), Book::class.java)
                    bookList.add(book)
                    Log.e("Book -> ${i+1}", book.title)
                }
                Log.e("Size in for", bookList.size.toString())
            },
            { error ->
                // TODO: Handle error
            }
        )

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest)
        // loading - done
        progressBar.visibility = View.INVISIBLE
    }

    private fun createSignInIntent() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.TwitterBuilder().build(),
            AuthUI.IdpConfig.AnonymousBuilder().build()
        )


        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.book)
            .setIsSmartLockEnabled(false)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            val database = Firebase.firestore
            database.collection("users").document(user!!.uid).addSnapshotListener { value, error ->
                if (value!!.exists()) {
                    Log.e("User", "Already exists!")
                    Toast.makeText(this, "Welcome, ${user.displayName}", Toast.LENGTH_SHORT)
                        .show()

                } else {
                    val userData = hashMapOf(
                        "name" to user.displayName,
                        "email" to user.email,
                        "total_time" to 1
                    )
                    database.collection("users").document(user.uid).set(userData)
                        .addOnSuccessListener {
                            Log.d(
                                "Success",
                                "DocumentSnapshot successfully written!"
                            )
                        }
                        .addOnFailureListener { e -> Log.w("Error", "Error writing document", e) }
                }
            }


        } else {
            Toast.makeText(this, "Sign in failed! ${response?.error}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                createSignInIntent()
            }
    }
}