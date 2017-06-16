package org.vaadin.bugrap

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.vaadin.data.provider.DataProvider
import com.vaadin.data.provider.Query
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.shared.data.sort.SortDirection
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.Clock
import org.vaadin.bugrap.core.PRIORITY
import org.vaadin.bugrap.core.VERSION
import org.vaadin.bugrap.core.verifyObserver
import org.vaadin.bugrap.domain.entities.Project
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.domain.entities.Report.Priority.BLOCKER
import org.vaadin.bugrap.domain.entities.Report.Status.FIXED
import org.vaadin.bugrap.domain.entities.Report.Status.WONT_FIX
import org.vaadin.bugrap.domain.entities.Report.Type.BUG
import org.vaadin.bugrap.domain.entities.Reporter
import org.vaadin.bugrap.events.ReportsRefreshEvent
import org.vaadin.bugrap.events.ReportsSelectionEvent
import org.vaadin.bugrap.ui.reportsoverview.ActionsBar
import org.vaadin.bugrap.ui.reportsoverview.FilterBar
import org.vaadin.bugrap.ui.reportsoverview.ProjectSelectorBar
import org.vaadin.bugrap.ui.reportsoverview.PropertiesBar
import org.vaadin.bugrap.ui.reportsoverview.ReportDescriptionBar
import org.vaadin.bugrap.ui.reportsoverview.VersionBar
import java.util.stream.Collectors.toSet
import javax.enterprise.event.Event
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * @author oladeji
 */
class ReportsOverviewUITest {

  private val report1 = Report().apply {
    id = 1
    assigned = Reporter()
    author = assigned
    description = "My fancy report"
    occursIn = ProjectVersion().apply { id = 1 }
    priority = BLOCKER
    project = Project()
    reportedTimestamp = Clock.currentTimeAsDate()
    status = FIXED
    summary = "Broken login button"
    timestamp = reportedTimestamp
    type = BUG
    version = occursIn
  }

  private val report2 = Report().apply {
    id = 2
    assigned = Reporter()
    author = assigned
    description = "My fancy report"
    occursIn = ProjectVersion().apply { id = 2 }
    priority = BLOCKER
    project = Project()
    reportedTimestamp = Clock.currentTimeAsDate()
    status = WONT_FIX
    summary = "Broken login button"
    timestamp = reportedTimestamp
    type = BUG
    version = occursIn
  }

  private lateinit var appModel: ApplicationModel
  private lateinit var sut: ReportsOverviewUI

  @Before
  fun init() {
    appModel = mock<ApplicationModel>()
    sut = spy(ReportsOverviewUI(
        appModel,
        mock<ProjectSelectorBar>(),
        mock<ActionsBar>(),
        mock<VersionBar>(),
        mock<FilterBar>(),
        mock<PropertiesBar>(),
        mock<ReportDescriptionBar>(),
        mock<Event<ReportsSelectionEvent>>()
    ))
  }

  @After
  fun cleanup() {
    reset(appModel)
  }

  @Test
  fun updateGrid_noVersionSelected() {
    println("updateGrid_noVersionSelected")

    verifyObserver(sut, "updateGrid", ReportsRefreshEvent::class)

    doReturn(setOf(report1, report2)).whenever(appModel).getReports()
    doReturn(setOf(report1)).whenever(appModel).getSelectedReports()
    doReturn(null).whenever(appModel).getSelectedVersion()

    if (sut.table.getColumn(VERSION) != null) sut.table.removeColumn(VERSION)

    sut.updateGrid(ReportsRefreshEvent())

    println("  -> Verify table selection is cleared")
    assertTrue(sut.table.selectedItems.isEmpty())

    println("  -> Verify table is repopulated with reports from the application model")
    assertEquals(setOf(report1, report2),
        (sut.table.dataProvider as DataProvider<Report, Any>).fetch(Query()).collect(toSet()))

    println("  -> Verify Version column is added to table if absent")
    assertNotNull(sut.table.getColumn(VERSION))

    println("  -> Verify table's first column is Version followed by Priority")
    assertEquals(VERSION, sut.table.columns[0].caption.toLowerCase())
    assertEquals(PRIORITY, sut.table.columns[1].caption.toLowerCase())

    println("  -> Verify table is sorted by Version (ASC) and Priority (DESC)")
    assertEquals(SortDirection.ASCENDING, sut.table.sortOrder[0].direction)
    assertEquals(sut.table.getColumn(VERSION), sut.table.sortOrder[0].sorted)
    assertEquals(SortDirection.DESCENDING, sut.table.sortOrder[1].direction)
    assertEquals(sut.table.getColumn(PRIORITY), sut.table.sortOrder[1].sorted)

    println("  -> Verify vertical split is updated to reflect new table selection")
    verify(appModel, times(1)).getSelectedReports()
  }

