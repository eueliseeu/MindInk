package com.mindInk.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.mindInk.app.R
import java.util.Locale

private const val MINUTE_MS = 60_000L
private const val MINUTES_PER_HOUR = 60L
private const val MINUTES_PER_DAY = 1_440L
private const val MINUTES_PER_MONTH = 43_200L

fun formatCount(count: Int): String = when {
    count < 1_000 -> count.toString()
    count < 999_500 -> compact(count / 1_000.0, "k")
    else -> compact(count / 1_000_000.0, "M")
}

private fun compact(value: Double, suffix: String): String =
    String.format(Locale.US, "%.1f", value).removeSuffix(".0") + suffix

@Composable
fun postedAgoText(
    createdAtMillis: Long,
    nowMillis: Long = System.currentTimeMillis()
): String {
    val minutes = ((nowMillis - createdAtMillis) / MINUTE_MS).coerceAtLeast(0L)

    return when {
        minutes < 1L -> stringResource(R.string.posted_just_now)

        minutes < MINUTES_PER_HOUR -> {
            val value = minutes.toInt()
            pluralStringResource(R.plurals.posted_minutes_ago, value, value)
        }

        minutes < MINUTES_PER_DAY -> {
            val value = (minutes / MINUTES_PER_HOUR).toInt()
            pluralStringResource(R.plurals.posted_hours_ago, value, value)
        }

        minutes < MINUTES_PER_MONTH -> {
            val value = (minutes / MINUTES_PER_DAY).toInt()
            pluralStringResource(R.plurals.posted_days_ago, value, value)
        }

        else -> {
            val value = (minutes / MINUTES_PER_MONTH).toInt()
            pluralStringResource(R.plurals.posted_months_ago, value, value)
        }
    }
}
