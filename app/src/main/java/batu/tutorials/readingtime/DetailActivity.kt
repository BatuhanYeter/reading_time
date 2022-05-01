package batu.tutorials.readingtime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        textViewTitleDet.text = intent.getStringExtra("title")
        textViewAuthorDet.text = intent.getStringExtra("author")
        textViewPageCountDet.text = intent.getStringExtra("pageCount")
    }
}