  @Test
  fun updateGrid_versionSelected() {
    println("updateGrid_versionSelected")

    verifyObserver(sut, "updateGrid", ReportsRefreshEvent::class)

    doReturn(setOf(report1, report2)).whenever(appModel).getReports()
    doReturn(setOf(report1)).whenever(appModel).getSelectedReports()
    doReturn(ProjectVersion()).whenever(appModel).getSelectedVersion()

    if (sut.table.getColumn(VERSION) == null) sut.table.addColumn(VERSION)

    sut.updateGrid(ReportsRefreshEvent())

    println("  -> Verify table selection is cleared")
    assertTrue(sut.table.selectedItems.isEmpty())

    println("  -> Verify table is repopulated with reports from the application model")
    assertEquals(setOf(report1, report2),
        (sut.table.dataProvider as DataProvider<Report, Any>).fetch(Query()).collect(toSet()))

    println("  -> Verify Version column is removed to table if present")
    assertNull(sut.table.getColumn(VERSION))

    println("  -> Verify table's first column is Priority")
    assertEquals(PRIORITY, sut.table.columns[0].caption.toLowerCase())

    println("  -> Verify table is sorted by Priority (DESC)")
    assertEquals(SortDirection.DESCENDING, sut.table.sortOrder[0].direction)
    assertEquals(sut.table.getColumn(PRIORITY), sut.table.sortOrder[0].sorted)

    println("  -> Verify vertical split is updated to reflect new table selection")
    verify(appModel, times(1)).getSelectedReports()
  }

  @Test
  fun updateSplit() {
    println("updateSplit")

    val sut = spy<ReportsOverviewUI>()

    verifyObserver(sut, "updateSplit", ReportsSelectionEvent::class)

    sut.updateSplit(ReportsSelectionEvent(emptySet<Report>()))

    println("  -> Verify vertical split is locked when no reports are selected")
    assertTrue(sut.split.isLocked)

    println("  -> Verify vertical fills screen with the reports grid when no reports are selected")
    assertEquals(100f, sut.split.splitPosition)
    assertEquals(PERCENTAGE, sut.split.splitPositionUnit)

    sut.updateSplit(ReportsSelectionEvent(setOf(Report())))

    println("  -> Verify vertical split is adjustable when only one report is selected")
    assertFalse(sut.split.isLocked)

    println("  -> Verify vertical split position reveals reports details when only one report is selected")
    assertEquals(250f, sut.split.splitPosition)
    assertEquals(PIXELS, sut.split.splitPositionUnit)
    assertTrue(sut.split.isSplitPositionReversed)

    sut.updateSplit(ReportsSelectionEvent(setOf(Report(), Report().apply { id = 1 })))

    println("  -> Verify vertical split is locked when more than one report is selected")
    assertTrue(sut.split.isLocked)

    println("  -> Verify vertical split position reveals reports properties when more than one report is selected")
    assertEquals(105f, sut.split.splitPosition)
    assertEquals(PIXELS, sut.split.splitPositionUnit)
    assertTrue(sut.split.isSplitPositionReversed)
  }
}