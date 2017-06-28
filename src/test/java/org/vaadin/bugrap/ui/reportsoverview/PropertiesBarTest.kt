package org.vaadin.bugrap.ui.reportsoverview

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import com.vaadin.server.ExternalResource
import org.junit.Before
import org.junit.Test
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.CONTEXT_ROOT
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
import javax.enterprise.event.Event
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author oladeji
 */
class PropertiesBarTest {

  private lateinit var sut: PropertiesBar
  private lateinit var appModel: ApplicationModel

  @Before
  fun init() {
    appModel = spy(ApplicationModel(mock<RepositorySearchFacade>(), Filter(), mock<Event<ReportsRefreshEvent>>()))
    appModel.setup()
    sut = spy(PropertiesBar(appModel, mock<Event<ReportsRefreshEvent>>()))
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

    val report = Report().apply {
      assigned = Reporter()
      author = assigned
      description = "My fancy report"
      occursIn = ProjectVersion()
      priority = BLOCKER
      project = Project()
      reportedTimestamp = currentTimeAsDate()
      status = FIXED
      summary = "Broken login button"
      timestamp = reportedTimestamp
      type = BUG
      version = occursIn
    }

    sut.updateProperties(ReportsSelectionEvent(setOf(report)))

    println("  -> Verify is visible")
    assertTrue(sut.isVisible)

    println("  -> Verify open in new window link is visible")
    assertTrue(sut.newWindowLink.isVisible)

    println("  -> Verify open in new window link has the selected report's summary as caption")
    assertEquals(report.summary, sut.newWindowLink.caption)

    println("  -> Verify open in new window link points to the right resource")
    assertEquals(CONTEXT_ROOT + report.id, (sut.newWindowLink.resource as ExternalResource).url)

    println("  -> Verify report detail label is invisible")
    assertFalse(sut.reportDetailLabel.isVisible)

    println("  -> Verify fixed text label is invisible")
    assertFalse(sut.fixedTextLabel.isVisible)

    println("  -> Verify controls are initialized to the report values")
    assertEquals(report.priority, sut.priorityControl.selectedItem.get())
    assertEquals(report.type, sut.typeControl.selectedItem.get())
    assertEquals(report.status, sut.statusControl.selectedItem.get())
    assertEquals(report.assigned, sut.assigneeControl.selectedItem.get())
    assertEquals(report.version, sut.versionControl.selectedItem.get())
  }

  @Test
  fun updateProperties_reportsSelectionEvent_multipleSelection() {
    println("updateProperties_reportsSelectionEvent_multipleSelection")

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
    assertFalse(sut.statusControl.selectedItem.isPresent())
    assertFalse(sut.versionControl.selectedItem.isPresent)
  }

  @Test
  fun updateProperties_reportsRefreshEvent() {
    println("updateProperties_reportsRefreshEvent")

    verifyObserver(sut, "updateProperties", ReportsRefreshEvent::class)

    val event = ReportsRefreshEvent()

    println("  -> Verify is invisible when no report is selected")
    doReturn(emptySet<Report>()).whenever(appModel).getSelectedReports()
    sut.updateProperties(event)
    assertFalse(sut.isVisible)

    println("  -> Verify is visible when one or more reports are selected")
    doReturn(setOf(Report())).whenever(appModel).getSelectedReports()
    sut.updateProperties(event)
    assertTrue(sut.isVisible)
  }
}