package org.vaadin.bugrap.ui.shared

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.vaadin.bugrap.cdi.events.ReportsUpdateEvent
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.verifyObserver
import org.vaadin.bugrap.domain.BugrapRepository
import org.vaadin.bugrap.domain.entities.Project
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.domain.entities.Report.Priority.BLOCKER
import org.vaadin.bugrap.domain.entities.Report.Priority.CRITICAL
import org.vaadin.bugrap.domain.entities.Report.Status.INVALID
import org.vaadin.bugrap.ui.reportdetail.SingleReportPropertiesBar
import org.vaadin.bugrap.ui.report1
import org.vaadin.bugrap.ui.report2
import javax.enterprise.event.Event
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * @author oladeji
 */
class AbstractPropertiesBarTest {

  private lateinit var sut: AbstractPropertiesBar

  companion object {

    @BeforeClass
    @JvmStatic
    fun setupRepo() {
      ApplicationModel.bugrapRepository = mock<BugrapRepository>().apply {
        doReturn(setOf(Project())).whenever(this).findProjects()
        doReturn(25L).whenever(this).countReports(any<Project>())
        doReturn(13L).whenever(this).countReports(any<ProjectVersion>())
        doReturn(report1).whenever(this).getReportById(1)
      }
    }
  }

  @Before
  fun init() {
    sut = spy(
        SingleReportPropertiesBar(
            mock<Event<ReportsUpdateEvent>>().apply {
              doNothing().whenever(this).fire(any())
            }
        )
    )
  }

  @Test
  fun setup() {

  }

  @Test
  fun setSelectedReports() {
    println("setSelectedReports")

    doNothing().whenever(sut).refreshControls(any())

    val reports = setOf(Report())

    println("  -> Verify selected reports set")
    sut.setSelectedReports(reports, false)
    assertEquals(sut.getSelectedReports(), reports)

    println("  -> Verify updateControls flag")
    verify(sut, never()).refreshControls(any())
    sut.setSelectedReports(reports)
    verify(sut, times(1)).refreshControls(any())

    println("  -> Verify only one report permitted")
    try {
      sut.setSelectedReports(emptySet())
      fail("IllegalArgumentException expected")
    } catch (expected: Exception) { }
    try {
      sut.setSelectedReports(setOf(Report(), Report().apply { id = 1 }))
      fail("IllegalArgumentException expected")
    } catch (expected: Exception) { }
  }

  @Test
  fun refreshControls() {
    println("refreshControls")

    verifyObserver(sut, "refreshControls", ReportsUpdateEvent::class)

    sut.setSelectedReports(setOf(report1))

    println("  -> Verify event ignored when updated report in event is not in working set")
    sut.refreshControls(ReportsUpdateEvent(setOf(report2)))
    assertEquals(report1.priority, sut.priorityControl.selectedItem.get())
    assertEquals(report1.type, sut.typeControl.selectedItem.get())
    assertEquals(report1.status, sut.statusControl.selectedItem.get())
    assertEquals(report1.assigned, sut.assigneeControl.selectedItem.get())
    assertEquals(report1.version, sut.versionControl.selectedItem.get())

    println("  -> Verify event consumed when updated report in event is in working set")
    report1.status = INVALID
    sut.refreshControls(ReportsUpdateEvent(setOf(report1)))
    assertEquals(INVALID, sut.statusControl.selectedItem.get())
  }

  @Test
  fun saveReports() {
    println("saveReports")

    sut.setSelectedReports(setOf(report1))

    assertEquals(BLOCKER, sut.getSelectedReports().first().priority)

    sut.priorityControl.setSelectedItem(CRITICAL)
    sut.saveReports()

    println("  -> Verify report save and refresh")
    assertEquals(CRITICAL, sut.getSelectedReports().first().priority)
    verify(ApplicationModel.bugrapRepository).getReportById(1)
    verify(sut, times(2)).setSelectedReports(setOf(report1))

    sut.priorityControl.setSelectedItem(BLOCKER)
    sut.saveReports()
  }
}