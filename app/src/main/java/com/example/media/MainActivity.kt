package com.example.media

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.media.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    lateinit var viewBinding: ActivityMainBinding
    var cameraIntent: ActivityResultLauncher<Intent>? = null
    private var attacmentType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        cameraIntent = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {

                try {

                    val uri: Uri? = result.data?.data
                    val intent = Intent(this, Image::class.java)

                    if(attacmentType.equals("video")) {

                        intent.putExtra("video", uri.toString())
                        cameraIntent!!.launch(intent)

                    }else if(attacmentType.equals("file")){

                        intent.putExtra("file", uri.toString())
                        cameraIntent!!.launch(intent)

                    }

                }catch (ex: java.lang.Exception){

                }
            }
        }

        viewBinding.btnCamera.setOnClickListener{
            val intent = Intent(this, Camera::class.java)
            startActivity(intent)
        }

        viewBinding.btnVideo.setOnClickListener{

            attacmentType="video"
            val galleryIntent = Intent(Intent.ACTION_PICK)
            galleryIntent.type = "video/*"
            cameraIntent!!.launch(galleryIntent)
        }

        viewBinding.btnFIle.setOnClickListener{
            attacmentType="file"
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            val uri = Uri.parse(
                Environment.getExternalStorageDirectory().absolutePath
            )
            intent.setDataAndType(uri,"*/*")
            cameraIntent!!.launch(intent)
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