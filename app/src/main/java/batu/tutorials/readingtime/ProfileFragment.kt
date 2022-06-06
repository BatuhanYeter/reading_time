package batu.tutorials.readingtime

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        getStatistics()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun getStatistics() {
        // start loading
        progressBarProfile.visibility = View.VISIBLE

        // get reading time
        val user = FirebaseAuth.getInstance().currentUser
        val database = Firebase.firestore
        database.collection("users").document(user!!.uid).get().addOnSuccessListener {
            document ->
            if (document != null) {
                val data = document.data
                val totalTime = data?.get("total_time")
                Log.e("total", totalTime.toString())
                val convertedTime: Int
                textViewProfileName.text = data!!["name"].toString()
                when (totalTime) {
                    is Long -> {
                        convertedTime = totalTime.toInt()
                        textViewProfileReadingTime.text = timeConvert(convertedTime)
                    }
                    is Double -> {
                        convertedTime = totalTime.toInt()
                        textViewProfileReadingTime.text = timeConvert(convertedTime)
                    }
                    is Int -> {
                        convertedTime = totalTime
                        textViewProfileReadingTime.text = timeConvert(convertedTime)
                    }
                }
            }
        }

        // get total pages and total books already read
        database.collection("users").document(user.uid).collection("finished_reading").get().addOnSuccessListener {
                documents ->
            var total_pages = 0
            var total_number = 0
            for(doc in documents) {
                total_number += 1
                val pages = doc.data["pageCount"]
                    if (pages is Long) total_pages += pages.toInt()
                    else if (pages is Double) total_pages += pages.toInt()
                    else if (pages is Int) total_pages += pages
            }
            textViewProfileTotalPages.text = total_pages.toString()
            textViewProfileTotalBooks.text = total_number.toString()
        }

        // calculate how much more time the user needs to read and the number of books
        database.collection("users").document(user.uid).collection("reading_list").get().addOnSuccessListener {
                documents ->
            var total_pages = 0
            var total_number = 0
            for(doc in documents) {
                total_number += 1
                val pages = doc.data["pageCount"]
                if (pages is Long) total_pages += pages.toInt()
                else if (pages is Double) total_pages += pages.toInt()
                else if (pages is Int) total_pages += pages
            }
            textViewProfileNeededReadingTime.text = timeConvert((total_pages * 300 / 238))
            textViewTotalReadingList.text = total_number.toString()
        }

        // ready to go, end loading
        progressBarProfile.visibility = View.GONE
    }

    private fun timeConvert(time: Int): String {
        return (time / (60 * 24)).toString() + " Day(s), " + time / 60 % 24 + " Hour(s), " + time % 60 + " Minute(s)"
    }
}