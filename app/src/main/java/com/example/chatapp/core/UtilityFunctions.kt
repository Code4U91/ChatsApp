package com.example.chatapp.core

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.chatapp.ChatsApplication
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale


// Utils

fun Context.appInstance(): ChatsApplication {
    return applicationContext as ChatsApplication
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f, // adjust distance according to screen
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.White.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim - 200f, y = 0f),
        end = Offset(x = translateAnim, y = 0f)
    )

    this.background(brush)
}

fun Timestamp.toLocalDate(): LocalDate {
    return this.toDate().toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}


fun getMessageStatusIcon(messageStatus: String?): ImageVector {

    return when (messageStatus) {
        "sent" -> Icons.Default.Check
        "delivered" -> Icons.Default.DoneAll
        "seen" -> Icons.Default.DoneAll
        else -> Icons.Default.Schedule
    }
}

fun getMessageIconColor(messageStatus: String?): Color {

    return when (messageStatus) {
        "seen" -> Color(0xFF34B7F1)
        else -> Color.Gray
    }
}

fun formatTimestamp(timestamp: Timestamp): String {
    val instant = Instant.ofEpochSecond(timestamp.seconds)
    val messageDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    val currentDate = LocalDate.now()

    return when {
        messageDate.isEqual(currentDate) -> {
            // Same day → Show time (e.g., "10:30 AM")
            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            instant.atZone(ZoneId.systemDefault()).format(timeFormatter)
        }

        messageDate.isEqual(currentDate.minusDays(1)) -> {
            // Yesterday → Show "Yesterday"
            "Yesterday"
        }

        else -> {
            // Older than yesterday → Show date in MM/dd/yyyy format (e.g., "03/14/2025")
            val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.getDefault())
            messageDate.format(dateFormatter)
        }
    }
}

fun formatTimestampToDateTime(timeMills: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val date = Date(timeMills)
    return sdf.format(date)
}


fun formatCallDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs) // HH:MM:SS
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, secs) // MM:SS
    }
}

fun formatDurationText(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (hours > 0) append("$hours hr ")
        if (minutes > 0) append("$minutes min ")
        if (hours == 0L && minutes == 0L && seconds > 0L) append("$seconds sec")

    }.trim()
}


fun getTimeOnly(timestamp: Timestamp): String {

    val instant = Instant.ofEpochSecond(timestamp.seconds)
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

    return instant.atZone(ZoneId.systemDefault()).format(timeFormatter)

}

fun getDateLabelForMessage(dateMillis: Long): String {

    val messageDate = Instant.ofEpochSecond(dateMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    val today = LocalDate.now()

    return when {
        messageDate.isEqual(today) -> "Today"
        messageDate.isEqual(today.minusDays(1)) -> "Yesterday"
        messageDate.isAfter(today.minusDays(4)) -> messageDate.dayOfWeek.name.lowercase()
            .replaceFirstChar { it.uppercase() }

        else -> messageDate.format(DateTimeFormatter.ofPattern("MMM,d, yyyy"))
    }
}


fun formatOnlineStatusTime(timestampMillis: Long): String {
    val now = Calendar.getInstance()
    val messageTime = Calendar.getInstance().apply { timeInMillis = timestampMillis }

    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    return when {
        isSameDay(now, messageTime) -> "today at ${timeFormat.format(messageTime.time)}"
        isYesterday(now, messageTime) -> "yesterday at ${timeFormat.format(messageTime.time)}"
        else -> dateFormat.format(messageTime.time)
    }
}

private fun isSameDay(now: Calendar, other: Calendar): Boolean {
    return now.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(now: Calendar, other: Calendar): Boolean {
    now.add(Calendar.DAY_OF_YEAR, -1) // Move to yesterday
    val isYesterday = now.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    now.add(Calendar.DAY_OF_YEAR, 1) // Reset to today
    return isYesterday
}

fun checkEmailPattern(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}".toRegex()
    return email.matches(emailPattern)
}
