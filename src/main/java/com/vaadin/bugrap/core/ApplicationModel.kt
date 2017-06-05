package com.vaadin.bugrap.core

import com.vaadin.bugrap.events.LogoutEvent
import com.vaadin.bugrap.events.ProjectChangeEvent
import com.vaadin.bugrap.events.VersionChangeEvent
import org.vaadin.bugrap.domain.BugrapRepository
import org.vaadin.bugrap.domain.BugrapRepository.ReportsQuery
import org.vaadin.bugrap.domain.entities.Project
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.domain.entities.Reporter
import java.io.Serializable
import javax.annotation.PostConstruct
import javax.enterprise.context.SessionScoped
import javax.enterprise.event.Observes
import javax.inject.Inject

/**
 *
 * @author oladeji
 */
@SessionScoped
class ApplicationModel: Serializable {

  @Inject
  private lateinit var filter: Filter

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

  private var user: Reporter? = null
  fun getUsername() = user

  @PostConstruct
  fun setup() {
    user = bugrapRepository.authenticate("admin", "admin")
    projects = bugrapRepository.findProjects().sortedBy { it.name }
    selectedProject = projects.first()
    updateVersionInfo()
    refreshReports()
  }

  private fun updateVersionInfo() {
    versions = bugrapRepository.findProjectVersions(selectedProject).sortedBy { it.releaseDate }
    selectedVersion = null
  }

  private fun refreshReports() {
    val query = ReportsQuery().apply {
      project = selectedProject
      projectVersion = selectedVersion
      reportStatuses = filter.statuses
      if (filter.assignedOnlyMe) reportAssignee = user
    }
    reports = bugrapRepository.findReports(query)
  }

  fun logout(@Observes event: LogoutEvent) {
    this.user = null
  }

  fun switchProject(@Observes event: ProjectChangeEvent) {
    selectedProject = event.project!!
    updateVersionInfo()
  }

  fun switchVersion(@Observes event: VersionChangeEvent) {
    selectedVersion = event.version
  }

  companion object {
    private const val serialVersionUID = 1L
    private const val dbFileName = "~/bugrap/bugrap.db"
    @JvmStatic val bugrapRepository = BugrapRepository(dbFileName)
//    private var dbFile = File(dbFileName).apply {
//      if (!exists()) {
//        bugrapRepository.populateWithTestData()
//      }
//    }
  }
}