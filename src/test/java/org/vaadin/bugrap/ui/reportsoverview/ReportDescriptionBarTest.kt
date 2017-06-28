package org.vaadin.bugrap.ui.reportsoverview

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.Clock
import org.vaadin.bugrap.core.Clock.Companion.currentTimeAsDate
import org.vaadin.bugrap.core.Filter
import org.vaadin.bugrap.core.verifyObserver
import org.vaadin.bugrap.domain.RepositorySearchFacade
import org.vaadin.bugrap.domain.entities.Project
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.domain.entities.Report.Priority.BLOCKER
import org.vaadin.bugrap.domain.entities.Report.Status.FIXED
import org.vaadin.bugrap.domain.entities.Report.Status.WONT_FIX
import org.vaadin.bugrap.domain.entities.Report.Type.BUG
import org.vaadin.bugrap.domain.entities.Reporter
import org.vaadin.bugrap.cdi.events.ReportsRefreshEvent
import org.vaadin.bugrap.cdi.events.ReportsSelectionEvent
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.enterprise.event.Event
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author oladeji
 */
class ReportDescriptionBarTest {

  private lateinit var appModel: ApplicationModel
  private lateinit var sut: ReportDescriptionBar

  @Before
  fun init() {
    appModel = spy(ApplicationModel(mock<RepositorySearchFacade>(), Filter(), mock<Event<ReportsRefreshEvent>>()))
    sut = ReportDescriptionBar(appModel)
    Clock.unfreeze()
  }

  @After
  fun cleanup() = Clock.unfreeze()

  @Test
  fun updateUI_reportsSelectionEvent() {
    println("updateUI_reportsSelectionEvent")

    verifyObserver(sut, "updateUI", ReportsSelectionEvent::class)

    val report1 = Report().apply {
      id = 1
      assigned = Reporter()
      author = assigned
      description = "My fancy report"
      occursIn = ProjectVersion().apply { id = 1 }
      priority = BLOCKER
      project = Project()
      reportedTimestamp = currentTimeAsDate()
      status = FIXED
      summary = "Broken login button"
      timestamp = reportedTimestamp
      type = BUG
      version = occursIn
    }

    val report2 = Report().apply {
      id = 2
      assigned = Reporter()
      author = assigned
      description = "My fancy report"
      occursIn = ProjectVersion().apply { id = 2 }
      priority = BLOCKER
      project = Project()
      reportedTimestamp = currentTimeAsDate()
      status = WONT_FIX
      summary = "Broken login button"
      timestamp = reportedTimestamp
      type = BUG
      version = occursIn
    }

    println("  -> Verify is invisible when no reports are selected")
    sut.updateUI(ReportsSelectionEvent(emptySet<Report>()))
    assertFalse(sut.isVisible)

    println("  -> Verify is visible when a single report is selected")
    sut.updateUI(ReportsSelectionEvent(setOf(report1)))
    assertTrue(sut.isVisible)

    println("  -> Verify is invisible when multiple reports are selected")
    sut.updateUI(ReportsSelectionEvent(setOf(report1, report2)))
    assertFalse(sut.isVisible)

    Clock.freeze()
    sut.updateUI(ReportsSelectionEvent(setOf(report1)))

    println("  -> Verify selected report description displayed in text area")
    assertEquals(report1.description, sut.descriptionArea.value)

    println("  -> Verify \"Unknown Reporter\" shown along with time diff when reporter's name is unknown")
    assertEquals("Unknown Reporter (${ReportDescriptionBar.userFriendlyTimeDiff(report1.reportedTimestamp)})",
        sut.infoLabel.value)

    println("  -> Verify reporter's name shown along with time diff when reporter's name is known")
    report1.author.name = "Sayo Oladeji"
    sut.updateUI(ReportsSelectionEvent(setOf(report1)))
    assertEquals("Sayo Oladeji (${ReportDescriptionBar.userFriendlyTimeDiff(report1.reportedTimestamp)})",
        sut.infoLabel.value)
  }

  @Test
  fun updateUI_reportsRefreshEvent() {
    println("updateUI_reportsRefreshEvent")

    verifyObserver(sut, "updateUI", ReportsRefreshEvent::class)

    val event = ReportsRefreshEvent()

    println("  -> Verify is invisible when no reports are selected")
    doReturn(emptySet<Report>()).whenever(appModel).getSelectedReports()
    sut.updateUI(event)
    assertFalse(sut.isVisible)

    println("  -> Verify is visible when a single report is selected")
    doReturn(setOf(Report())).whenever(appModel).getSelectedReports()
    sut.updateUI(event)
    assertTrue(sut.isVisible)

    println("  -> Verify is invisible when multiple reports are selected")
    doReturn(setOf(Report(), Report().apply { id = 1 })).whenever(appModel).getSelectedReports()
    sut.updateUI(event)
    assertFalse(sut.isVisible)
  }

  @Test
  fun userFriendlyTimeDiff() {
    val currentTime = Clock.freeze()

    assertEquals("Just now", ReportDescriptionBar.userFriendlyTimeDiff(toDate(currentTime)))
    assertEquals("5 seconds ago", ReportDescriptionBar.userFriendlyTimeDiff(toDate(currentTime.minusSeconds(5))))
    assertEquals("1 minute ago", ReportDescriptionBar.userFriendlyTimeDiff(toDate(currentTime.minusMinutes(1))))
    assertEquals("2 minutes ago", ReportDescriptionBar.userFriendlyTimeDiff(toDate(currentTime.minusMinutes(2))))
    assertEquals("1 hour ago", ReportDescriptionBar.userFriendlyTimeDiff(toDate(currentTime.minusHours(1))))
    assertEquals("2 hours ago", ReportDescriptionBar.userFriendlyTimeDiff(toDate(currentTime.minusHours(2))))
    assertEquals("1 day ago", ReportDescriptionBar.userFriendlyTimeDiff(toDate(currentTime.minusDays(1))))
    assertEquals("2 days ago", ReportDescriptionBar.userFriendlyTimeDiff(toDate(currentTime.minusDays(2))))
    assertEquals("1 week ago", ReportDescriptionBar.userFriendlyTimeDiff(toDate(currentTime.minusWeeks(1))))
    assertEquals("2 weeks ago", ReportDescriptionBar.userFriendlyTimeDiff(toDate(currentTime.minusWeeks(2))))
    assertEquals("1 month ago", ReportDescriptionBar.userFriendlyTimeDiff(toDate(currentTime.minusMonths(1))))
    // Edge cases that can cause flaky tests. We really don't care too much about precision.
    assertEquals("2 months ago", ReportDescriptionBar.userFriendlyTimeDiff(
        toDate(currentTime.minusMonths(2).minusDays(4))))
    assertEquals("1 year ago", ReportDescriptionBar.userFriendlyTimeDiff(
        toDate(currentTime.minusYears(1).minusDays(1))))
    assertEquals("30 years ago", ReportDescriptionBar.userFriendlyTimeDiff(
        toDate(currentTime.minusYears(30).minusDays(1))))
  }

  fun toDate(dateTime: LocalDateTime) = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
}