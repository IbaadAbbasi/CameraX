package com.example.media

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.media.databinding.ListItemImgBinding
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileDescriptor

class GalleryAdapter(private val fileArray: Array<File>, val context: Context) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(ListItemImgBinding.inflate(layoutInflater, parent, false), context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(fileArray[position])
    }

    class ViewHolder(private val binding: ListItemImgBinding, private val context: Context) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(file: File) {


            Glide.with(binding.root).load(file).into(binding.localImg)

            val bitmap: Bitmap
            bitmap = uriToBitmap(file.toUri())!!
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            val decoded = BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))
            val imageInByte: Long = file.length()
            val lengthbmp = imageInByte
            val lenth = (lengthbmp/1024)/1024
            Log.i(TAG, "Image Size is: "+lenth)
        }

        private fun uriToBitmap(selectedFileUri: Uri): Bitmap? {

            try {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(selectedFileUri, "r")
                val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
                val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                parcelFileDescriptor.close()
                return image
            }catch (ex: Exception){

            }

            return null
        }
    }

    override fun getItemCount() = fileArray.size
}