package com.mindInk.app.ui.articles

import java.util.Locale

fun formatCount(count: Int): String = when {
    count < 1_000 -> count.toString()
    count < 999_500 -> compact(count / 1_000.0, "k")
    else -> compact(count / 1_000_000.0, "M")
}

private fun compact(value: Double, suffix: String): String =
    String.format(Locale.US, "%.1f", value).removeSuffix(".0") + suffix

fun postedAgoText(
    createdAtMillis: Long,
    nowMillis: Long = System.currentTimeMillis()
): String {
    if (createdAtMillis <= 0L) return ""
    val minutes = ((nowMillis - createdAtMillis) / 60_000L).coerceAtLeast(0L)
    return when {
        minutes < 1L -> "Postado agora"
        minutes < 60L -> "Postado há ${minutes} min"
        minutes < 1_440L -> {
            val hours = minutes / 60L
            if (hours == 1L) "Postado há 1 h" else "Postado há $hours h"
        }
        minutes < 43_200L -> {
            val days = minutes / 1_440L
            if (days == 1L) "Postado há 1 dia" else "Postado há $days dias"
        }
        else -> {
            val months = minutes / 43_200L
            if (months == 1L) "Postado há 1 mês" else "Postado há $months meses"
        }
    }
}
