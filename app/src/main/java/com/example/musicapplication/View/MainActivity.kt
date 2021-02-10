package com.example.musicapplication.View

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicapplication.Adapter.MainRecyclerAdapter
import com.example.musicapplication.Model.MusicFile
import com.example.musicapplication.Util.OnSwipeTouchListener
import com.example.musicapplication.R
import com.example.musicapplication.Util.MusicObject
import com.example.musicapplication.Util.loadImage
import com.example.musicapplication.ViewModel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var adapter: MainRecyclerAdapter

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MusicObject.currentSong = -1
        MusicObject.mediaPlayer = null
        MusicObject.list = ArrayList()

        registerReceiver(activityBroadcastReceiver, IntentFilter("activityAction"))

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.updateTime(this)
        permission()

        main_rv.layoutManager = LinearLayoutManager(this)
        adapter.updateData(MusicObject.list)
        main_rv.adapter = adapter
        adapter.setOnItemClickListener {
            MusicObject.currentSong = it
            prepareSong(MusicObject.list.get(it), true)
            playSong()
        }

        bottomSheet()

    }

    private fun observeViewModel() {
        viewModel.time1.observe(this, Observer {
            bottom_sheet_time1.text = it
        })
        viewModel.time2.observe(this, Observer {
            bottom_sheet_time2.text = it
        })
        viewModel.currentTime.observe(this, Observer {
            bottom_sheet_seekBar.progress = it
        })
        viewModel.lastPath.observe(this, Observer {
            if (MusicObject.currentSong == -1) {
                if (it == "non") {
                    if (MusicObject.list.size > 0) {
                        MusicObject.currentSong = 0
                        prepareSong(MusicObject.list.get(0), false)
                    }
                }else {
                    viewModel.findSong(it)
                }
            }else {
                return@Observer
            }
        })
        viewModel.path.observe(this, Observer {
            if (MusicObject.currentSong == -1) {
                MusicObject.currentSong = it
                prepareSong(MusicObject.list.get(it), false)
            }
        })
    }

    private fun prepareSong(mf: MusicFile, play: Boolean) {
        bottom_sheet_song_name.text = mf.title
        bottom_sheet_pause_play.isChecked = play
        bottom_sheet_album.loadImage(mf.path)
        bottom_sheet_song_name2.text = mf.title
        bottom_sheet_pause_play2.isChecked = play
        bottom_sheet_seekBar.max = (mf.duration.toInt() / 1000)

        viewModel.saveDataStore(MusicObject.list.get(MusicObject.currentSong).path)

        viewModel.getTime(mf.duration)
    }

    private fun playSong() {
        viewModel.playSong()
    }

    private fun bottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.isDraggable = false

        bottom_sheet_up_down.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                when (p1) {
                    true -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    false -> bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        })

        bottom_sheet_seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    MusicObject.mediaPlayer?.let {
                        it.seekTo(p1 * 1000)
                        bottom_sheet_seekBar.progress = p1
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        bottom_sheet_up_down.isChecked = true
                        bottom_sheet_pause_play.visibility = View.GONE
                        bottom_sheet_next.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        bottom_sheet_up_down.isChecked = false
                        bottom_sheet_pause_play.visibility = View.VISIBLE
                        bottom_sheet_next.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }
        })

        bottom_sheet_pause_play.setOnClickListener {
            val boolean = bottom_sheet_pause_play.isChecked
            bottom_sheet_pause_play2.isChecked = boolean
            if (!MusicObject.list.isEmpty()) {
                if (boolean) {
                    if (MusicObject.mediaPlayer != null) {
                        viewModel.resumeSong()
                    }else {
                        viewModel.playSong()
                    }
                }else {
                    viewModel.pauseSong()
                }
            }
        }
        bottom_sheet_pause_play2.setOnClickListener {
            val boolean = bottom_sheet_pause_play2.isChecked
            bottom_sheet_pause_play.isChecked = boolean
            if (!MusicObject.list.isEmpty()) {
                if (boolean) {
                    if (MusicObject.mediaPlayer != null) {
                        viewModel.resumeSong()
                    }else {
                        viewModel.playSong()
                    }
                }else {
                    viewModel.pauseSong()
                }
            }
        }

        bottom_sheet_next.setOnClickListener {
            nextSong()
        }
        bottom_sheet_next2.setOnClickListener {
            nextSong()
        }

        bottom_sheet_previous.setOnClickListener {
            previousSong()
        }

        bottomSheet.setOnTouchListener(object : OnSwipeTouchListener(this@MainActivity) {
            override fun onSwipeRight() {
                previousSong()
                super.onSwipeRight()
            }

            override fun onSwipeLeft() {
                nextSong()
                super.onSwipeLeft()
            }
        })

    }

    private fun nextSong() {
        if (MusicObject.list.size == MusicObject.currentSong + 1) {
            MusicObject.currentSong = 0
        } else {
            MusicObject.currentSong++
        }

        if (MusicObject.mediaPlayer != null) {
            prepareSong(MusicObject.list.get(MusicObject.currentSong), true)
            playSong()
        }else {
            prepareSong(MusicObject.list.get(MusicObject.currentSong), false)
            viewModel.stopService()
        }

    }

    private fun previousSong() {
        if (MusicObject.currentSong == 0) {
            MusicObject.currentSong = MusicObject.list.size - 1
        } else {
            MusicObject.currentSong--
        }

        if (MusicObject.mediaPlayer != null) {
            if (MusicObject.mediaPlayer!!.isPlaying) {
                prepareSong(MusicObject.list.get(MusicObject.currentSong), true)
                playSong()
            }else {
                prepareSong(MusicObject.list.get(MusicObject.currentSong), true)
                playSong()
            }
        }else {
            prepareSong(MusicObject.list.get(MusicObject.currentSong), false)
            viewModel.stopService()
        }

    }

    private fun permission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        } else {
            viewModel.getData(adapter)
            observeViewModel()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1 && grantResults.get(0) == PackageManager.PERMISSION_GRANTED) {
            viewModel.getData(adapter)
            observeViewModel()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onDestroy() {
        unregisterReceiver(activityBroadcastReceiver)
        viewModel.stopService()
        super.onDestroy()
    }

    private val activityBroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.getStringExtra("action")
            when (action) {
                "previous" -> previousSong()
                "next" -> nextSong()

                "pausePlay" -> {
                    MusicObject.mediaPlayer?.let {
                        if (it.isPlaying) {
                            bottom_sheet_pause_play.isChecked = false
                            bottom_sheet_pause_play2.isChecked = false
                            viewModel.pauseSong()
                        }else {
                            bottom_sheet_pause_play.isChecked = true
                            bottom_sheet_pause_play2.isChecked = true
                            viewModel.resumeSong()
                        }
                    }
                }
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        val searchView = menu?.findItem(R.id.search)?.actionView as androidx.appcompat.widget.SearchView

        searchView.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                adapter.filter.filter(p0)
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

}