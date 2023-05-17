package com.ngengeapps.zicam

interface CameraPermissionListener {
    fun sendResult(result: PermissionResult)
}

enum class PermissionResult {
    GRANTED,
    DENIED,
    NOTHING
}