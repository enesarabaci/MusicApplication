package com.example.musicapplication.Util

import android.media.MediaPlayer
import com.example.musicapplication.Model.MusicFile

object MusicObject {
    var list = ArrayList<MusicFile>()
    var currentSong = -1
    var mediaPlayer: MediaPlayer? = null
}