package com.ngengeapps.zicam.video

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ngengeapps.zicam.R

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
