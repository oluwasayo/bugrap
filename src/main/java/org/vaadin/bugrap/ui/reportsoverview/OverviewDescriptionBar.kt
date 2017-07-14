package org.vaadin.bugrap.ui.reportsoverview

import org.vaadin.bugrap.cdi.events.ReportsRefreshEvent
import org.vaadin.bugrap.cdi.events.ReportsSelectionEvent
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.ui.shared.AbstractDescriptionBar
import javax.enterprise.context.SessionScoped
import javax.enterprise.event.Observes
import javax.inject.Inject

/**
 *
 * @author oladeji
 */
@SessionScoped
class OverviewDescriptionBar @Inject constructor(private val model: ApplicationModel) : AbstractDescriptionBar() {

  fun updateUI(@Observes event: ReportsSelectionEvent) {
    isVisible = event.selectedReports.size == 1
    if (isVisible) report = event.selectedReports.first()
  }

  fun updateUI(@Observes event: ReportsRefreshEvent) {
    updateUI(ReportsSelectionEvent(model.getSelectedReports()))
  }
}