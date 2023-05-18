package com.ngengeapps.zicam


import android.content.ContentValues
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController.IMAGE_ANALYSIS
import androidx.camera.view.CameraController.IMAGE_CAPTURE
import androidx.camera.view.CameraController.VIDEO_CAPTURE
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.imageview.ShapeableImageView
import com.ngengeapps.zicam.databinding.FragmentCameraBinding
import java.text.SimpleDateFormat
import java.util.Locale


class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private lateinit var cameraController: LifecycleCameraController
    private lateinit var cameraManager: CameraManager
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private val permissionViewModel: PermissionViewModel by activityViewModels()
    private val TAG = CameraFragment::class.simpleName
    private lateinit var imageVideoPreview: ShapeableImageView


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
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

    private fun createContentValues(isImage: Boolean = true): ContentValues {
        val mimeType = if (isImage) "image/jpeg" else "video/mp4"
        val relativePath = if (isImage) "Pictures/ZiCam" else "Video/ZiCam"
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

    private fun takePicture() {
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, createContentValues()
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