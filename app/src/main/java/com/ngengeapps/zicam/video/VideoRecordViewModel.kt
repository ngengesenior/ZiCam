package com.ngengeapps.zicam.video

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ngengeapps.zicam.Utils

class VideoRecordViewModel(private val app: Application) : AndroidViewModel(app) {
    var currentState = MutableLiveData(RecordingState.IDLE)
        private set


    var currentUri: MutableLiveData<Uri?> = MutableLiveData(null)
        private set

    var videoThumb: MutableLiveData<Bitmap?> = MutableLiveData(null)
        private set


    var error: MutableLiveData<String?> = MutableLiveData()
        private set


    private fun updateState(videoRecordState: RecordingState) {
        Log.d(VideoRecordViewModel::class.simpleName, "updateState: ${videoRecordState.name}")
        currentState.value = videoRecordState
    }

    fun setVideoUri(uri: Uri) {
        currentUri.value = uri
        updateThumb(uri)
    }

    private fun updateThumb(uri: Uri) {
        val bmp = Utils.createVideoThumbnail(uri, app.applicationContext)
        Log.d(VideoRecordViewModel::class.simpleName, "Bitmap is ${bmp.toString()}")
        bmp?.let {
            videoThumb.value = it
        }

    }

    fun setErrorMessage(message: String?) {
        error.value = message
    }

    fun onPause() {
        updateState(RecordingState.PAUSE)
    }

    fun onResume() {
        updateState(RecordingState.RESUME)
    }

    fun onStart() {
        updateState(RecordingState.START)
    }

    fun resetEvent() {
        updateState(RecordingState.IDLE)
    }


}