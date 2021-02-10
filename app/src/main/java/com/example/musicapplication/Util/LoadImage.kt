package com.example.musicapplication.Util

import android.media.MediaMetadataRetriever
import android.widget.ImageView
import com.bumptech.glide.Glide

fun ImageView.loadImage(path: String) {
    if (path != "") {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val image = retriever.embeddedPicture
        retriever.release()

        Glide.with(context).asBitmap().load(image).into(this)
    }

}