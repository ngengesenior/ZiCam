package com.ngengeapps.zicam.video

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.camera.view.video.ExperimentalVideo
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ngengeapps.zicam.Utils
import com.ngengeapps.zicam.databinding.FragmentVideoCameraBinding

@ExperimentalVideo
class VideoCameraFragment : Fragment() {
    private var recording: Recording? = null
    private val cameraController: LifecycleCameraController by lazy {
        LifecycleCameraController(requireContext())
    }
    private lateinit var recordVideoButton: FloatingActionButton
    private lateinit var pauseResumeRecordingVideoButton: ImageView
    private lateinit var stopVideoRecordingButton: ImageView
    private lateinit var flipCameraButton: FloatingActionButton
    private lateinit var captureImageButton: FloatingActionButton

    //private lateinit var imageVideoPreview: ShapeableImageView
    private lateinit var pauseResumeLayout: LinearLayout
    private lateinit var recordTimer: Chip
    private lateinit var previewView: PreviewView
    private val videoViewModel: VideoRecordViewModel by viewModels()

    private var _binding: FragmentVideoCameraBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun initViews() {
        previewView = binding.previewView
        previewView.controller = cameraController
        captureImageButton = binding.stillCameraButton
        recordTimer = binding.recordTimer
        recordVideoButton = binding.recordButton
        flipCameraButton = binding.flipCameraButton
        stopVideoRecordingButton = binding.stopRecordingButton
        pauseResumeRecordingVideoButton = binding.pauseRecordingButton
        pauseResumeLayout = binding.pauseStopRecordLayout

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoCameraBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        cameraController.bindToLifecycle(this)
        enableVideoUseCase()
        initViews()
        binding.videoEventViewModel = videoViewModel
        recordVideoButton.setOnClickListener {
            recordVideo()
        }
        stopVideoRecordingButton.setOnClickListener {
            stopRecording()
        }
        flipCameraButton.setOnClickListener {
            try {
                flipCamera()
            } catch (ex: Exception) {

            }
        }
        return binding.root
    }

    private fun flipCamera() {
        if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }


    @ExperimentalVideo
    private fun enableVideoUseCase() {
        cameraController.setEnabledUseCases(CameraController.VIDEO_CAPTURE)
    }

    @ExperimentalVideo
    private fun recordVideo() {
        if (!cameraController.isRecording) {
            val outputOptions = MediaStoreOutputOptions.Builder(
                requireContext().applicationContext.contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
                .setContentValues(Utils.createContentValues(isImage = false))
                .build()


            recording = cameraController.startRecording(
                outputOptions,
                AudioConfig.AUDIO_DISABLED,
                ContextCompat.getMainExecutor(requireActivity())
            ) { event ->

                updateEvent(event)
            }


        } else return


    }


    private fun updateEvent(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Start -> {
                videoViewModel.onStart()
            }

            is VideoRecordEvent.Pause -> {
                videoViewModel.onPause()
            }

            is VideoRecordEvent.Resume -> {
                videoViewModel.onResume()
            }

            is VideoRecordEvent.Finalize -> {
                val options = event.outputOptions
                val error = event.error
                val errorMessage = Utils.getErrorMessage(error)
                videoViewModel.setErrorMessage(errorMessage)
                when (error) {
                    /**
                     * The recording succeeded without an error
                     */

                    VideoRecordEvent.Finalize.ERROR_NONE -> {
                        val uri = event.outputResults.outputUri
                        videoViewModel.setVideoUri(uri)
                    }
                    /**
                     * A properly constructed file is generated when these errors occur
                     */
                    VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED, VideoRecordEvent.Finalize.ERROR_SOURCE_INACTIVE,
                    VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED -> {
                        val uri: Uri = event.outputResults.outputUri
                        videoViewModel.setVideoUri(uri)
                    }

                    /**
                     * A file might or might not be generated depending whether the storage
                     * became full before recording started or it became full during recording.
                     */
                    VideoRecordEvent.Finalize.ERROR_INSUFFICIENT_STORAGE -> {
                        val uri: Uri = event.outputResults.outputUri
                        uri.let {
                            videoViewModel.setVideoUri(it)
                        }

                    }

                    /**
                     * A file generated from these errors is not well formed and so,
                     * delete the generated file
                     */
                    VideoRecordEvent.Finalize.ERROR_UNKNOWN, VideoRecordEvent.Finalize.ERROR_ENCODING_FAILED,
                    VideoRecordEvent.Finalize.ERROR_RECORDER_ERROR, VideoRecordEvent.Finalize.ERROR_NO_VALID_DATA,
                    VideoRecordEvent.Finalize.ERROR_INVALID_OUTPUT_OPTIONS -> {
                        if (options is MediaStoreOutputOptions) {
                            /*val uri: Uri = event.outputResults.outputUri
                            uri?.let {
                                requireContext().contentResolver.delete(it, null, null)
                            }*/

                        }

                    }


                }

                videoViewModel.resetEvent()
            }
        }
    }

    private fun stopRecording() {
        recording?.stop()
    }

}