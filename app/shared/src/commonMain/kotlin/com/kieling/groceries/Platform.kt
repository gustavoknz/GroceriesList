package com.kieling.groceries

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getCurrentTimestamp(): Long

fun formatDate(epochMillis: Long): String {
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = localDateTime.dayOfMonth
    val month = localDateTime.monthNumber
    val year = localDateTime.year % 100
    return "$day/$month/$year"
}
