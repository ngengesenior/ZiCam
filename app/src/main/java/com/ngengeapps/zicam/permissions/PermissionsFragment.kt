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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ngengeapps.zicam.PermissionResult
import com.ngengeapps.zicam.R
import com.ngengeapps.zicam.databinding.FragmentPermissionsBinding


class PermissionsFragment : Fragment() {
    private var _binding: FragmentPermissionsBinding? = null
    private val args: PermissionsFragmentArgs by navArgs()
    private val binding get() = _binding!!
    private val permissionViewModel: PermissionViewModel by viewModels()
    private lateinit var permissionsLauncher: ActivityResultLauncher<Array<String>>
    private val acceptedPermissions: MutableSet<String> = mutableSetOf()
    private val navController by lazy {
        findNavController()
    }

    private fun registerPermissionsLauncher() {
        permissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsResult ->
                for (result in permissionsResult) {
                    if (result.key == CAMERA) {
                        if (result.value) {
                            acceptedPermissions.add(CAMERA)
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
                            //permissionViewModel.addPermission(RECORD_AUDIO)
                            acceptedPermissions.add(RECORD_AUDIO)
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
                if (acceptedPermissions.contains(CAMERA)) {
                    permissionViewModel.sendResult(PermissionResult.GRANTED)
                } else {
                    permissionViewModel.sendResult(PermissionResult.DENIED)
                }
            }

    }

    private fun toastAudioPermissionDenied() {
        Toast.makeText(
            requireContext(),
            getString(R.string.no_sound_warning),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun toastCameraPermissionDenied() {
        Toast.makeText(
            requireContext(),
            getString(R.string.no_camera_permission_warning),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun String.getPermissionMessage(): String {
        return when {
            this == RECORD_AUDIO -> {
                getString(R.string.audio_is_required)
            }

            this == CAMERA -> {
                getString(R.string.needs_camera)
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
            .setTitle(getString(R.string.permission_denied_title))
            .setMessage(getString(R.string.permissions_denied_warning))
            .setPositiveButton(getString(R.string.settings)) { _, _ ->
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
                    title = getString(R.string.permission_required_title),
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
                    title = getString(R.string.record_audio_permission),
                    message = getString(R.string.grant_audio_record_title),
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
            permissionViewModel.sendResult(PermissionResult.GRANTED)
        } else if (ActivityCompat.checkSelfPermission(
                requireContext(),
                CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            permissionViewModel.sendResult(PermissionResult.GRANTED)
        }
        registerPermissionsLauncher()
        observePermissions()
        binding.grantPermissionsButton.setOnClickListener {
            permissionsLauncher.launch(permissions)
        }


        return binding.root
    }

    private fun observePermissions() {
        permissionViewModel.result.observe(viewLifecycleOwner) {
            if (it == PermissionResult.GRANTED) {
                navController.popBackStack(destinationId = args.callerId, inclusive = false)
            }

        }
    }

    companion object {
        private val permissions = arrayOf(CAMERA, RECORD_AUDIO)
    }
}