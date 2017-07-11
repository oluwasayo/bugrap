package org.vaadin.bugrap.core

import java.util.Date

/**
 *
 * @author oladeji
 */
fun userFriendlyTimeDiff(date: Date): String {
  val diff = Clock.currentTimeAsDate().time - date.time
  var result = when (diff) {
    in 0..4_999 -> "Just now"
    in 5_000..59_999 -> "${diff / 1_000} seconds ago"
    in 60_000..3_599_999 -> "${diff / 60_000} minutes ago"
    in 3_600_000..86_399_999 -> "${diff / 3_600_000} hours ago"
    in 86_400_000..604_799_999 -> "${diff / 86_400_000} days ago"
    in 604_800_000..2_678_399_999L -> "${diff / 604_800_000} weeks ago"
    in 2_678_400_000L..31_557_599_999L -> "${diff / 2_678_400_000L} months ago"
    else -> "${diff / 31_557_600_000L} years ago"
  }

  if (result.startsWith("1 ")) { // Singularize.
    result = StringBuilder(result).deleteCharAt(result.length - 5).toString()
  }

  return result
}

fun addTimestampToFilename(fileName: String): String {
  if (fileName.contains(".")) {
    return fileName.substring(0, fileName.lastIndexOf(".")) + "_" + System.currentTimeMillis() +
        fileName.substring(fileName.lastIndexOf("."))
  }

  return fileName + "_" + System.currentTimeMillis()
}

fun removeTimestampFromFileName(fileName: String): String {
  if (!fileName.contains('_')) return fileName
  var result = fileName.substring(0, fileName.lastIndexOf("_"))
  if (fileName.contains(".")) {
    result += fileName.substring(fileName.lastIndexOf("."))
  }

  return result
}