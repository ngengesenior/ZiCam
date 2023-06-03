package com.ngengeapps.zicam

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.provider.MediaStore
import android.provider.MediaStore.Video.Thumbnails
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.video.VideoRecordEvent.Finalize
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_NONE
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_SOURCE_INACTIVE
import androidx.camera.video.VideoRecordEvent.Finalize.VideoRecordError
import androidx.camera.view.LifecycleCameraController
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

object Utils {

    private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
    fun getErrorMessage(@VideoRecordError code: Int): String {
        when (code) {
            ERROR_NONE -> {
                return ""
            }

            ERROR_SOURCE_INACTIVE -> {
                return "Camera closed during recording"
            }

            else -> {
                return errorToString(code)
                    .replace("_", " ")
                    .lowercase(Locale.getDefault())
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
        }
    }


    private fun errorToString(@VideoRecordError error: Int): String {
        when (error) {
            ERROR_NONE -> return "ERROR_NONE"
            Finalize.ERROR_UNKNOWN -> return "ERROR_UNKNOWN"
            Finalize.ERROR_FILE_SIZE_LIMIT_REACHED -> return "ERROR_FILE_SIZE_LIMIT_REACHED"
            Finalize.ERROR_INSUFFICIENT_STORAGE -> return "ERROR_INSUFFICIENT_STORAGE"
            Finalize.ERROR_INVALID_OUTPUT_OPTIONS -> return "ERROR_INVALID_OUTPUT_OPTIONS"
            Finalize.ERROR_ENCODING_FAILED -> return "ERROR_ENCODING_FAILED"
            Finalize.ERROR_RECORDER_ERROR -> return "ERROR_RECORDER_ERROR"
            Finalize.ERROR_NO_VALID_DATA -> return "ERROR_NO_VALID_DATA"
            ERROR_SOURCE_INACTIVE -> return "ERROR_SOURCE_INACTIVE"
            Finalize.ERROR_DURATION_LIMIT_REACHED -> return "ERROR_DURATION_LIMIT_REACHED"
        }

        // Should never reach here, but just in case...
        return "Unknown($error)"
    }

    fun createContentValues(isImage: Boolean = true): ContentValues {
        val mimeType = if (isImage) "image/jpeg" else "video/mp4"
        val app = "ZiCam"
        val relativePath = if (isImage) "Pictures/$app" else "Movies/$app"
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        return ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            }

        }

    }

    fun hasBackCamera(cameraController: LifecycleCameraController): Boolean {
        return cameraController.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
    }

    fun hasFrontCamera(cameraController: LifecycleCameraController): Boolean {
        return cameraController.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
    }

    fun createVideoThumbnail(uri: Uri): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ThumbnailUtils.createVideoThumbnail(
                File(uri.path), Size(240, 240),
                CancellationSignal()
            )
        } else {
            ThumbnailUtils.createVideoThumbnail(uri.path!!, Thumbnails.MINI_KIND)
        }
    }

    fun createVideoThumbnail(uri: Uri, context: Context): Bitmap? {
        val file = getVideoFile(uri, context)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            file?.let {
                ThumbnailUtils.createVideoThumbnail(
                    it, Size(240, 240),
                    CancellationSignal()
                )
            }
        } else {
            ThumbnailUtils.createVideoThumbnail(uri.path!!, Thumbnails.MINI_KIND)
        }
    }

    fun getVideoFile(uri: Uri, context: Context): File? {
        var path: String? = null
        var file: File? = null
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            path = cursor.getString(columnIndex)
            cursor.close()
        }

        if (path != null) {
            file = File(path)
        }

        return file
    }
}