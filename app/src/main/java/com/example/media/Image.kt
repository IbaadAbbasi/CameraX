package com.example.media

import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.media.databinding.ActivityImageBinding
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.FileDescriptor


class Image : AppCompatActivity() {

    private lateinit var binding: ActivityImageBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uri =intent.getStringExtra("image");
        val videoUri =intent.getStringExtra("video");
        val fileUri =intent.getStringExtra("file");
        val bitmap: Bitmap


//      val original = BitmapFactory.decodeStream(assets.open("1024x768.jpg"))


        if(uri!=null) {
            bitmap = uriToBitmap(Uri.parse(uri))!!
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out)
            val decoded = BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))
            binding.img.setImageBitmap(decoded)
            binding.viewView.visibility=View.GONE
            binding.tvFile.visibility=View.GONE
        }else if(videoUri!=null) {
            binding.viewView.setVideoURI(Uri.parse(videoUri))
            binding.viewView.start()
            binding.img.visibility=View.GONE
            binding.tvFile.visibility=View.GONE
        }else{
            binding.viewView.visibility=View.GONE
            binding.img.visibility=View.GONE
            binding.tvFile.setText(fileUri.toString())
        }

    }

    private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {

        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(selectedFileUri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        }catch (ex: Exception){

        }

        return null
    }

    fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap? {
        val output = Bitmap.createBitmap(
            bitmap.width, bitmap
                .height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        val roundPx = pixels.toFloat()
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

}