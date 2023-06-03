package com.ngengeapps.zicam.video

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
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
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.ngengeapps.zicam.CameraFragmentDirections
import com.ngengeapps.zicam.CameraViewModel
import com.ngengeapps.zicam.OnDoubleClickListener
import com.ngengeapps.zicam.R
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
    private lateinit var thumbPreview: ShapeableImageView
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private val videoViewModel: VideoRecordViewModel by viewModels()

    private var _binding: FragmentVideoCameraBinding? = null
    private val navController: NavController by lazy { findNavController() }
    private val cameraViewModel: CameraViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun initViews() {
        thumbPreview = binding.imageVideoPreview
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

    private fun startCamera() {
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        cameraController.cameraSelector = cameraSelector
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoCameraBinding.inflate(inflater, container, false)
        binding.videoEventViewModel = videoViewModel
        binding.lifecycleOwner = this
        initViews()
        checkCameraPermission()
        cameraController.bindToLifecycle(this)
        enableVideoUseCase()
        setClickListeners()
        onDoubleClick()
        cameraViewModel.lensFacing.observe(viewLifecycleOwner) {
            lensFacing = it
            startCamera()
        }
        observeAndGenerateThumb()
        return binding.root
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            navController.navigate(
                CameraFragmentDirections.actionPhotoCameraFragmentToPermissionsFragment(
                    R.id.videoCamFragment
                )
            )
        } else {
            startCamera()

        }
    }

    private fun setClickListeners() {
        recordVideoButton.setOnClickListener {
            recordVideo()
        }
        stopVideoRecordingButton.setOnClickListener {
            stopRecording()
        }
        flipCameraButton.setOnClickListener {
            flipCamera()
        }
    }

    private fun onDoubleClick() {
        previewView.setOnClickListener(object : OnDoubleClickListener() {
            override fun onDoubleClick(view: View) {
                try {
                    flipCamera()
                } catch (ex: Exception) {
                    Log.e("TAG", "onDoubleClick: Switching camera failed", ex)
                }
            }
        })
    }

    private fun flipCamera() {
        if (cameraController.isRecording) {
            Toast.makeText(
                requireContext(),
                getString(R.string.video_camw_switch_message), Toast.LENGTH_SHORT
            ).show()

        } else {
            changeCamera()
        }

    }

    private fun changeCamera() {
        cameraViewModel.flipCamera()
    }


    @ExperimentalVideo
    private fun enableVideoUseCase() {
        cameraController.setEnabledUseCases(CameraController.VIDEO_CAPTURE)
    }

    @SuppressLint("MissingPermission")
    @ExperimentalVideo
    private fun recordVideo() {
        if (!cameraController.isRecording) {
            val outputOptions = MediaStoreOutputOptions.Builder(
                requireContext().applicationContext.contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            )
                .setContentValues(Utils.createContentValues(isImage = false))
                .build()
            var audioConfig: AudioConfig = AudioConfig.AUDIO_DISABLED
            if (isAudioPermissionGranted()) {
                audioConfig = AudioConfig.create(true)
                Toast.makeText(requireContext(), "Audio perm enabled", Toast.LENGTH_SHORT).show()
            }

            recording = cameraController.startRecording(
                outputOptions,
                audioConfig,
                ContextCompat.getMainExecutor(requireActivity())
            ) { event ->

                updateEvent(event)
            }


        } else return


    }

    private fun isAudioPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun observeAndGenerateThumb() {
        videoViewModel.videoThumb.observe(viewLifecycleOwner) { bmp ->
            bmp?.let {
                thumbPreview.setImageBitmap(it)
            }


        }
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