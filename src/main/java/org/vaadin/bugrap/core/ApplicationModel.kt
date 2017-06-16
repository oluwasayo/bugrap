package org.vaadin.bugrap.core

import org.vaadin.bugrap.domain.BugrapRepository
import org.vaadin.bugrap.domain.BugrapRepository.ReportsQuery
import org.vaadin.bugrap.domain.RepositorySearchFacade
import org.vaadin.bugrap.domain.entities.Project
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.domain.entities.Reporter
import org.vaadin.bugrap.events.FilterChangeEvent
import org.vaadin.bugrap.events.ProjectChangeEvent
import org.vaadin.bugrap.events.ReportsRefreshEvent
import org.vaadin.bugrap.events.ReportsSelectionEvent
import org.vaadin.bugrap.events.SearchEvent
import org.vaadin.bugrap.events.VersionChangeEvent
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
class ApplicationModel() : Serializable {

  private lateinit var searchFacade: RepositorySearchFacade
  private lateinit var filter: Filter
  private lateinit var reportsRefreshEvent: Event<ReportsRefreshEvent>

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
  fun getUsername() = user

  @Inject
  constructor(searchFacade: RepositorySearchFacade, filter: Filter, reportsRefreshEvent: Event<ReportsRefreshEvent>)
      : this() {
    this.searchFacade = searchFacade
    this.filter = filter
    this.reportsRefreshEvent = reportsRefreshEvent
  }

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

  fun searchReports(@Observes event: SearchEvent) {
    val statuses = if (filter.statuses.isEmpty()) null else filter.statuses
    reports = HashSet(searchFacade.search(event.searchTerm, getSelectedProject(), getSelectedVersion(), statuses))
    reportsRefreshEvent.fire(ReportsRefreshEvent())
  }

  fun switchProject(@Observes event: ProjectChangeEvent) {
    selectedProject = event.project!!
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
    private val dbDir = "${System.getProperty("user.home")}/.bugrap"
    @JvmStatic internal var bugrapRepository = BugrapRepository("$dbDir/bugrap.db")

    init {
      if (!File(dbDir).exists()) {
        println("Bugrap DB not found in home directory. Generating test data...")
        bugrapRepository.populateWithTestData()
      }
    }
  }
}