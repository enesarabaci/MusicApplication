package com.example.musicapplication.Model

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class IntentClass @Inject constructor(val intent: Intent, @ApplicationContext val context: Context) {

    fun playSong() {
        intent.putExtra("action", "play")
        ContextCompat.startForegroundService(context, intent)
    }

    fun pauseSong() {
        intent.putExtra("action", "pause")
        ContextCompat.startForegroundService(context, intent)
    }

    fun resumeSong() {
        intent.putExtra("action", "resume")
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopService() {
        context.stopService(intent)
    }

}