package com.example.app.ui.feed.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.app.domain.usecase.CreatePostUseCase

class CreatePostViewModelFactory(
    private val createPostUseCase: CreatePostUseCase,
    private val avatarUrl: String?,
    private val userName: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatePostViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreatePostViewModel(createPostUseCase, avatarUrl, userName) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
