package org.vaadin.bugrap.domain

import org.vaadin.bugrap.domain.entities.Project
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.domain.entities.Report.Status
import javax.enterprise.context.ApplicationScoped

/**
 *
 * @author oladeji
 */
@ApplicationScoped
class RepositorySearchFacade {

  fun search(text: String, project: Project, version: ProjectVersion?, statuses: Set<Status>?)
      = FacadeUtil.searchReports(text, project, version, null, statuses, null, null)
}