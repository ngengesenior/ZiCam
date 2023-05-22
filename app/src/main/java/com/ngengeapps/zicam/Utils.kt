package com.ngengeapps.zicam

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.camera.video.VideoRecordEvent.Finalize
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_NONE
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_SOURCE_INACTIVE
import androidx.camera.video.VideoRecordEvent.Finalize.VideoRecordError
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
}