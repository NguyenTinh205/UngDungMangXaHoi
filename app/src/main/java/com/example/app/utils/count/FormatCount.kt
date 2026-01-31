package com.example.app.utils.count

import android.icu.text.CompactDecimalFormat
import android.icu.text.CompactDecimalFormat.CompactStyle
import java.util.Locale

fun formatCount(count: Int): String {
    if (count < 1000) return count.toString()

    val compactFormat = CompactDecimalFormat.getInstance(
        Locale.getDefault(),           // hoặc Locale.US nếu muốn cố định kiểu Mỹ
        CompactStyle.SHORT
    )

    // Muốn luôn có 1 chữ số thập phân (1.0K thay vì 1K)
    compactFormat.maximumFractionDigits = 1
    compactFormat.minimumFractionDigits = 0  // vẫn giữ 1K nếu không cần thập phân

    return compactFormat.format(count.toLong())  // dùng Long để an toàn
}