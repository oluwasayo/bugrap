package org.vaadin.bugrap.ui.shared

import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.vaadin.bugrap.core.Clock
import org.vaadin.bugrap.ui.report1
import org.vaadin.bugrap.ui.reportdetail.DetailDescriptionBar
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import kotlin.test.assertEquals
import org.vaadin.bugrap.ui.shared.AbstractDescriptionBar.Companion.userFriendlyTimeDiff as timeDiff

/**
 * @author oladeji
 */
class AbstractDescriptionBarTest {

  private lateinit var sut: AbstractDescriptionBar

  @Before
  fun init() {
    sut = spy(DetailDescriptionBar())
  }

  @Test
  fun setReport() {
    println("setReport")

    doNothing().whenever(sut).updateUI()

    sut.report = report1

    println("  -> Verify updateUI called")
    verify(sut, times(1)).updateUI()
  }

  @Test
  fun updateUI() {
    println("updateUI")

    report1.author.name = null
    sut.report = report1

    println("  -> Verify selected report description displayed in text area")
    assertEquals(report1.description, sut.descriptionArea.value)

    println("  -> Verify \"Unknown Reporter\" shown along with time diff when reporter's name is unknown")
    assertEquals("Unknown Reporter (${timeDiff(report1.reportedTimestamp)})",
        sut.infoLabel.value)

    println("  -> Verify reporter's name shown along with time diff when reporter's name is known")
    report1.author.name = "Sayo Oladeji"

    sut.updateUI()
    assertEquals("Sayo Oladeji (${timeDiff(report1.reportedTimestamp)})",
        sut.infoLabel.value)
  }

  @Test
  fun userFriendlyTimeDiff() {
    println("userFriendlyTimeDiff")

    val currentTime = Clock.freeze()

    assertEquals("Just now", timeDiff(toDate(currentTime)))
    assertEquals("5 seconds ago", timeDiff(toDate(currentTime.minusSeconds(5))))
    assertEquals("1 minute ago", timeDiff(toDate(currentTime.minusMinutes(1))))
    assertEquals("2 minutes ago", timeDiff(toDate(currentTime.minusMinutes(2))))
    assertEquals("1 hour ago", timeDiff(toDate(currentTime.minusHours(1))))
    assertEquals("2 hours ago", timeDiff(toDate(currentTime.minusHours(2))))
    assertEquals("1 day ago", timeDiff(toDate(currentTime.minusDays(1))))
    assertEquals("2 days ago", timeDiff(toDate(currentTime.minusDays(2))))
    assertEquals("1 week ago", timeDiff(toDate(currentTime.minusWeeks(1))))
    assertEquals("2 weeks ago", timeDiff(toDate(currentTime.minusWeeks(2))))
    assertEquals("1 month ago", timeDiff(toDate(currentTime.minusMonths(1))))
    // Edge cases that can cause flaky tests. We really don't care too much about precision.
    assertEquals("2 months ago", timeDiff(
        toDate(currentTime.minusMonths(2).minusDays(4))))
    assertEquals("1 year ago", timeDiff(
        toDate(currentTime.minusYears(1).minusDays(1))))
    assertEquals("30 years ago", timeDiff(
        toDate(currentTime.minusYears(30).minusDays(1))))
  }

  fun toDate(dateTime: LocalDateTime) = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
}