package batu.tutorials.readingtime

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import batu.tutorials.readingtime.databinding.CardCellBinding
import java.util.ArrayList

class CardAdapter(books: List<Book>, context: Context) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {
    val mcontext = context
    val myBooks = books
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = CardCellBinding.inflate(from, parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val book = myBooks[position]
        holder.bindBook(book)

        holder.itemView.setOnClickListener {
            val intent = Intent(mcontext, DetailActivity::class.java)
            intent.putExtra("title", book.title)
            intent.putExtra("author", book.author)
            intent.putExtra("pageCount", book.pageCount)
            mcontext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return myBooks.size
    }

    inner class CardViewHolder(private val cardCellBinding: CardCellBinding) : RecyclerView.ViewHolder(cardCellBinding.root) {
        fun bindBook(book: Book) {
            // cardCellBinding.imageViewCover.setImageResource(book.coverImage)
            cardCellBinding.textViewTitle.text = book.title
            cardCellBinding.textViewAuthor.text = book.author[0].toString()
            cardCellBinding.textViewPageCount.text = "Page Count: ${book.pageCount}"
        }
    }


}