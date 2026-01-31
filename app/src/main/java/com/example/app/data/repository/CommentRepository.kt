package com.example.app.data.repository

import com.example.app.domain.model.CommentList
import com.example.app.domain.model.Comment
import com.example.app.network.RetrofitClient
import com.example.app.network.api.CommentApiService
import com.example.app.network.api.PostApiService
import com.example.app.network.dto.comment.request.CommentRequest
import com.example.app.utils.mapper.toDomain

class CommentRepository {

    private val apiService = RetrofitClient.create(CommentApiService::class.java)
    suspend fun getComments(postId: String, cursorTime: String?): Result<CommentList> {
        return try {

            val response = apiService.getComments(postId, cursorTime)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body.toDomain())
            }
            else {
                Result.failure(Exception("Lỗi lấy danh sách bình luận"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendComment(postId: String, content: String): Result<Comment> {
        return try {

            val response = apiService.postComment(postId, CommentRequest(content))
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body.toDomain())
            }
            else {
                Result.failure(Exception("Lỗi gửi bình luận"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun sendReply(postId: String, commentId: String, content: String): Result<Comment> {
        return try {

            val response = apiService.postReply(postId, commentId, CommentRequest(content))
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body.toDomain())
            }
            else {
                Result.failure(Exception("Lỗi gửi phản hồi"))
            }
        }
        catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getReplies(postId: String, parentId: String, cursor: String? = null): Result<CommentList> {
        return try {
            val response = apiService.getReplies(postId, parentId, cursor)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                Result.success(body.toDomain())
            }
            else {
                Result.failure(Exception("Không thể tải phản hồi"))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likeComment(commentId: String): Result<Unit> {
        return try {
            val response = apiService.likeComment(commentId)

            if (response.isSuccessful) {
                Result.success(Unit)
            }
            else {
                val errorMsg = response.errorBody()?.string() ?: "Lỗi like bình luận (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlikeComment(commentId: String): Result<Unit> {
        return try {

            val response = apiService.unlikeComment(commentId)

            if (response.isSuccessful) {
                Result.success(Unit)
            }
            else {
                val errorMsg = response.errorBody()?.string() ?: "Lỗi bỏ like bình luận (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        }
        catch (e: Exception) {
            Result.failure(e)
        }

    }


    suspend fun deleteComment(commentId: String): Result<Unit> {
        return try {
            val response = apiService.deleteComment(commentId)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Lỗi xóa bình luận (${response.code()})"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}