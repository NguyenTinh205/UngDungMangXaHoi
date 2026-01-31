package com.example.app.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun uploadToCloudinary(
    context: Context,
    uri: Uri,
    uploadPreset: String
): String = suspendCancellableCoroutine { cont ->

    val requestId = MediaManager.get()
        .upload(uri)
        .unsigned(uploadPreset)
        .option("resource_type", "auto") // image/video đều được
        .callback(object : UploadCallback {
            override fun onStart(requestId: String?) {}

            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

            override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                val url = resultData?.get("secure_url")?.toString()
                Log.d("link: ",url.toString())
                if (url.isNullOrBlank()) {
                    cont.resumeWithException(IllegalStateException("Missing secure_url"))
                } else {
                    cont.resume(url)
                }
            }

            override fun onError(requestId: String?, error: ErrorInfo?) {
                cont.resumeWithException(RuntimeException(error?.description ?: "Upload error"))
            }

            override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
        })
        .dispatch(context)

    cont.invokeOnCancellation {
        // Hủy upload nếu coroutine bị cancel
        MediaManager.get().cancelRequest(requestId)
    }
}
