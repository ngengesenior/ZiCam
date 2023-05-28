package com.ngengeapps.zicam.permissions

import android.Manifest.permission.CAMERA
import android.Manifest.permission.RECORD_AUDIO
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ngengeapps.zicam.databinding.FragmentPermissionsBinding

class PermissionsFragment : Fragment() {
    private var _binding: FragmentPermissionsBinding? = null
    private val binding get() = _binding!!
    private val permissionViewModel: PermissionViewModel by viewModels()
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private fun registerPermissionsLauncher() {
        permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
                for (result in permissionsResult) {
                    if (result.key == CAMERA) {
                        if (result.value) {
                            permissionViewModel.addPermission(CAMERA)
                        } else {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                    requireActivity(),
                                    CAMERA
                                )
                            ) {
                                handlePermissionInLauncher(CAMERA)

                            } else {
                                showCameraPermissionDeniedDialog()
                            }
                        }
                    } else {
                        if (result.value) {
                            permissionViewModel.addPermission(RECORD_AUDIO)
                        } else {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(
                                    requireActivity(),
                                    RECORD_AUDIO
                                )
                            ) {
                                handlePermissionInLauncher(RECORD_AUDIO)

                            } else {
                                toastAudioPermissionDenied()
                            }
                        }
                    }
                }

            }

    }

    private fun toastAudioPermissionDenied() {
        Toast.makeText(
            requireContext(),
            "Your videos will have no sound since you have refused to grant AUDIO permissions.You can grant it in app settings",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun toastCameraPermissionDenied() {
        Toast.makeText(
            requireContext(),
            "You will be unable to use this app without CAMERA permission. Change the permission in app settings",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun String.getPermissionMessage(): String {
        return when {
            this == RECORD_AUDIO -> {
                "RECORD AUDIO permission is required to record audio for your video to have sound"
            }

            this == CAMERA -> {
                "ZiCam needs camera permission to take photos or videos."
            }

            else -> {
                ""
            }
        }
    }

    private fun showPermissionRationale(
        permission: String, title: String, message: String,
        onPositiveButtonClick: (perm: String) -> Unit = {

        },
        onCancelButtonClick: () -> Unit = {}
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onPositiveButtonClick(permission)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                onCancelButtonClick()
            }
            .show()
    }


    private fun showCameraPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Camera permission denied")
            .setMessage("Without camera permission, the app cannot take photos or videos. Please go to Settings to grant the app camera permission.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri =
                    Uri.fromParts("package", requireContext().applicationContext.packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun handlePermissionInLauncher(permission: String) {
        when (permission) {
            CAMERA -> {
                showPermissionRationale(
                    permission = permission,
                    title = "Camera permission required",
                    message = permission.getPermissionMessage(),
                    onPositiveButtonClick = {
                        permissionsLauncher.launch(permissions)
                    },
                    onCancelButtonClick = {
                        toastCameraPermissionDenied()
                    }
                )
            }

            RECORD_AUDIO -> {
                showPermissionRationale(
                    permission = permission,
                    title = "Record Audio permission",
                    message = "Grant Record Audio permission else your recorded videos will have no sound",
                    onPositiveButtonClick = {
                        permissionsLauncher.launch(permissions)

                    },
                    onCancelButtonClick = {
                        toastAudioPermissionDenied()
                    }
                )

            }

        }
    }

    private fun areAllPermissionsAreGranted(): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(
            requireContext(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        if (areAllPermissionsAreGranted()) {
            permissionViewModel.addPermission(CAMERA)
            permissionViewModel.addPermission(RECORD_AUDIO)
        }
        registerPermissionsLauncher()
        binding.grantPermissionsButton.setOnClickListener {
            permissionsLauncher.launch(permissions)
        }


        return binding.root
    }

    private fun observePermissions() {
        permissionViewModel.permissions.observe(this) {
            if (permissions.size == 2) {

            }
        }
    }

    companion object {
        private val permissions = arrayOf(CAMERA, RECORD_AUDIO)
    }
}