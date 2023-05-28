package com.ngengeapps.zicam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.ngengeapps.zicam.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

//    private fun registerCameraLauncher() {
//        cameraPermissionLauncher =
//            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//                if (isGranted) {
//                    //Camera permissions granted
//                    permissionViewModel.sendResult(PermissionResult.GRANTED)
//                } else {
//                    //
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(
//                            this,
//                            Manifest.permission.CAMERA
//                        )
//                    ) {
//                        showPermissionRationale()
//
//                    } else {
//                        showPermissionDeniedDialog()
//                    }
//                }
//            }
//
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // registerCameraLauncher()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        //checkCameraPermission()
        supportActionBar?.hide()

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}