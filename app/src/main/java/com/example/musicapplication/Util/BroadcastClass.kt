package com.example.musicapplication.Util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BroadcastClass : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        val action = p1?.action
        p0?.sendBroadcast(Intent("activityAction").putExtra("action", action))
    }
}