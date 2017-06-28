package org.vaadin.bugrap.ui.reportsoverview

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.vaadin.data.provider.DataProvider
import com.vaadin.data.provider.Query
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.Filter
import org.vaadin.bugrap.core.verifyObserver
import org.vaadin.bugrap.domain.BugrapRepository
import org.vaadin.bugrap.domain.RepositorySearchFacade
import org.vaadin.bugrap.domain.entities.Project
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.cdi.events.ProjectChangeEvent
import org.vaadin.bugrap.cdi.events.ReportsRefreshEvent
import org.vaadin.bugrap.cdi.events.VersionChangeEvent
import java.util.stream.Collectors.toSet
import javax.enterprise.event.Event
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * @author oladeji
 */
class VersionBarTest {

  private lateinit var sut: VersionBar
  private lateinit var appModel: ApplicationModel

  companion object {

    val version1 = ProjectVersion().apply { id = 1 }
    val version2 = ProjectVersion().apply { id = 2 }

    @BeforeClass
    @JvmStatic
    fun setupRepo() {
      ApplicationModel.bugrapRepository = mock<BugrapRepository>().apply {
        doReturn(setOf(Project())).whenever(this).findProjects()
        doReturn(setOf(version1, version2)).whenever(this).findProjectVersions(any<Project>())
      }
    }
  }

  @Before
  fun init() {
    appModel = spy(ApplicationModel(mock<RepositorySearchFacade>(), Filter(), mock<Event<ReportsRefreshEvent>>()))
    appModel.setup()
    sut = spy(VersionBar(appModel, mock<Event<VersionChangeEvent>>()))
  }

  @Test
  fun updateVersionComponents() {
    println("updateVersionComponents")

    verifyObserver(sut, "updateVersionComponents", ProjectChangeEvent::class)

    sut.versionSelector.setSelectedItem(version1)
    sut.updateVersionComponents(ProjectChangeEvent(Project()))

    println("  -> Verify selected version is cleared on project change")
    assertFalse(sut.versionSelector.selectedItem.isPresent)

    println("  -> Verify versions for selected projects are fetched from repository")
    verify(ApplicationModel.bugrapRepository, atLeastOnce()).findProjectVersions(any<Project>())

    println("  -> Verify fetched versions applied to version selector")
    val data = (sut.versionSelector.dataProvider as DataProvider<ProjectVersion, Any>).fetch(Query()).collect(toSet())
    assertEquals(setOf(version1, version2), data)

    println("  -> Verify distribution bars are updated")
    verify(sut, times(1)).updateDistributionBars()
  }

  @Test
  fun updateDistributionBars_noVersionSelected() {
    println("updateDistributionBars_noVersionSelected")

    doReturn(null).whenever(appModel).getSelectedVersion()
    doReturn(1L).whenever(ApplicationModel.bugrapRepository).countClosedReports(any<Project>())
    doReturn(2L).whenever(ApplicationModel.bugrapRepository).countOpenedReports(any<Project>())
    doReturn(3L).whenever(ApplicationModel.bugrapRepository).countUnassignedReports(any<Project>())
    
    sut.updateDistributionBars()

    println("  -> Verify distribution bars carry report counts in selected project")
    assertEquals("1", sut.darkProgress.value)
    assertEquals("2", sut.middleProgress.value)
    assertEquals("3", sut.lightProgress.value)
  }

  @Test
  fun updateDistributionBars_versionSelected() {
    println("updateDistributionBars_versionSelected")

    doReturn(ProjectVersion()).whenever(appModel).getSelectedVersion()
    doReturn(4L).whenever(ApplicationModel.bugrapRepository).countClosedReports(any<ProjectVersion>())
    doReturn(5L).whenever(ApplicationModel.bugrapRepository).countOpenedReports(any<ProjectVersion>())
    doReturn(6L).whenever(ApplicationModel.bugrapRepository).countUnassignedReports(any<ProjectVersion>())

    sut.updateDistributionBars()

    println("  -> Verify distribution bars carry report counts in selected version")
    assertEquals("4", sut.darkProgress.value)
    assertEquals("5", sut.middleProgress.value)
    assertEquals("6", sut.lightProgress.value)
  }

  @Test
  fun updateDistributionBars_noMatchingProjects() {
    println("updateDistributionBars_noMatchingProjects")

    doReturn(null).whenever(appModel).getSelectedVersion()
    doReturn(0L).whenever(ApplicationModel.bugrapRepository).countClosedReports(any<Project>())
    doReturn(0L).whenever(ApplicationModel.bugrapRepository).countOpenedReports(any<Project>())
    doReturn(0L).whenever(ApplicationModel.bugrapRepository).countUnassignedReports(any<Project>())

    sut.updateDistributionBars()

    println("  -> Verify distribution bars width equally sized")
    assertEquals(33f, sut.darkProgress.width)
    assertEquals(PERCENTAGE, sut.darkProgress.widthUnits)

    assertEquals(33f, sut.middleProgress.width)
    assertEquals(PERCENTAGE, sut.middleProgress.widthUnits)

    assertEquals(34f, sut.lightProgress.width)
    assertEquals(PERCENTAGE, sut.lightProgress.widthUnits)
  }

  @Test
  fun updateDistributionBars_withMatchingProjects() {
    println("updateDistributionBars_withMatchingProjects")

    doReturn(null).whenever(appModel).getSelectedVersion()
    doReturn(10L).whenever(ApplicationModel.bugrapRepository).countClosedReports(any<Project>())
    doReturn(15L).whenever(ApplicationModel.bugrapRepository).countOpenedReports(any<Project>())
    doReturn(25L).whenever(ApplicationModel.bugrapRepository).countUnassignedReports(any<Project>())

    sut.updateDistributionBars()

    println("  -> Verify distribution bars width relatively sized by project state counts")
    assertEquals(20f, sut.darkProgress.width)
    assertEquals(PERCENTAGE, sut.darkProgress.widthUnits)

    assertEquals(30f, sut.middleProgress.width)
    assertEquals(PERCENTAGE, sut.middleProgress.widthUnits)

    assertEquals(50f, sut.lightProgress.width)
    assertEquals(PERCENTAGE, sut.lightProgress.widthUnits)
  }
}