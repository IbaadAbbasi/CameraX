package com.example.media

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ScaleGestureDetector
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.media.databinding.ActivityCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Camera : AppCompatActivity() {

    lateinit var viewBinding: ActivityCameraBinding
    lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var isBackLens = true
    private var isFlash = false
    var cameraIntent: ActivityResultLauncher<Intent>? = null
    var effect: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        cameraIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {

                try {

                    val uri: Uri? = result.data?.data
                    val intent = Intent(this, Image::class.java)
                    intent.putExtra("image", uri.toString())
                    startActivity(intent)

                }catch (ex: java.lang.Exception){

                }
            }
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener {

            takePhoto() }


        viewBinding.switchBtn.setOnClickListener {
            //change the cameraSelector
            cameraSelector = if(cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA){
                CameraSelector.DEFAULT_FRONT_CAMERA
            }else {
                isBackLens=true
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            // restart the camera
            startCamera()
        }

        viewBinding.galleryBtn.setOnClickListener {

            pickImageFromGallery()

        }


        cameraExecutor = Executors.newSingleThreadExecutor()

        viewBinding.viewFinder
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCamera() {

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)

        }

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Image Capture
            var flashMode = ImageCapture.FLASH_MODE_OFF
            if (isBackLens && isFlash) {
                flashMode = ImageCapture.FLASH_MODE_ON
            }
            imageCapture = ImageCapture.Builder()
                .setFlashMode(flashMode)
                .build()

            val extensionsManager =
                ExtensionsManager.getInstanceAsync(this, cameraProvider)


            // Query if extension is available.
            if (extensionsManager.get().isExtensionAvailable(cameraSelector, effect)) {
                // Unbind all use cases before enabling different extension modes.
                cameraProvider.unbindAll()

                // Retrieve extension enabled camera selector

                val bokehCameraSelector = extensionsManager.get().getExtensionEnabledCameraSelector(cameraSelector, effect)

                try {
                    cameraProvider.bindToLifecycle(this, bokehCameraSelector, preview, imageCapture)
                } catch (e: Exception) {
                    Log.d(TAG, "Use case binding failed")
                }
            }else{

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                } catch (e: Exception) {
                    Log.d(TAG, "Use case binding failed")
                }

                Toast.makeText(this, "No Support", Toast.LENGTH_SHORT).show()
                effect=0
            }

            val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            val cameraControl = camera.cameraControl

            // Listen to pinch gestures
            val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    // Get the camera's current zoom ratio
                    val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio ?: 0F

                    // Get the pinch gesture's scaling factor
                    val delta = detector.scaleFactor

                    // Update the camera's zoom ratio. This is an asynchronous operation that returns
                    // a ListenableFuture, allowing you to listen to when the operation completes.
                    cameraControl.setZoomRatio(currentZoomRatio * delta)

                    // Return true, as the event was handled
                    return true
                }
            }

            val scaleGestureDetector = ScaleGestureDetector(this, listener)

            viewBinding.viewFinder.setOnTouchListener { _, event ->
                scaleGestureDetector.onTouchEvent(event)
                return@setOnTouchListener true
            }



        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        imageCapture?.let {
            val fileName = "JPEG_${System.currentTimeMillis()}"
            val file = File(externalMediaDirs[0], fileName)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            it.takePicture(
                outputFileOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.i(TAG, "The image has been saved in ${file.toUri()}")
                        gotoGallery()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(
                            viewBinding.root.context,
                            "Error taking photo",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.d(TAG, "Error taking photo:$exception")
                    }

                })
        }
    }

    private fun gotoGallery(){

        val intent = Intent(this, Gallery::class.java)
        startActivity(intent)

    }

    private fun pickImageFromGallery() {
        //Intent to pick image

        try {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"

            cameraIntent!!.launch(intent, )

        }catch (ex: java.lang.Exception){

        }
    }


    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}