package com.example.app.utils.time

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun formatTimeAgo(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "Vừa xong"
    return try {
        val instant = Instant.parse(isoString)
        formatTimeAgo(instant.toEpochMilli())
    } catch (_: Exception) {
        "Vừa xong"
    }
}

fun formatTimeAgo(createdAt: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - createdAt

    if (diff < 0L || diff < 60_000) return "Vừa xong"

    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 60 -> "$minutes phút trước"
        hours < 24   -> "$hours giờ trước"
        days < 30    -> "$days ngày trước" // Dưới 1 tháng thì hiện "ngày trước"
        else -> {
            // Lấy thông tin thời gian cụ thể của bài đăng và hiện tại
            val createdDateTime = Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault())
            val nowDateTime = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault())

            val localeVN = Locale.forLanguageTag("vi-VN")

            // Kiểm tra xem có cùng năm hay không
            val pattern = if (createdDateTime.year == nowDateTime.year) {
                // Nếu cùng năm: chỉ hiện "12 thg 12"
                "dd 'thg' MM"
            } else {
                // Nếu khác năm (VD: 2025 vs 2026): hiện đầy đủ "12/12/2025"
                "dd/MM/yyyy"
            }

            val formatter = DateTimeFormatter.ofPattern(pattern, localeVN)
            createdDateTime.format(formatter)
        }
    }
}