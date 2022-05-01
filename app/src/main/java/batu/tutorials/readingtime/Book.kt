package batu.tutorials.readingtime

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.android.parcel.Parcelize
import java.lang.reflect.Type

var bookList = mutableListOf<Book>()

@Parcelize
data class Book(
    val title: String,
    val author: String,
    val thumbnail: String,
    val description: String,
    val publisher: String,
    val publishedDate: String,
    val pageCount: Int,
    val categories: ArrayList<String>
) : Parcelable {}

class BookDeserializer : JsonDeserializer<Book> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Book {
        json as JsonObject
        val volumeInfo = json.get("volumeInfo").asJsonObject
        val title =
            volumeInfo.get("title")
                .toString()
        val author =
            volumeInfo.get("authors").asJsonArray.get(0).toString()

        val pageCount = volumeInfo.get("pageCount").asInt

        var publisher = ""
        if (volumeInfo.get("publisher") == null) publisher = ""
        else publisher = volumeInfo.get("publisher").toString()

        var publishedDate = ""
        if(volumeInfo.get("publishedDate") == null) publishedDate = ""
        else publishedDate = volumeInfo.get("publishedDate").toString()


        var description = ""
        if (volumeInfo.get("description") == null) description = ""
        else description = volumeInfo.get("description").toString()

        var thumbnail = ""

        return Book(title, author, "", description, publisher, publishedDate , pageCount, arrayListOf())

    }

}