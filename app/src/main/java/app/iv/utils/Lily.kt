package app.iv.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MediaInfoFormatter {
	
    fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(Locale.getDefault(), "%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
    
    fun formatDate(dateAdded: Long): String {
        val adjustedDateAdded = if (dateAdded < 1_000_000_000_000L) dateAdded * 1000L else dateAdded
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return formatter.format(Date(adjustedDateAdded))
    }
    
    fun formatDuration(duration: Long): String {
        val totalSeconds = duration / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
