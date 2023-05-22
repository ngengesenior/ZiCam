package com.ngengeapps.zicam


import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_DURATION_LIMIT_REACHED
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_ENCODING_FAILED
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_INSUFFICIENT_STORAGE
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_INVALID_OUTPUT_OPTIONS
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_NONE
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_NO_VALID_DATA
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_RECORDER_ERROR
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_SOURCE_INACTIVE
import androidx.camera.video.VideoRecordEvent.Finalize.ERROR_UNKNOWN
import androidx.camera.view.CameraController.IMAGE_ANALYSIS
import androidx.camera.view.CameraController.IMAGE_CAPTURE
import androidx.camera.view.CameraController.VIDEO_CAPTURE
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.imageview.ShapeableImageView
import com.ngengeapps.zicam.databinding.FragmentCameraBinding


class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var cameraController: LifecycleCameraController
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private val permissionViewModel: PermissionViewModel by activityViewModels()
    private val TAG = CameraFragment::class.simpleName
    private var recording: Recording? = null
    private var videoCapture: VideoCapture<Recorder>? = null

    private lateinit var recordVideoButton: View
    private lateinit var pauseRecordingVideoButton: ImageView
    private lateinit var stopVideoRecordingButton: ImageView
    private lateinit var flipCameraButton: ImageView
    private lateinit var captureImageButton: View
    private lateinit var imageVideoPreview: ShapeableImageView
    private lateinit var photoTextView: TextView
    private lateinit var videoTextView: TextView


    private fun initViews() {
        recordVideoButton = binding.recordVideoButton
        flipCameraButton = binding.flipCameraButton
        captureImageButton = binding.capturePhotoButton
        stopVideoRecordingButton = binding.stopRecordingButton
        pauseRecordingVideoButton = binding.pauseRecordingButton
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        initViews()

        permissionViewModel.result.observe(viewLifecycleOwner) { result ->
            if (result == PermissionResult.GRANTED) {
                startCamera()
            }


        }
        imageVideoPreview = binding.imageVideoPreview
        binding.capturePhotoButton.setOnClickListener {
            takePicture()
        }
        return binding.root

    }


    override fun onDestroyView() {
        super.onDestroyView()
        cameraController.unbind()
        _binding = null
    }

    /**
     * Switch to video recording
     */
    @androidx.camera.view.video.ExperimentalVideo
    private fun enableVideoUseCase() {
        cameraController.setEnabledUseCases(VIDEO_CAPTURE)
    }

    private fun enableImageCaptureUseCase() {
        cameraController.setEnabledUseCases(IMAGE_CAPTURE or IMAGE_ANALYSIS)
    }
    //cameraController.setEnabledUseCases(IMAGE_CAPTURE|IMAGE_ANALYSIS);

    private fun startCamera() {
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        val viewFinder = binding.viewFinder
        cameraController = LifecycleCameraController(requireContext())
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = cameraSelector
        viewFinder.controller = cameraController

    }


    private fun updateUI(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Finalize -> {
                val options = event.outputOptions
                val error = event.error
                val errorMessage = Utils.getErrorMessage(error)
                when (error) {
                    /**
                     * The recording succeeded without an error
                     */

                    ERROR_NONE -> {
                        val uri = event.outputResults.outputUri
                    }
                    /**
                     * A properly constructed file is generated when these errors occur
                     */
                    ERROR_FILE_SIZE_LIMIT_REACHED, ERROR_SOURCE_INACTIVE,
                    ERROR_DURATION_LIMIT_REACHED -> {
                        pauseRecordingVideoButton.visibility = View.INVISIBLE
                        val uri: Uri = event.outputResults.outputUri
                    }

                    /**
                     * A file might or might not be generated depending whether the storage
                     * became full before recording started or it became full during recording.
                     */
                    ERROR_INSUFFICIENT_STORAGE -> {

                    }

                    /**
                     * A file generated from these errors is not well formed and so,
                     * delete the generated file
                     */
                    ERROR_UNKNOWN, ERROR_ENCODING_FAILED,
                    ERROR_RECORDER_ERROR, ERROR_NO_VALID_DATA,
                    ERROR_INVALID_OUTPUT_OPTIONS -> {
                        if (options is MediaStoreOutputOptions) {
                            val uri: Uri = event.outputResults.outputUri
                            requireContext().contentResolver.delete(uri, null, null)
                        }

                    }


                }
            }

            is VideoRecordEvent.Pause -> {

            }

            is VideoRecordEvent.Resume -> {

            }
        }
    }

    @androidx.camera.view.video.ExperimentalVideo
    private fun recordVideo() {
        if (!cameraController.isRecording) {
            val outputOptions = MediaStoreOutputOptions.Builder(
                requireContext().contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
                .setContentValues(Utils.createContentValues(isImage = false))
                .build()

            recording = cameraController.startRecording(
                outputOptions,
                AudioConfig.AUDIO_DISABLED,
                ContextCompat.getMainExecutor(requireContext())
            ) { event ->
                updateUI(event)
            }

        }


    }

    private fun takePicture() {
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Utils.createContentValues()
        )
            .build()
        cameraController.takePicture(outputFileOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(
                        requireContext(),
                        "Image captured ${outputFileResults.savedUri}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    imageVideoPreview.visibility = View.VISIBLE
                    imageVideoPreview.setImageURI(outputFileResults.savedUri)

                }

                override fun onError(exception: ImageCaptureException) {

                }

            }
        )
    }


    companion object {
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
    }
}