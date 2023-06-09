package com.ngengeapps.zicam

import android.net.Uri
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CameraViewModel : ViewModel() {
    var lensFacing: MutableLiveData<Int> = MutableLiveData(LENS_FACING_BACK)
        private set
    var imageUri: MutableLiveData<Uri> = MutableLiveData(Uri.EMPTY)
        private set

    fun flipCamera() {
        if (lensFacing.value == LENS_FACING_BACK) {
            lensFacing.value = LENS_FACING_FRONT
        } else {
            lensFacing.value = LENS_FACING_BACK
        }

    }

    fun setImageUri(uri: Uri?) {
        uri?.let {
            imageUri.value = it
        }
    }
}