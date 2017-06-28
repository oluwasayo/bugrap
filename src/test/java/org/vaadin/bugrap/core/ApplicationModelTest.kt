package org.vaadin.bugrap.core

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.isNotNull
import com.nhaarman.mockito_kotlin.isNull
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.vaadin.bugrap.domain.BugrapRepository
import org.vaadin.bugrap.domain.RepositorySearchFacade
import org.vaadin.bugrap.domain.entities.Project
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.domain.entities.Report.Status
import org.vaadin.bugrap.domain.entities.Reporter
import org.vaadin.bugrap.cdi.events.FilterChangeEvent
import org.vaadin.bugrap.cdi.events.ProjectChangeEvent
import org.vaadin.bugrap.cdi.events.ReportsRefreshEvent
import org.vaadin.bugrap.cdi.events.ReportsSelectionEvent
import org.vaadin.bugrap.cdi.events.SearchEvent
import org.vaadin.bugrap.cdi.events.VersionChangeEvent
import javax.enterprise.event.Event
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author oladeji
 */
class ApplicationModelTest {

  private lateinit var filter: Filter
  private lateinit var event: Event<ReportsRefreshEvent>
  private lateinit var searchFacade: RepositorySearchFacade
  private lateinit var sut: ApplicationModel

  companion object {
    private val projectA = Project().apply { name = "A" }
    private val projectB = Project().apply { name = "B" }

    @BeforeClass
    @JvmStatic
    fun setupRepo() {
      ApplicationModel.bugrapRepository = mock<BugrapRepository>().apply {
        doReturn(Reporter()).whenever(this).authenticate(anyString(), anyString())
        doReturn(setOf(projectB, projectA)).whenever(this).findProjects()
      }
    }
  }

  @Before
  fun init() {
    filter = Filter()
    event = mock<Event<ReportsRefreshEvent>>()
    searchFacade = spy<RepositorySearchFacade>()

    sut = spy(ApplicationModel(searchFacade, filter, event))
    sut.setup()
    reset(sut)
  }

  @Test
  fun setup() {
    println("@PostConstruct")

    sut.setup()

    println("  -> Verify authentication")
    verify(ApplicationModel.bugrapRepository, atLeastOnce()).authenticate(anyString(), anyString())

    println("  -> Verify projects fetch from repo")
    verify(ApplicationModel.bugrapRepository, atLeastOnce()).findProjects()

    println("  -> Verify lexically first project by name selected by default")
    assertEquals(projectA, sut.getSelectedProject())

    println("  -> Verify version info setup for default project")
    verify(sut, times(1)).updateVersionInfo()

    println("  -> Verify reports refreshed from default project and no event fired")
    verify(sut, times(1)).refreshReports(false)
  }

  @Test
  fun searchReports() {
    println("searchReports")

    val project = Project()
    val version = ProjectVersion()
    sut.apply {
      doReturn(project).whenever(this).getSelectedProject()
      doReturn(version).whenever(this).getSelectedVersion()
    }

    val searchTerm = "Sayo"
    sut.searchReports(SearchEvent(searchTerm))

    println("  -> Verify search facade invoked with null statuses when filter is empty")
    verify(searchFacade, times(1)).search(anyString(), eq(project), eq(version), isNull())

    println("  -> Verify reports refresh event fired")
    verify(event, times(1)).fire(any())

    reset(searchFacade)

    doAnswer {
      println("  -> Verify correct search term argument passed to search facade")
      assertEquals(searchTerm, it.getArgument(0))

      val statuses = it.getArgument<Set<Status>>(3)
      println("  -> Verify correct statuses argument passed to search facade")
      assertEquals(1, statuses.size)
      assertEquals(Status.DUPLICATE, statuses.first())

      emptyList<Report>()
    }.whenever(searchFacade).search(anyString(), eq(project), eq(version), any())

    filter.statuses.add(Status.DUPLICATE)
    sut.searchReports(SearchEvent(searchTerm))

    println("  -> Verify search facade invoked with non-null statuses when filter is not empty")
    verify(searchFacade, times(1)).search(anyString(), eq(project), eq(version), isNotNull())
  }

  @Test
  fun switchProject() {
    println("switchProject")

    verifyObserver(sut, "switchProject", ProjectChangeEvent::class)

    val project = Project()
    sut.apply {
      doReturn(ProjectVersion()).whenever(this).getSelectedVersion()
      doNothing().whenever(this).updateVersionInfo()
      doNothing().whenever(this).refreshReports()
    }

    sut.switchProject(ProjectChangeEvent(project))
    println("  -> Verify selected project updated")
    assertEquals(project, sut.getSelectedProject())

    println("  -> Verify version information updated")
    verify(sut, times(1)).updateVersionInfo()

    println("  -> Verify reports refreshed from newly selected project")
    verify(sut, times(1)).refreshReports()
  }

  @Test
  fun switchVersion() {
    println("switchVersion")

    verifyObserver(sut, "switchVersion", VersionChangeEvent::class)

    doNothing().whenever(sut).refreshReports()

    val version = ProjectVersion()
    sut.switchVersion(VersionChangeEvent(version))

    println("  -> Verify selected version updated")
    assertEquals(version, sut.getSelectedVersion())

    println("  -> Verify reports refreshed from newly selected version")
    verify(sut, times(1)).refreshReports()
  }

  @Test
  fun applyFilter() {
    println("applyFilter")

    verifyObserver(sut, "applyFilter", FilterChangeEvent::class)

    doNothing().whenever(sut).refreshReports()

    sut.applyFilter(FilterChangeEvent())

    println("  -> Verify reports refreshed with updated filter")
    verify(sut, times(1)).refreshReports()
  }

  @Test
  fun updateSelectedReports() {
    println("updateSelectedReports")

    verifyObserver(sut, "updateSelectedReports", ReportsSelectionEvent::class)

    println("  -> Verify selected reports applied")
    assertTrue(sut.getSelectedReports().isEmpty())
    val selectedReports = setOf(Report().apply { id = 1 }, Report().apply { id = 2 })
    sut.updateSelectedReports(ReportsSelectionEvent(selectedReports))
    assertEquals(2, sut.getSelectedReports().size)
  }
}