package com.ngengeapps.zicam.video

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VideoRecordViewModel : ViewModel() {
    private val _currentState: MutableLiveData<RecordingState> =
        MutableLiveData(RecordingState.IDLE)
    val currentState: LiveData<RecordingState> = _currentState

    private val _currentUri: MutableLiveData<Uri?> = MutableLiveData(null)
    val currentUri: LiveData<Uri?> = _currentUri

    private val _error: MutableLiveData<String?> = MutableLiveData()
    val error: LiveData<String?> = _error


    private fun updateState(videoRecordState: RecordingState) {
        Log.d(VideoRecordViewModel::class.simpleName, "updateState: ${videoRecordState.name}")
        _currentState.value = videoRecordState
    }

    fun setVideoUri(uri: Uri) {
        _currentUri.value = uri
    }

    fun setErrorMessage(message: String?) {
        _error.value = message
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