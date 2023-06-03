package com.ngengeapps.zicam

import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.ngengeapps.zicam.video.RecordingState

@BindingAdapter("hideShowStillCamera")
fun hideShowStillCamera(button: FloatingActionButton, state: RecordingState) {
    when (state) {
        RecordingState.PAUSE, RecordingState.RESUME -> {
            button.visibility = View.VISIBLE
        }

        else -> {
            button.visibility = View.GONE
        }
    }
}

@BindingAdapter("setBitmapThumb")
fun setBitmapThumb(imageView: ShapeableImageView, bitmap: Bitmap?) {
    bitmap?.let {
        imageView.setImageBitmap(it)
    }
}


@BindingAdapter("hideShowVideoPreview")
fun hideShowVideoPreview(view: View, state: RecordingState) {
    when (state) {
        RecordingState.IDLE, RecordingState.FINALIZE -> {
            view.visibility = View.VISIBLE
        }

        else -> {
            view.visibility = View.INVISIBLE

        }
    }
}


@BindingAdapter("hideShowRecordButton")
fun hideShowRecordButton(button: FloatingActionButton, state: RecordingState) {
    when (state) {
        RecordingState.IDLE, RecordingState.FINALIZE -> {
            button.visibility = View.VISIBLE
        }

        else -> {
            button.visibility = View.GONE
        }
    }
}


@BindingAdapter("toggleIcon")
fun showResumePauseIcon(icon: ImageView, state: RecordingState) {
    if (state == RecordingState.PAUSE) {
        icon.setImageResource(R.drawable.resume)
    } else if (state == RecordingState.RESUME) {
        icon.setImageResource(R.drawable.pause_recording)
    }
}

@BindingAdapter("showHidePauseResumeLayout")
fun showHidePauseResumeLayout(view: View, state: RecordingState) {
    when (state) {
        RecordingState.START, RecordingState.PAUSE, RecordingState.RESUME -> {
            view.visibility = View.VISIBLE
        }

        else -> {
            view.visibility = View.INVISIBLE
        }
    }
}

@BindingAdapter("showHideTimer")
fun showHideTimerChip(chip: Chip, state: RecordingState) {
    when (state) {
        RecordingState.START, RecordingState.PAUSE, RecordingState.RESUME -> {
            chip.visibility = View.VISIBLE
        }

        else -> {
            chip.visibility = View.GONE
        }
    }
}

@BindingAdapter("enableOrDisable")
fun enableOrDisableNextButton(button: Button, permissions: List<String>?) {
    button.isEnabled =
        !(permissions.isNullOrEmpty()
                || !permissions.contains(android.Manifest.permission.CAMERA))
}

@BindingAdapter("bindImageUri")
fun bindImageUri(view: ShapeableImageView, uri: Uri?) {
    uri?.let {
        Toast.makeText(view.context, "The Uri is $uri", Toast.LENGTH_LONG)
            .show()
        view.setImageURI(it)
    }
}

@BindingAdapter("hideOrShowAudioRequestButton")
fun hideOrShowAudioRequestButton(button: Button, permissions: List<String>?) {
    if (!permissions.isNullOrEmpty() && permissions.contains(android.Manifest.permission.RECORD_AUDIO)) {
        button.visibility = View.INVISIBLE
    } else {
        button.visibility = View.VISIBLE
    }
}

