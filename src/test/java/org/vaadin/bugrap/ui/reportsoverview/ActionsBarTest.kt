package org.vaadin.bugrap.ui.reportsoverview

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.vaadin.bugrap.cdi.events.ProjectChangeEvent
import org.vaadin.bugrap.cdi.events.ReportsRefreshEvent
import org.vaadin.bugrap.cdi.events.SearchEvent
import org.vaadin.bugrap.cdi.events.VersionChangeEvent
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.Filter
import org.vaadin.bugrap.core.verifyObserver
import org.vaadin.bugrap.domain.BugrapRepository
import org.vaadin.bugrap.domain.RepositorySearchFacade
import org.vaadin.bugrap.domain.entities.Project
import org.vaadin.bugrap.domain.entities.ProjectVersion
import javax.enterprise.event.Event
import kotlin.test.assertEquals

/**
 * @author oladeji
 */
class ActionsBarTest {

  private lateinit var sut: ActionsBar
  private lateinit var appModel: ApplicationModel

  companion object {

    @BeforeClass
    @JvmStatic
    fun setupRepo() {
      ApplicationModel.bugrapRepository = mock<BugrapRepository>().apply {
        doReturn(setOf(Project())).whenever(this).findProjects()
        doReturn(25L).whenever(this).countReports(any<Project>())
        doReturn(13L).whenever(this).countReports(any<ProjectVersion>())
      }
    }
  }

  @Before
  fun init() {
    appModel = spy(ApplicationModel(mock<RepositorySearchFacade>(), Filter(), mock<Event<ReportsRefreshEvent>>()))
    appModel.setup()
    sut = spy(ActionsBar(appModel, mock<Event<SearchEvent>>()))
  }

  @Test
  fun setup() {
    println("setup")

    println("  -> Verify the value of projectCountLabel is set")
    assertEquals("", sut.projectCountLabel.value)
    sut.setup()
    assertEquals("25", sut.projectCountLabel.value)
  }

  @Test
  fun countReports() {
    println("countReports")

    println("  -> Verify project count is fetched when no version is selected")
    sut.updateReportCount(ProjectChangeEvent(Project()))
    assertEquals("25", sut.countReports())

    println("  -> Verify version count is fetched when a version is selected")
    val version = ProjectVersion()
    doReturn(version).whenever(appModel).getSelectedVersion()
    sut.updateReportCount(VersionChangeEvent(version))
    assertEquals("13", sut.countReports())
  }

  @Test
  fun updateReportCount_projectChange() {
    println("updateReportCount_projectChange")

    verifyObserver(sut, "updateReportCount", ProjectChangeEvent::class)

    doReturn("25").whenever(sut).countReports()

    assertEquals("", sut.projectCountLabel.value)
    sut.updateReportCount(ProjectChangeEvent(Project()))

    println("  -> Verify report count for project is fetched from repo on project selection")
    verify(sut, times(1)).countReports()

    println("  -> Verify project count label gets updated with data from repo")
    assertEquals("25", sut.projectCountLabel.value)
  }

  @Test
  fun updateReportCount_versionChange() {
    println("updateReportCount_versionChange")

    verifyObserver(sut, "updateReportCount", VersionChangeEvent::class)

    doReturn("25").whenever(sut).countReports()

    assertEquals("", sut.projectCountLabel.value)
    sut.updateReportCount(VersionChangeEvent(null))

    println("  -> Verify report count for version is fetched from repo on version selection")
    verify(sut, times(1)).countReports()

    println("  -> Verify project count label gets updated with data from repo")
    assertEquals("25", sut.projectCountLabel.value)
  }
}