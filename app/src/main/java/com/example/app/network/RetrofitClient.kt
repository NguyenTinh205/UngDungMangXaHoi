package com.example.app.network


import com.example.app.network.api.AuthApiService
import com.example.app.network.dto.auth.AuthEvent
import com.example.app.network.dto.auth.AuthEventBus
import com.example.app.network.dto.auth.AuthManager
import com.example.app.network.dto.auth.request.RefreshTokenRequest
import com.example.app.network.interceptor.AuthInterceptor
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://nonoily-overinfluential-deegan.ngrok-free.dev/api/"


    //Giúp ghi lại (log) toàn bộ thông tin về các request (gửi đi) và response (nhận về) khi app gọi API.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Level.BODY là mức cao nhất, nghĩa là in ra toàn bộ:
    }


    // OkHttp sẽ tự động gọi đoạn code này chỉ khi nhận được mã 401. (Token hết hạn)
    private val authenticator = Authenticator { _, response ->
        // Lấy refreshToken đã lưu trước đó
        val refreshToken = AuthManager.getRefreshToken()

        if (refreshToken != null) {

            /** Không được dùng Retrofit client chính, tạo một instance Retrofit "sạch"
             không có authenticator để gọi API refresh, tránh vòng lặp vô hạn nếu API này trả về 401.*/
            val refreshApi = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApiService::class.java)

            // Tạo một coroutine scope và block thread hiện tại cho đến khi coroutine hoàn thành.
            val res = runBlocking {
                refreshApi.checkRefreshToken(RefreshTokenRequest(refreshToken))
            }

            if (res.isSuccessful && res.body()?.success == true) {
                val newAccessToken = res.body()?.accessToken ?: ""
                AuthManager.saveAccessToken(newAccessToken)

                // Trả về request mới với token mới để OkHttp thực hiện lại
                return@Authenticator response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            }
        }

        // Nếu refresh thất bại hoặc không có token -> Logout
        runBlocking {
            AuthEventBus.emit(AuthEvent.SessionExpired)
        }
        null
    }

    private val client = OkHttpClient.Builder()

        //In ra toàn bộ log của mọi request và response (header, body, thời gian, v.v.) vào Logcat.
        .addInterceptor(loggingInterceptor)

        //Lấy access token từ AuthManager (SharedPreferences) và gắn vào header.
        .addInterceptor(AuthInterceptor())

        /** Khi server trả về mã 401 Unauthorized, OkHttp sẽ tự động gọi authenticator này.
        Dùng refresh token để gọi API lấy access token mới.
        Nếu thành công → trả về một request mới với token mới → OkHttp sẽ tự động thực hiện lại request gốc. */
        .authenticator(authenticator)

        // Nếu trong 30 giây không kết nối được (do mạng yếu, server chết, firewall, v.v.) → request sẽ thất
        .connectTimeout(30, TimeUnit.SECONDS)

        // Đặt thời gian tối đa để đọc dữ liệu từ server sau khi đã kết nối thành công.
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Hàm tái sử dụng gọi ApiService
    fun <T> create(service: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)

            /**Parse JSON từ response của server → thành các data class Kotlin
                mà bạn đã định nghĩa (ví dụ: PostResponse, AuthResponse, v.v.).
                Serialize (chuyển đổi) các request body (nếu bạn gửi object lên server)
                thành chuỗi JSON.*/
            .addConverterFactory(GsonConverterFactory.create())

            .build()
            .create(service)
    }
}