package org.vaadin.bugrap.ui.reportsoverview

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import com.vaadin.server.ExternalResource
import org.junit.Before
import org.junit.Test
import org.vaadin.bugrap.cdi.events.ReportsRefreshEvent
import org.vaadin.bugrap.cdi.events.ReportsSelectionEvent
import org.vaadin.bugrap.core.CONTEXT_ROOT
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
class MultiReportPropertiesBarTest {

  private lateinit var sut: MultiReportPropertiesBar

  @Before
  fun init() {
    sut = spy(MultiReportPropertiesBar(mock()))
  }

  @Test
  fun updateProperties_reportsSelectionEvent_noSelection() {
    println("updateProperties_reportsSelectionEvent_noSelection")

    verifyObserver(sut, "updateProperties", ReportsSelectionEvent::class)

    sut.updateProperties(ReportsSelectionEvent(emptySet()))

    println("  -> Verify is not visible")
    assertFalse(sut.isVisible)
  }

  @Test
  fun updateProperties_reportsSelectionEvent_singleSelection() {
    println("updateProperties_reportsSelectionEvent_singleSelection")

    sut.updateProperties(ReportsSelectionEvent(setOf(report1)))

    println("  -> Verify is visible")
    assertTrue(sut.isVisible)

    println("  -> Verify open in new window link is visible")
    assertTrue(sut.newWindowLink.isVisible)

    println("  -> Verify open in new window link has the selected report's summary as caption")
    assertEquals(report1.summary, sut.newWindowLink.caption)

    println("  -> Verify open in new window link points to the right resource")
    assertEquals(CONTEXT_ROOT + "detail?id=" + report1.id, (sut.newWindowLink.resource as ExternalResource).url)

    println("  -> Verify report detail label is invisible")
    assertFalse(sut.reportDetailLabel.isVisible)

    println("  -> Verify fixed text label is invisible")
    assertFalse(sut.fixedTextLabel.isVisible)

    println("  -> Verify controls are initialized to the report values")
    assertEquals(report1.priority, sut.priorityControl.selectedItem.get())
    assertEquals(report1.type, sut.typeControl.selectedItem.get())
    assertEquals(report1.status, sut.statusControl.selectedItem.get())
    assertEquals(report1.assigned, sut.assigneeControl.selectedItem.get())
    assertEquals(report1.version, sut.versionControl.selectedItem.get())
  }

  @Test
  fun updateProperties_reportsSelectionEvent_multipleSelection() {
    println("updateProperties_reportsSelectionEvent_multipleSelection")

    sut.updateProperties(ReportsSelectionEvent(setOf(report1, report2)))

    println("  -> Verify is visible")
    assertTrue(sut.isVisible)

    println("  -> Verify open in new window link is invisible")
    assertFalse(sut.newWindowLink.isVisible)

    println("  -> Verify report detail label is visible")
    assertTrue(sut.reportDetailLabel.isVisible)

    println("  -> Verify fixed text label is visible")
    assertTrue(sut.fixedTextLabel.isVisible)

    println("  -> Verify controls are selected when values are uniform across selected reports")
    assertEquals(report1.priority, sut.priorityControl.selectedItem.get())
    assertEquals(report1.type, sut.typeControl.selectedItem.get())
    assertEquals(report1.assigned, sut.assigneeControl.selectedItem.get())

    println("  -> Verify controls are unselected when values are different across selected reports")
    assertFalse(sut.statusControl.selectedItem.isPresent)
    assertFalse(sut.versionControl.selectedItem.isPresent)
  }

  @Test
  fun updateProperties_reportsRefreshEvent() {
    println("updateProperties_reportsRefreshEvent")

    verifyObserver(sut, "updateProperties", ReportsRefreshEvent::class)

    val event = ReportsRefreshEvent()

    println("  -> Verify is invisible when no report is selected")
    doReturn(emptySet<Report>()).whenever(sut).getSelectedReports()
    sut.updateProperties(event)
    assertFalse(sut.isVisible)

    println("  -> Verify is visible when one or more reports are selected")
    doReturn(setOf(Report())).whenever(sut).getSelectedReports()
    sut.updateProperties(event)
    assertTrue(sut.isVisible)
  }
}