package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private val kolkataTimeZone = TimeZone.getTimeZone("Asia/Kolkata")

    fun formatMatchTime(timestampMs: Long): String {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a 'IST'", Locale.ENGLISH)
        sdf.timeZone = kolkataTimeZone
        return sdf.format(Date(timestampMs))
    }

    fun formatShortDate(timestampMs: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        sdf.timeZone = kolkataTimeZone
        return sdf.format(Date(timestampMs))
    }

    fun formatTimeOnly(timestampMs: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
        sdf.timeZone = kolkataTimeZone
        return sdf.format(Date(timestampMs))
    }

    fun formatCountdown(targetTimeMs: Long): String {
        val diff = targetTimeMs - System.currentTimeMillis()
        if (diff <= 0) return "Started / Live"
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff % (1000 * 60 * 60)) / (1000 * 60)
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }
}
