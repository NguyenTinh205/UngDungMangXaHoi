package com.example.app.domain.usecase

import com.example.app.data.repository.PostRepository
import com.example.app.domain.model.Media
import com.example.app.ui.feed.create.PostPrivacy

class CreatePostUseCase(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(
        content: String,
        privacy: PostPrivacy,
        mediaItems: List<Media>
    ): Result<Unit> {
        return try {
            val response = postRepository.createPost(
                content = content,
                privacy = privacy.apiValue.lowercase(),
                mediaItems = mediaItems
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Server error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}