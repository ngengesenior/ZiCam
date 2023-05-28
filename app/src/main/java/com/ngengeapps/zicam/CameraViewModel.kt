package com.ngengeapps.zicam

import android.util.Log
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {
    var lensFacing: MutableLiveData<Int> = MutableLiveData(LENS_FACING_BACK)
        private set

    fun flipCamera() {
        if (lensFacing.value == LENS_FACING_BACK) {
            lensFacing.value = LENS_FACING_FRONT
        } else {
            lensFacing.value = LENS_FACING_BACK
        }

        Log.d(CameraViewModel::class.simpleName, "New CAMERA ${lensFacing.value}")
    }
}