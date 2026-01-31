package com.example.app



import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.example.app.network.SocketManager
import com.example.app.network.dto.auth.AuthManager

class MyApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        val config = mapOf(
            "cloud_name" to "dnvetb271"
        )
        com.cloudinary.android.MediaManager.init(this, config)
        AuthManager.init(this)
        SocketManager.init(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
