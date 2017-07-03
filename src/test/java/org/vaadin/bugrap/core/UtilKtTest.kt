package org.vaadin.bugrap.core

import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.test.assertEquals

/**
 * @author oladeji
 */
class UtilKtTest {

  @Test
  fun userFriendlyTimeDiff() {
    println("userFriendlyTimeDiff")

    val currentTime = Clock.freeze()

    assertEquals("Just now", userFriendlyTimeDiff(toDate(currentTime)))
    assertEquals("5 seconds ago", userFriendlyTimeDiff(toDate(currentTime.minusSeconds(5))))
    assertEquals("1 minute ago", userFriendlyTimeDiff(toDate(currentTime.minusMinutes(1))))
    assertEquals("2 minutes ago", userFriendlyTimeDiff(toDate(currentTime.minusMinutes(2))))
    assertEquals("1 hour ago", userFriendlyTimeDiff(toDate(currentTime.minusHours(1))))
    assertEquals("2 hours ago", userFriendlyTimeDiff(toDate(currentTime.minusHours(2))))
    assertEquals("1 day ago", userFriendlyTimeDiff(toDate(currentTime.minusDays(1))))
    assertEquals("2 days ago", userFriendlyTimeDiff(toDate(currentTime.minusDays(2))))
    assertEquals("1 week ago", userFriendlyTimeDiff(toDate(currentTime.minusWeeks(1))))
    assertEquals("2 weeks ago", userFriendlyTimeDiff(toDate(currentTime.minusWeeks(2))))
    assertEquals("1 month ago", userFriendlyTimeDiff(
        toDate(currentTime.minusMonths(1).minusDays(4))))
    // Edge cases that can cause flaky tests. We really don't care too much about precision.
    assertEquals("2 months ago", userFriendlyTimeDiff(
        toDate(currentTime.minusMonths(2).minusDays(4))))
    assertEquals("1 year ago", userFriendlyTimeDiff(
        toDate(currentTime.minusYears(1).minusDays(1))))
    assertEquals("30 years ago", userFriendlyTimeDiff(
        toDate(currentTime.minusYears(30).minusDays(1))))
  }

  fun toDate(dateTime: LocalDateTime) = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
}