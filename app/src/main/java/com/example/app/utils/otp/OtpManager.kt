package com.example.app.utils.otp

import android.os.SystemClock

object OtpManager {
    private var savedOtp: String? = null
    private var userId: String? = null
    private var expiryTimestamp: Long = 0L

    fun saveOtp(otp: String, id: String) {
        savedOtp = otp
        userId = id
        // Lưu thời điểm hết hạn = thời gian hiện tại + 5 phút (300.000 ms)
        expiryTimestamp = SystemClock.elapsedRealtime() + 300_000L
    }

    fun verify(inputOtp: String): Boolean {
        // Kiểm tra xem đã hết hạn chưa
        if (SystemClock.elapsedRealtime() > expiryTimestamp) {
            clear()
            return false
        }
        return savedOtp == inputOtp
    }

    fun getUserId(): String? = userId

    fun clear() {
        savedOtp = null
        userId = null
        expiryTimestamp = 0L
    }
}