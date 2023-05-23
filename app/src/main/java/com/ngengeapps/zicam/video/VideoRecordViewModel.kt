package com.ngengeapps.zicam.video

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VideoRecordViewModel : ViewModel() {
    var currentState = MutableLiveData(RecordingState.IDLE)
        private set


    var currentUri: MutableLiveData<Uri?> = MutableLiveData(null)
        private set


    var error: MutableLiveData<String?> = MutableLiveData()
        private set


    private fun updateState(videoRecordState: RecordingState) {
        Log.d(VideoRecordViewModel::class.simpleName, "updateState: ${videoRecordState.name}")
        currentState.value = videoRecordState
    }

    fun setVideoUri(uri: Uri) {
        currentUri.value = uri
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