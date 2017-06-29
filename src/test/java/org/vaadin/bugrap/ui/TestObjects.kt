package org.vaadin.bugrap.ui

import org.vaadin.bugrap.core.Clock
import org.vaadin.bugrap.domain.entities.Project
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.domain.entities.Report.Priority.BLOCKER
import org.vaadin.bugrap.domain.entities.Report.Status.FIXED
import org.vaadin.bugrap.domain.entities.Report.Status.WONT_FIX
import org.vaadin.bugrap.domain.entities.Report.Type.BUG
import org.vaadin.bugrap.domain.entities.Reporter

/**
 *
 * @author oladeji
 */

val report1 = Report().apply {
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

val report2 = Report().apply {
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