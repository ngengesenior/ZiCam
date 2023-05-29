package com.ngengeapps.zicam


import android.Manifest.permission.CAMERA
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.imageview.ShapeableImageView
import com.ngengeapps.zicam.databinding.FragmentCameraBinding


class CameraFragment : Fragment() {
    private var _binding: FragmentCameraBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val cameraController: LifecycleCameraController by lazy {
        LifecycleCameraController(requireContext())
    }
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    private lateinit var flipCameraButton: ImageView
    private lateinit var captureImageButton: View
    private lateinit var imageVideoPreview: ShapeableImageView
    private lateinit var viewFinder: PreviewView
    private lateinit var photoTextView: TextView
    private lateinit var videoTextView: TextView
    private val cameraViewModel: CameraViewModel by viewModels()

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>


    private fun initViews() {
        flipCameraButton = binding.flipCameraButton
        captureImageButton = binding.capturePhotoButton
        viewFinder = binding.viewFinder
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Request the permission
            cameraPermissionLauncher.launch(CAMERA)
        } else {
            // Permission is already granted
            startCamera()


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.cameraViewModel = cameraViewModel
        initViews()
        cameraController.bindToLifecycle(this)
        viewFinder.controller = cameraController
        imageVideoPreview = binding.imageVideoPreview
        registerCameraLauncher()
        checkCameraPermission()
        onDoubleClick()
        cameraViewModel
            .lensFacing
            .observe(viewLifecycleOwner) {
                lensFacing = it
                startCamera()
            }
        binding
            .capturePhotoButton
            .setOnClickListener {
                takePicture()
            }


        binding.root.setOnClickListener {
            cameraViewModel.flipCamera()
        }

        cameraViewModel.imageUri.observe(viewLifecycleOwner) {
            bindImageUri(binding.imageVideoPreview, it)
        }
        return binding.root

    }


    private fun onDoubleClick() {
        viewFinder.setOnClickListener(object : OnDoubleClickListener() {
            override fun onDoubleClick(view: View) {
                try {
                    cameraViewModel.flipCamera()
                } catch (ex: Exception) {
                    Log.e("TAG", "onDoubleClick: Switching camera failed", ex)
                }
            }
        })
    }


    private fun registerCameraLauncher() {
        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    //Camera permissions granted
                    startCamera()
                } else {
                    //
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(),
                            CAMERA
                        )
                    ) {
                        showPermissionRationale()

                    } else {
                        showPermissionDeniedDialog()
                    }
                }
            }

    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Camera permission required")
            .setMessage("The app needs camera permission to take photos or videos.")
            .setPositiveButton("Ok") { _, _ ->
                cameraPermissionLauncher.launch(CAMERA)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Camera permission denied")
            .setMessage("Without camera permission, the app cannot take photos or videos. Please go to Settings to grant the app camera permission.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraController.unbind()
        _binding = null
    }

    private fun startCamera() {
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        cameraController.cameraSelector = cameraSelector
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
                    imageVideoPreview.visibility = View.VISIBLE
                    //imageVideoPreview.setImageURI(outputFileResults.savedUri)
                    cameraViewModel.setImageUri(outputFileResults.savedUri)

                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(
                        CameraViewModel::class.simpleName,
                        "onError: ${exception.localizedMessage}",
                    )
                }

            }
        )
    }


    companion object {
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
    }
}