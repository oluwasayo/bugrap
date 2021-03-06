package org.vaadin.bugrap.core

import org.vaadin.bugrap.cdi.events.FilterChangeEvent
import org.vaadin.bugrap.cdi.events.ProjectChangeEvent
import org.vaadin.bugrap.cdi.events.ReportsRefreshEvent
import org.vaadin.bugrap.cdi.events.ReportsSelectionEvent
import org.vaadin.bugrap.cdi.events.ReportsUpdateEvent
import org.vaadin.bugrap.cdi.events.SearchEvent
import org.vaadin.bugrap.cdi.events.VersionChangeEvent
import org.vaadin.bugrap.domain.BugrapRepository
import org.vaadin.bugrap.domain.BugrapRepository.ReportsQuery
import org.vaadin.bugrap.domain.RepositorySearchFacade
import org.vaadin.bugrap.domain.entities.Project
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.domain.entities.Reporter
import java.io.File
import java.io.Serializable
import javax.annotation.PostConstruct
import javax.enterprise.context.SessionScoped
import javax.enterprise.event.Event
import javax.enterprise.event.Observes
import javax.inject.Inject

/**
 *
 * @author oladeji
 */
@SessionScoped
class ApplicationModel @Inject constructor(
    private val searchFacade: RepositorySearchFacade,
    private val filter: Filter,
    private val reportsRefreshEvent: Event<ReportsRefreshEvent>) : Serializable {

  private lateinit var projects: List<Project>
  fun getProjects() = projects

  private lateinit var selectedProject: Project
  fun getSelectedProject() = selectedProject

  private lateinit var versions: List<ProjectVersion>
  fun getVersions() = versions

  private var selectedVersion: ProjectVersion? = null
  fun getSelectedVersion() = selectedVersion

  private lateinit var reports: Set<Report>
  fun getReports() = reports

  private var selectedReports = mutableSetOf<Report>()
  fun getSelectedReports() = selectedReports

  private var user: Reporter? = null
  fun getUser() = user

  @PostConstruct
  fun setup() {
    user = bugrapRepository.authenticate("developer", "developer")
    projects = bugrapRepository.findProjects().sortedBy { it.name }
    selectedProject = projects.first()
    updateVersionInfo()
    refreshReports(false)
  }

  internal fun updateVersionInfo() {
    versions = bugrapRepository.findProjectVersions(selectedProject).sortedBy { it.releaseDate }
    selectedVersion = null
  }

  internal fun refreshReports(fireEvent: Boolean = true) {
    val query = ReportsQuery().apply {
      project = selectedProject
      if (selectedVersion != null) projectVersion = selectedVersion
      if (!filter.statuses.isEmpty()) reportStatuses = filter.statuses
      if (filter.assignedOnlyMe) reportAssignee = user
    }

    reports = bugrapRepository.findReports(query)
    selectedReports.clear()
    if (fireEvent) reportsRefreshEvent.fire(ReportsRefreshEvent())
  }

  fun refreshReports(@Observes event: ReportsUpdateEvent) {
    if (event.reports.intersect(getReports()).isNotEmpty()) {
      refreshReports()
    }
  }

  fun searchReports(@Observes event: SearchEvent) {
    val statuses = if (filter.statuses.isEmpty()) null else filter.statuses
    reports = HashSet(searchFacade.search(event.searchTerm, getSelectedProject(), getSelectedVersion(), statuses))
    reportsRefreshEvent.fire(ReportsRefreshEvent())
  }

  fun switchProject(@Observes event: ProjectChangeEvent) {
    selectedProject = event.project
    updateVersionInfo()
    refreshReports()
  }

  fun switchVersion(@Observes event: VersionChangeEvent) {
    selectedVersion = event.version
    refreshReports()
  }

  fun applyFilter(@Observes event: FilterChangeEvent) {
    refreshReports()
  }

  fun updateSelectedReports(@Observes event: ReportsSelectionEvent) {
    selectedReports.apply {
      clear()
      addAll(event.selectedReports)
    }
  }

  companion object {
    private const val serialVersionUID = 1L
    private var dbDir: String
    @JvmStatic internal var bugrapRepository: BugrapRepository

    init {
      // Horrible hack that allows me keep the web app running while clicking individual
      // test run in IDE without the two HSQL instances failing to acquire lock.
      var dbExists = true
      try {
        dbDir = "${System.getProperty("user.home")}/.bugrap"
        dbExists = File(dbDir).exists()
        bugrapRepository = BugrapRepository("$dbDir/bugrap.db")

        if (!dbExists) {
          println("Bugrap main DB not found in home directory. Generating test data...")
          bugrapRepository.populateWithTestData()
        }
      } catch (ex: Exception) {
        println("Main DB locked by another application instance. Falling back to test DB.")
        dbDir = "${System.getProperty("user.home")}/.bugrap_test"
        dbExists = File(dbDir).exists()
        bugrapRepository = BugrapRepository("$dbDir/bugrap.db")

        if (!dbExists) {
          println("Bugrap test DB not found in home directory. Generating test data...")
          bugrapRepository.populateWithTestData()
        }
      }

      bugrapRepository.authenticate("developer", "developer")
    }
  }
}