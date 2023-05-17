package com.ngengeapps.zicam


import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.ngengeapps.zicam.databinding.FragmentCameraBinding
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch


class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var cameraManager: CameraManager
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private val permissionViewModel: PermissionViewModel by activityViewModels()
    private val TAG = CameraFragment::class.simpleName


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
                lifecycleScope.launch {
                    startCamera()
                }
            }


        }
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private suspend fun startCamera() {
        cameraProvider = ProcessCameraProvider.getInstance(requireContext()).await()
        bindPreview(cameraProvider)
    }

    private fun bindPreview(provider: ProcessCameraProvider) {
        val preview = Preview.Builder().build()
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()
        preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
        try {
            provider.unbindAll()
            var camera = provider.bindToLifecycle(this, cameraSelector, preview)
        } catch (ex: IllegalStateException) {
            Log.e(TAG, "bindPreview: Failed binding preview")
        }

    }
}