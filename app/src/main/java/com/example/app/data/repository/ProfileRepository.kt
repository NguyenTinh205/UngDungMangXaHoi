package com.example.app.data.repository
import com.example.app.domain.model.Post
import com.example.app.domain.model.PostList
import com.example.app.domain.model.Profile
import com.example.app.network.RetrofitClient
import com.example.app.network.api.ProfileApiService
import com.example.app.network.dto.profile.response.ProfilePostResponse
import com.example.app.network.dto.profile.response.UpdateProfileRequest
import com.example.app.network.dto.profile.response.UserDto
import com.example.app.utils.mapper.toDomain
import retrofit2.Response


class ProfileRepository {
    private val api = RetrofitClient.create(ProfileApiService::class.java)
    private var profileCache = mutableMapOf<String, Profile>()
    suspend fun getProfile(userId: String): Result<Pair<Profile, List<Post>>> {
        return try {
            val response = api.getProfile(userId)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    val profileDomain = body.profile.toDomain()
                    profileCache[userId] = profileDomain
                    val postsDomain = body.posts.map { it.toDomain() }  // ← Map ở đây
                    Result.success(profileDomain to postsDomain)
                } ?: Result.failure(Exception("Dữ liệu trả về rỗng"))
            } else {
                Result.failure(Exception("Lỗi ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getProfileById(userId: String, forceRefresh: Boolean = false): Profile? {
        if (!forceRefresh && profileCache.containsKey(userId)) {
            return profileCache[userId]
        }

        val result = getProfile(userId)
        return result.getOrNull()?.first  // trả về Profile nếu thành công, null nếu fail
    }

    suspend fun getMoreProfilePosts(
        userId: String = "me",
        limit: Int = 5,
        cursor: String? = null
    ): Result<PostList> {
        return try {
            val response: Response<ProfilePostResponse> = api.getProfilePosts(
                userId = userId,
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

        suspend fun followUser(userId: String): Result<Unit> {
            return try {
                val response = api.followUser(userId)

                if (response.isSuccessful || response.code() == 409) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Lỗi: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun unfollowUser(userId: String): Result<Unit> {
        return try {
            val response = api.unFollowUser(userId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Lỗi: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateProfile(
        username: String?,
        fullName: String?,
        gender: String?,
        avatar: String?,
        address: String?,
        phone: String?
    ): Result<Unit> {
        return try {
            val response = api.updateProfile(
                UpdateProfileRequest(
                    username = username,
                    fullName = fullName, // sẽ gửi thành full_name nhờ @SerializedName
                    gender = gender,
                    avatar = avatar,
                    address = address,
                    phone = phone
                )
            )

            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Lỗi: ${response.code()} - ${response.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserDetails(userId: String): UserDto {
        val response = api.getUserDetails(userId)
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Dữ liệu rỗng")
            if (!body.success) throw Exception("API trả về success = false")
            return body.user
        } else {
            throw Exception("Lỗi mạng: ${response.code()} - ${response.message()}")
        }
    }
}
