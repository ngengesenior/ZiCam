package com.ngengeapps.zicam.permissions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ngengeapps.zicam.PermissionResult

class PermissionViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val result = MutableLiveData(PermissionResult.NOTHING)
    var permissions: MutableLiveData<MutableSet<String>> = MutableLiveData(mutableSetOf())
        private set

    fun sendResult(newResult: PermissionResult) {
        result.value = newResult
    }

    fun addPermission(permission: String) {
        permissions.value?.add(permission)
    }
}