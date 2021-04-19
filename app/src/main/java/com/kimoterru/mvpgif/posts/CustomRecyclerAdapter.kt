package com.kimoterru.mvpgif.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kimoterru.mvpgif.api.GifItem
import com.kimoterru.mvpgif.R

class CustomRecyclerAdapter(val data: ArrayList<GifItem>, val listener: (GifItem) -> Unit) : RecyclerView.Adapter<CustomRecyclerAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var image: ImageView
        init {
            image = itemView.findViewById(R.id.thumbnail)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_stolb, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = data[position]
        Glide.with(holder.itemView).load(item.images.downSized.url).into(holder.image)
        holder.itemView.setOnClickListener {
            listener.invoke(item)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun addData(newData: List<GifItem>) {
        data.addAll(newData)
        notifyDataSetChanged()
    }
    fun clearData() {
        data.clear()
        notifyDataSetChanged()
    }
}