package com.example.musicapplication.Service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.musicapplication.Util.BroadcastClass
import com.example.musicapplication.R
import com.example.musicapplication.Util.MusicObject
import java.io.File

class ForegroundService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onDestroy() {
        stopForeground(true)
        stopSelf()
        MusicObject.mediaPlayer?.stop()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.getStringExtra("action")
        action?.let {
            when (it) {
                "play" -> playSong()
                "pause" -> pauseSong()
                "resume" -> resumeSong()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification(action: String) {
        val act = createAction(action)
        val mf = MusicObject.list.get(MusicObject.currentSong)
        val bitmap = BitmapFactory.decodeFile(mf.path)

        val intent1 = Intent(this, BroadcastClass::class.java).setAction("pausePlay")
        val intent2 = Intent(this, BroadcastClass::class.java).setAction("previous")
        val intent3 = Intent(this, BroadcastClass::class.java).setAction("next")

        val pendingIntent1 = PendingIntent.getBroadcast(this, 1, intent1, 0)
        val pendingIntent2 = PendingIntent.getBroadcast(this, 2, intent2, 0)
        val pendingIntent3 = PendingIntent.getBroadcast(this, 3, intent3, 0)

        val notification = NotificationCompat.Builder(this, "channel1")
            .setContentText(mf.title)
            .addAction(R.drawable.ic_baseline_skip_previous_24, "Previous", pendingIntent2)
            .addAction(act, "PausePlay", pendingIntent1)
            .addAction(R.drawable.ic_baseline_skip_next_24, "Next", pendingIntent3)
            .setSmallIcon(R.drawable.ic_baseline_music_note_24)
            .setLargeIcon(bitmap)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2))
            .build()

        startForeground(1, notification)
    }

    private fun playSong() {
        val uri = Uri.parse(File(MusicObject.list.get(MusicObject.currentSong).path).toString())
        MusicObject.mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
        }
        MusicObject.mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(applicationContext, uri)
            prepare()
            start()
        }

        createNotification("play")

        MusicObject.mediaPlayer?.setOnCompletionListener {
            sendBroadcast(Intent("activityAction").putExtra("action", "next"))
        }

    }
    private fun pauseSong() {
        MusicObject.mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                stopForeground(false)
                createNotification("pause")
            }
        }
    }
    private fun resumeSong() {
        if (MusicObject.mediaPlayer != null) {
            if (!MusicObject.mediaPlayer!!.isPlaying) {
                MusicObject.mediaPlayer!!.start()
                createNotification("resume")
            }
        }
    }

    private fun createAction(action: String): Int {
        var r = 0
        when (action) {
            "play" -> r = R.drawable.ic_baseline_pause_24
            "pause" -> r = R.drawable.ic_baseline_play_arrow_24
            "resume" -> r = R.drawable.ic_baseline_pause_24
        }
        return r
    }

}