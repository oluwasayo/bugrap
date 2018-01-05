package org.vaadin.bugrap.ui.reportsoverview

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.vaadin.bugrap.cdi.events.ReportsRefreshEvent
import org.vaadin.bugrap.cdi.events.ReportsSelectionEvent
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.Clock
import org.vaadin.bugrap.core.Filter
import org.vaadin.bugrap.core.userFriendlyTimeDiff
import org.vaadin.bugrap.core.verifyObserver
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.ui.report1
import org.vaadin.bugrap.ui.report2
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
/**
 * @author oladeji
 */
class OverviewDescriptionBarTest {

  private lateinit var appModel: ApplicationModel
  private lateinit var sut: OverviewDescriptionBar

  @Before
  fun init() {
    appModel = spy(ApplicationModel(mock(), Filter(), mock()))
    sut = OverviewDescriptionBar(appModel)
    Clock.unfreeze()
  }

  @After
  fun cleanup() {
    report1.author.name = null
    Clock.unfreeze()
  }

  @Test
  fun updateUI_reportsSelectionEvent() {
    println("updateUI_reportsSelectionEvent")

    verifyObserver(sut, "updateUI", ReportsSelectionEvent::class)

    println("  -> Verify is invisible when no reports are selected")
    sut.updateUI(ReportsSelectionEvent(emptySet()))
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
    assertEquals("Unknown Reporter (${userFriendlyTimeDiff(report1.reportedTimestamp)})",
        sut.infoLabel.value)

    println("  -> Verify reporter's name shown along with time diff when reporter's name is known")
    report1.author.name = "Sayo Oladeji"
    sut.updateUI(ReportsSelectionEvent(setOf(report1)))
    assertEquals("Sayo Oladeji (${userFriendlyTimeDiff(report1.reportedTimestamp)})",
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
    doReturn(setOf(report1)).whenever(appModel).getSelectedReports()
    sut.updateUI(event)
    assertTrue(sut.isVisible)

    println("  -> Verify is invisible when multiple reports are selected")
    doReturn(setOf(report1, report2)).whenever(appModel).getSelectedReports()
    sut.updateUI(event)
    assertFalse(sut.isVisible)
  }
}
