package com.example.app.data.repository

import com.example.app.domain.model.Media
import com.example.app.domain.model.Post
import com.example.app.domain.model.PostList
import com.example.app.network.RetrofitClient
import com.example.app.network.api.PostApiService
import com.example.app.network.dto.post.request.CreatePostRequest
import com.example.app.network.dto.post.request.SharePostRequest
import com.example.app.network.dto.post.response.PostResponse
import com.example.app.network.dto.post.response.SharePostResponse
import com.example.app.network.dto.profile.response.ProfilePostResponse
import com.example.app.utils.mapper.toDomain
import retrofit2.Response

class PostRepository {
    private val api = RetrofitClient.create(PostApiService::class.java)

    suspend fun getPostFeed(
        limit: Int = 5,
        cursor: String? = null
    ): Result<PostList> {
        return try {
            val response: Response<ProfilePostResponse> = api.getPostFeed(
                limit = limit,
                cursor = cursor
            )

            if (response.isSuccessful) {
                response.body()?.let { wrapper ->
                    if (wrapper.success) {
                        val postsDomain = wrapper.posts.map { it.toDomain() }
                        Result.success(
                            PostList(
                                posts = postsDomain,
                                nextCursor = wrapper.nextCursor  // ← Lấy next_cursor từ response
                            )
                        )
                    } else {
                        Result.failure(Exception("API trả về success = false"))
                    }
                } ?: Result.failure(Exception("Dữ liệu rỗng từ server"))
            } else {
                Result.failure(Exception("Lỗi mạng: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun getPostSave(
        limit: Int = 5,
        cursor: String? = null
    ): Result<PostList> {
        return try {
            val response: Response<ProfilePostResponse> = api.getPostSave(
                limit = limit,
                cursor = cursor
            )

            if (response.isSuccessful) {
                response.body()?.let { wrapper ->
                    if (wrapper.success) {
                        val postsDomain = wrapper.posts.map { it.toDomain() }
                        Result.success(
                            PostList(
                                posts = postsDomain,
                                nextCursor = wrapper.nextCursor  // ← Lấy next_cursor từ response
                            )
                        )
                    } else {
                        Result.failure(Exception("API trả về success = false"))
                    }
                } ?: Result.failure(Exception("Dữ liệu rỗng từ server"))
            } else {
                Result.failure(Exception("Lỗi mạng: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPost(
        content: String,
        privacy: String,
        mediaItems: List<Media>
    ): Response<PostResponse> {
        val request = CreatePostRequest(
            content = content,
            privacy = privacy.lowercase(),
            media = mediaItems.ifEmpty { null }
        )
        return api.createPost(request)
    }
    suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean): Result<String> {
        return try {
            val response = if (isCurrentlyLiked) api.unlikePost(postId) else api.likePost(postId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.success(body.message ?: "Success")
                } else {
                    Result.failure(Exception(body?.message ?: "API returned success=false"))
                }
            } else {
                // Optional: parse error body nếu server trả JSON error
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun toggleSave(postId: String, isCurrentlySaved: Boolean): Result<String> {
        return try {
            val response = if (isCurrentlySaved) api.unsavePost(postId) else api.savePost(postId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.success(body.message ?: "Success")
                } else {
                    Result.failure(Exception(body?.message ?: "API returned success=false"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private val cachedPosts = mutableMapOf<String, Post>()
    }

    fun addToCache(posts: List<Post>) {
        posts.forEach { post ->
            cachedPosts[post.id] = post
        }
    }

    fun getPostFromCache(postId: String): Post? {
        return cachedPosts[postId]
    }

    suspend fun sharePost(
        originalPostId: String,
        content: String? = null,
        privacy: String = "public"
    ): Result<SharePostResponse?> {
        return try {
            val request = SharePostRequest(
                content = content,
                privacy = privacy
            )
            val response = api.sharePost(
                postId = originalPostId,
                request = request
            )

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        Result.success(body.sharedPost)
                    } else {
                        Result.failure(Exception("API trả về success = false"))
                    }
                } ?: Result.failure(Exception("Response body rỗng"))
            } else {
                when (response.code()) {
                    409 -> Result.success(null)
                    else -> Result.failure(Exception("Lỗi mạng: ${response.code()} - ${response.message()}"))
                }
            }
        }

        catch (e: Exception) {
            Result.failure(e)
        }
    }


    // MỚI: Xóa bài post
    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            val response = api.deletePost(postId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Xóa bài viết thất bại: HTTP ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePostPrivacy(postId: String, newPrivacy: String): Result<Unit> {
        return try {
            val privacyRequest = mapOf("privacy" to newPrivacy.lowercase())

            val response = api.updatePostPrivacy(postId, privacyRequest)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Cập nhật chế độ xem thất bại: HTTP ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPostById(postId: String): Result<Post> {
        return try {
            val response = api.getPostById(postId)
            if (response.isSuccessful && response.body() != null) {
                val wrapper = response.body()!!
                val postDto = wrapper.post

                // Convert sang Domain Model
                Result.success(postDto.toDomain())
            } else {
                Result.failure(Exception("Bài viết không tồn tại, đã bị xóa hoặc bạn không có quyền xem"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}