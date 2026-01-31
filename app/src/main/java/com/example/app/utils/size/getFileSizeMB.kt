package com.example.app.utils.size

import android.content.Context
import android.net.Uri

fun getFileSizeMB(uri: Uri, context: Context): Double {
    return context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
        it.length.toDouble() / (1024.0 * 1024.0)
    } ?: 0.0
}