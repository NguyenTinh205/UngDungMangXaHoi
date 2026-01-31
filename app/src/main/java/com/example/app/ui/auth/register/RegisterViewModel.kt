package com.example.app.ui.auth.register

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.AuthRepository
import com.example.app.network.RetrofitClient
import com.example.app.network.api.AuthApiService
import com.example.app.network.dto.auth.request.RegisterRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate


enum class RegisterStep { USERNAME, PASSWORD, DATE, EMAIL, OTP }
data class RegisterUiState(
    val step: RegisterStep = RegisterStep.USERNAME,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userId: String? = null,
    val isOtpSent: Boolean = false
)
data class RegisterUser(
    val userName: String = "",
    val email: String = "",
    val password: String = "",
    val dateOfBirth: String = "",
)

class RegisterViewModel(
    application: Application
) : AndroidViewModel(application) {

    // 1. Repository chịu trách nhiệm gọi API – ViewModel chỉ tương tác qua Repository
    private val apiAuth = RetrofitClient.create(AuthApiService::class.java)
    private val repository: AuthRepository = AuthRepository(apiAuth)

    // 2. State lưu thông tin người dùng đang nhập (tạm thời qua các bước)
    private val _user = MutableStateFlow(RegisterUser())
    val user: StateFlow<RegisterUser> = _user.asStateFlow()

    // 3. State quản lý UI chung (loading, error, userId sau register, trạng thái OTP)
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // 4. State điều hướng giữa các bước đăng ký
    private val _navigationStep = MutableStateFlow(RegisterStep.USERNAME)
    val navigationStep: StateFlow<RegisterStep> = _navigationStep.asStateFlow()

    // 5. State lưu ngày sinh được chọn từ DatePicker (LocalDate)
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    val selectedDate: StateFlow<LocalDate?> = _selectedDate.asStateFlow()

    // 6. State nhập OTP (chỉ lấy tối đa 6 chữ số)
    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp

    // 7. State đếm ngược cooldown khi bấm "Gửi lại mã"
    private val _noCodeCooldown = MutableStateFlow(0)
    val noCodeCooldown: StateFlow<Int> = _noCodeCooldown

    // 8. SharedPreferences để lưu thời gian cuối cùng bấm "Gửi lại mã"
    //    (đảm bảo cooldown vẫn hoạt động khi app bị kill hoặc xoay màn hình)
    private val prefs = application.getSharedPreferences("otp_pref", Context.MODE_PRIVATE)
    private val cooldownSeconds = 30

    private fun Char.isVietnameseNameChar(): Boolean =
        this.isLetter() || this == ' ' ||
                this.category == CharCategory.NON_SPACING_MARK ||
                this.category == CharCategory.COMBINING_SPACING_MARK ||
                this.category == CharCategory.ENCLOSING_MARK


    // 9. Khởi tạo cooldown nếu có dữ liệu từ lần trước
    init {
        // Load thời gian cooldown còn lại
        val lastTime = prefs.getLong("last_no_code_time", 0L)
        if (lastTime != 0L) {
            val diff = (System.currentTimeMillis() - lastTime) / 1000
            val remain = cooldownSeconds - diff.toInt()
            if (remain > 0) {
                _noCodeCooldown.value = remain
                startCooldown(cooldownSeconds)
            }
        }
    }

    // 10. Các hàm cập nhật thông tin người dùng (được gọi từ UI)

    fun updateUserName(input: String){
        val kept = input.filter { it.isVietnameseNameChar() }
        _user.update { it.copy(userName = kept) }
    }

    fun updateEmail(value: String) {
        _user.value = _user.value.copy(email = value)
    }

    fun updatePassword(value: String) {
        _user.value = _user.value.copy(password = value)
        val isValid = passwordRegex.matches(value)
        _isPasswordValid.value = isValid
        if (isValid) clearError()
    }

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    fun updateConfirmPassword(value: String) {
        _confirmPassword.value = value
    }

    private val _isAgeValid = MutableStateFlow(false)
    val isAgeValid: StateFlow<Boolean> = _isAgeValid.asStateFlow()

    // Cập nhật lại hàm updateDateOfBirth
    fun updateDateOfBirth(dateString: String) {
        _user.value = _user.value.copy(dateOfBirth = dateString)
    }

    fun clearError() {
        if (_uiState.value.errorMessage != null) {
            _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }
    // 11. Xử lý ngày sinh từ DatePicker
    /**
     * Khi người dùng chọn ngày từ DatePicker, lưu LocalDate và đồng thời
     * chuyển thành String để lưu vào RegisterUser (dùng cho API)
     */
    fun setDateOfBirth(date: LocalDate) {
        _selectedDate.value = date

        // Cập nhật string cho API
        updateDateOfBirth(date.toString()) // "YYYY-MM-DD"

        // Tự động kiểm tra tuổi hợp lệ
        val today = LocalDate.now()
        val age = java.time.Period.between(date, today).years
        val isValid = age >= 16 // hoặc 13 tùy yêu cầu

        _isAgeValid.value = isValid

        if (!isValid) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Bạn phải từ 16 tuổi trở lên để đăng ký"
            )
        } else {
            clearError()
        }
    }

    // 12. Xử lý nhập OTP
    fun onOtpChange(value: String) {
        _otp.value = value.take(6).filter { it.isDigit() }
    }

    // 14. Điều hướng giữa các bước đăng ký
    fun nextStep() {
        _navigationStep.value = when (_navigationStep.value) {
            RegisterStep.USERNAME -> RegisterStep.DATE
            RegisterStep.DATE -> RegisterStep.EMAIL
            RegisterStep.EMAIL -> RegisterStep.PASSWORD
            RegisterStep.PASSWORD -> RegisterStep.OTP
            RegisterStep.OTP -> RegisterStep.USERNAME
        }
    }

    /**
     * Quay lại bước trước đó
     */

    // 14. Quản lý cooldown "Gửi lại mã"
    fun startCooldown(seconds: Int) {
        viewModelScope.launch {
            (seconds downTo 0).asFlow().collect {
                _noCodeCooldown.value = it
                delay(1000)
            }
        }
    }


    /**
     * Khi người dùng bấm "Gửi lại mã" (No code received)
     * - Reset cooldown về 30 giây
     * - Lưu thời gian hiện tại vào SharedPreferences
     * - Bắt đầu đếm ngược
     */
    fun onNoCodeClick() {
        _noCodeCooldown.value = cooldownSeconds
        prefs.edit { putLong("last_no_code_time", System.currentTimeMillis()) }
        startCooldown(cooldownSeconds)
    }

    // 15. Gửi lại OTP (resend OTP)
    fun resendOtp() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val userId = _uiState.value.userId ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "User ID không xác định"
                )
                return@launch
            }

            val email = _user.value.email

            // Gọi sendOtp từ Repository, không gọi API trực tiếp
            val result = repository.sendOtp(userId, email)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    isOtpSent = true
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message
                        ?: "Không thể gửi lại mã OTP"
                )
            }
        }
    }

    // 16. Xác thực otp
    fun verifyOtp(
        otp: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        // Kiểm tra định dạng OTP trước khi gọi API
        if (otp.length != 6 || !otp.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Mã OTP phải gồm đúng 6 chữ số"
            )
            onError("Mã OTP phải gồm đúng 6 chữ số")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            // Lấy userId đã lưu từ lúc register thành công
            val userId = _uiState.value.userId ?: run {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Không tìm thấy thông tin người dùng. Vui lòng thử lại."
                )
                onError("Thông tin người dùng bị mất")
                return@launch
            }

            val email = _user.value.email

            // Gọi API verifyOtp qua Repository
            val result = repository.verifyOtp(userId, email, otp)

            if (result.isSuccess) {
                // Xác thực thành công
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
                onSuccess() // ← Quan trọng: Chuyển sang màn hình chính (FeedActivity)
            } else {
                // Xác thực thất bại
                val errorMessage = result.exceptionOrNull()?.message
                    ?: "Mã OTP không đúng hoặc đã hết hạn"

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
                onError(errorMessage)
            }
        }
    }


    // 17. Hàm tiện ích lấy dữ liệu user cuối cùng
    fun getFinalUser() = _user.value.copy()

    // 18. Kiểm tra mật khẩu
    private val passwordRegex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@._-]).{8,}$")

    // Thêm state để UI quan sát trạng thái hợp lệ của mật khẩu
    private val _isPasswordValid = MutableStateFlow(false)


    val isPasswordValid: StateFlow<Boolean> = _isPasswordValid.asStateFlow()
    fun canNavigateToConfirm(confirmPass: String): Boolean {
        return _isPasswordValid.value && _user.value.password == confirmPass
    }

    // 19. Bắt đầu đăng ký tài khoản (gọi khi ở bước Password bấm Tiếp)
    /**
     * Gọi API /auth/register
     * Thành công → lưu userId + tự động gửi OTP + chuyển sang bước OTP
     * Thất bại → hiển thị lỗi
     */

    /**
     * Kiểm tra email khi bấm "Tiếp" ở bước Email
     * - Nếu email rỗng → báo lỗi
     * - Nếu email đã tồn tại → báo lỗi "Email đã được sử dụng"
     * - Nếu email hợp lệ → chuyển sang bước Password
     */
    fun validateAndProceedFromEmail(
        onEmailExists: () -> Unit = {},  // Callback nếu email đã tồn tại (có thể dùng để gợi ý đăng nhập)
        onSuccess: () -> Unit            // Callback nếu email hợp lệ → đi tiếp
    ) {
        viewModelScope.launch {
            val email = _user.value.email.trim()

            // Kiểm tra email rỗng
            if (email.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Vui lòng nhập email"
                )
                return@launch
            }

            // Bắt đầu loading
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            // Gọi API kiểm tra email
            val result = repository.checkEmail(email)

            if (result.isSuccess) {
                val emailExists = result.getOrNull() ?: false

                if (emailExists) {
                    // Email đã tồn tại → báo lỗi
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Email đã được sử dụng"
                    )
                    onEmailExists() // Có thể dùng để hiển thị dialog "Bạn muốn đăng nhập?"
                } else {
                    // Email hợp lệ → đi tiếp bước Password
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
            } else {
                // Lỗi mạng hoặc server
                val errorMsg = result.exceptionOrNull()?.message
                    ?: "Không thể kiểm tra email. Vui lòng thử lại."

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
            }
        }
    }


    fun startRegistration(
        onSuccess: () -> Unit,   // Chuyển sang bước OTP
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            // Kiểm tra các field bắt buộc trước khi gọi API
            val currentUser = _user.value
            when {
                currentUser.userName.isBlank() -> {
                    _uiState.value =
                        _uiState.value.copy(errorMessage = "Vui lòng nhập tên người dùng")
                    return@launch
                }

                currentUser.dateOfBirth.isBlank() -> {
                    _uiState.value =
                        _uiState.value.copy(errorMessage = "Vui lòng chọn ngày sinh")
                    return@launch
                }

                currentUser.email.isBlank() -> {
                    _uiState.value = _uiState.value.copy(errorMessage = "Vui lòng nhập email")
                    return@launch
                }

                currentUser.password.isBlank() -> {
                    _uiState.value =
                        _uiState.value.copy(errorMessage = "Vui lòng nhập mật khẩu")
                    return@launch
                }

                !_isPasswordValid.value -> {
                    _uiState.value = _uiState.value.copy(errorMessage = "Mật khẩu chưa đủ mạnh")
                    return@launch
                }

                !_isAgeValid.value -> {
                    _uiState.value =
                        _uiState.value.copy(errorMessage = "Bạn phải từ 16 tuổi trở lên")
                    return@launch
                }
            }

            // Bắt đầu loading
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val request = RegisterRequest(
                full_name = currentUser.userName,
                birthday = currentUser.dateOfBirth, // format YYYY-MM-DD
                email = currentUser.email,
                password = currentUser.password
            )

            val result = repository.register(request)

            if (result.isSuccess) {
                val response = result.getOrNull()!!

                // Lưu userId để dùng cho sendOtp và verifyOtp
                _uiState.value = _uiState.value.copy(
                    userId = response.userId,
                    isLoading = false
                )

                // TỰ ĐỘNG GỬI OTP SAU KHI REGISTER THÀNH CÔNG
                resendOtp()

                // Chuyển sang bước OTP
                onSuccess()
            } else {
                val errorMsg = result.exceptionOrNull()?.message
                    ?: "Đăng ký thất bại. Vui lòng thử lại."

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMsg
                )
                onError(errorMsg)
            }
        }
    }
}

