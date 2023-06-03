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
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
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
    private val cameraViewModel: CameraViewModel by viewModels()
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private val navController: NavController by lazy { findNavController() }


    private fun initViews() {
        flipCameraButton = binding.flipCameraButton
        captureImageButton = binding.capturePhotoButton
        viewFinder = binding.viewFinder
        imageVideoPreview = binding.imageVideoPreview
    }


    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            navController.navigate(
                CameraFragmentDirections.actionPhotoCameraFragmentToPermissionsFragment(
                    R.id.photoCameraFragment
                )
            )
        } else {
            // Permission is already granted
            startCamera()

        }
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermission()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.cameraViewModel = cameraViewModel
        initViews()
        checkCameraPermission()
        cameraController.bindToLifecycle(this)
        viewFinder.controller = cameraController
        //registerCameraLauncher()
        onDoubleClick()
        setClickListeners()
        cameraViewModel
            .lensFacing
            .observe(viewLifecycleOwner) {
                lensFacing = it
                startCamera()
            }
        return binding.root

    }

    private fun setClickListeners() {
        captureImageButton
            .setOnClickListener {
                takePicture()
            }
        flipCameraButton.setOnClickListener {
            cameraViewModel.flipCamera()
        }

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