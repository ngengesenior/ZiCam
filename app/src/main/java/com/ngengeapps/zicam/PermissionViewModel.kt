package com.ngengeapps.zicam

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PermissionViewModel : ViewModel() {
    val result = MutableLiveData(PermissionResult.NOTHING)

    fun sendResult(newResult: PermissionResult) {
        result.value = newResult
    }
}