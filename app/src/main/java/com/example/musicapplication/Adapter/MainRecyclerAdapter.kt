package com.example.musicapplication.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapplication.Model.MusicFile
import com.example.musicapplication.R
import javax.inject.Inject

class MainRecyclerAdapter @Inject constructor() :
    RecyclerView.Adapter<MainRecyclerAdapter.ViewHolder>(), Filterable {

    var list = ArrayList<MusicFile>()
    var listFull = ArrayList<MusicFile>()
    private var onItemClickListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MainRecyclerAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.songs_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: MainRecyclerAdapter.ViewHolder, position: Int) {
        holder.title.text = list.get(position).title
        holder.layout.setOnClickListener {
            onItemClickListener?.let {
                it(position)
            }
        }
    }

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView
        var layout: LinearLayout

        init {
            title = view.findViewById(R.id.songs_row_title)
            layout = view.findViewById(R.id.songs_row_layout)
        }
    }

    fun updateData(data: ArrayList<MusicFile>) {
        list = data
        listFull = ArrayList(list)
        notifyDataSetChanged()
    }

    private val filter = object: Filter() {
        override fun performFiltering(p0: CharSequence?): FilterResults {
            var filteredList = ArrayList<MusicFile>()
            if (p0 == null || p0.isEmpty()) {
                filteredList.addAll(listFull)
            }else {
                val p = p0.toString().toLowerCase().trim()
                for(mf in listFull) {
                    if (mf.title.toLowerCase().contains(p)) {
                        filteredList.add(mf)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
            list.clear()
            val deneme = p1?.values as List<MusicFile>
            println("boş ${deneme.size}")
            list.addAll(p1?.values as List<MusicFile>)
            notifyDataSetChanged()
        }

    }

    override fun getFilter(): Filter {
        return filter
    }

}