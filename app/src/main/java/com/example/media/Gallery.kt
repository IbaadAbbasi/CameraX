package com.example.media

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.media.databinding.ActivityGalleryBinding
import java.io.File

class Gallery : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val directory = File(externalMediaDirs[0].absolutePath)
        val files = directory.listFiles() as Array<File>

        // array is reversed to ensure last taken photo appears first.
        val adapter = GalleryAdapter(files.reversedArray(), binding.root.context)
        binding.viewPager.adapter = adapter
    }
}