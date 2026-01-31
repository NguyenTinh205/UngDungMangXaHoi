package com.example.app.ui.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app.domain.usecase.CreatePostUseCase
import com.example.app.ui.feed.create.CreatePostViewModel

class SharePostViewModelFactory(
    private val createPostUseCase: CreatePostUseCase,  // ← Bắt buộc thêm để gọi API
    private val avatarUrl: String?,
    private val userName: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
            return CreatePostViewModel(
                createPostUseCase = createPostUseCase,   // ← Truyền vào
                avatarUrl = avatarUrl,
                userName = userName
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}