package com.example.musicapplication.ViewModel

import android.content.Context
import android.database.Cursor
import android.os.Looper
import android.provider.MediaStore
import androidx.lifecycle.*
import com.example.musicapplication.Adapter.MainRecyclerAdapter
import com.example.musicapplication.Model.IntentClass
import com.example.musicapplication.Model.MusicFile
import com.example.musicapplication.Model.PreferencesManager
import com.example.musicapplication.Util.MusicObject
import com.example.musicapplication.View.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(@ApplicationContext val context: Context,
            val preferencesManager: PreferencesManager) : ViewModel() {

    @Inject
    lateinit var intentClass: IntentClass

    private val timee1 = MutableLiveData<String>()
    val time1: LiveData<String>
        get() = timee1

    private val timee2 = MutableLiveData<String>()
    val time2: LiveData<String>
        get() = timee2

    private val currentTimee = MutableLiveData<Int>()
    val currentTime: LiveData<Int>
        get() =  currentTimee

    private val pathh = MutableLiveData<Int>()
    val path: LiveData<Int>
        get() = pathh

    val lastPath = preferencesManager.readData.asLiveData()

    fun getData(adapter: MainRecyclerAdapter) {
        val data = ArrayList<MusicFile>()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST
        )

        val cursor: Cursor? =
            context.contentResolver.query(uri, projection, null, null, MediaStore.Audio.Media.TITLE)
        cursor?.let {
            while (it.moveToNext()) {
                val album = it.getString(0)
                val title = it.getString(1)
                val duration = it.getString(2)
                val path = it.getString(3)
                val artist = it.getString(4)

                val musicFile = MusicFile(path, title, artist, album, duration)
                data.add(musicFile)
            }
            cursor.close()
        }
        MusicObject.list = data
        adapter.updateData(MusicObject.list)
    }

    fun getTime(duration: String) {
        var d = duration.toInt()
        d /= 1000
        var seconds = ""
        if (d%60 < 10) {
            seconds = "0${d%60}"
        }else {
            seconds = "${d%60}"
        }
        val minutes = "${d/60}"
        val result = "$minutes:$seconds"
        timee2.value = result
    }

    fun saveDataStore(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            preferencesManager.saveData(path)
        }
    }

    fun findSong(path: String) {
        var i = 0
        for (mf in MusicObject.list) {
            if (path == mf.path) {
                pathh.value = i
                break
            }
            i++
        }
        if (i == MusicObject.list.size-1) {
            pathh.value = 0
        }
    }

    fun updateTime(mainActivity: MainActivity) {
        val handler = android.os.Handler(Looper.getMainLooper())

        mainActivity.runOnUiThread(object: Runnable{
            override fun run() {
                MusicObject.mediaPlayer?.let {
                    var d = it.currentPosition
                    d /= 1000
                    currentTimee.value = d
                    var seconds = ""
                    if (d%60 < 10) {
                        seconds = "0${d%60}"
                    }else {
                        seconds = "${d%60}"
                    }
                    val minutes = "${d/60}"
                    val result = "$minutes:$seconds"
                    timee1.value = result
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    fun playSong() {
        intentClass.playSong()
    }

    fun resumeSong() {
        intentClass.resumeSong()
    }

    fun pauseSong() {
        intentClass.pauseSong()
    }

    fun stopService() {
        intentClass.stopService()
    }

}