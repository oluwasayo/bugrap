package org.vaadin.bugrap.ui.reportsoverview

import com.vaadin.icons.VaadinIcons.EXTERNAL_LINK
import com.vaadin.server.ExternalResource
import com.vaadin.ui.Label
import com.vaadin.ui.Link
import org.vaadin.bugrap.cdi.events.ReportsRefreshEvent
import org.vaadin.bugrap.cdi.events.ReportsSelectionEvent
import org.vaadin.bugrap.cdi.events.ReportsUpdateEvent
import org.vaadin.bugrap.core.ApplicationModel.Companion.bugrapRepository
import org.vaadin.bugrap.core.CONTEXT_ROOT
import org.vaadin.bugrap.core.ICON_AFTER_CAPTION
import org.vaadin.bugrap.core.LABEL_GRAY_TEXT
import org.vaadin.bugrap.core.NEW_WINDOW
import org.vaadin.bugrap.core.SELECT_A_SINGLE_REPORT
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.ui.shared.AbstractPropertiesBar
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
class MultiReportPropertiesBar @Inject constructor(private val reportsRefreshEvent: Event<ReportsRefreshEvent>,
                                                   reportsUpdateEvent: Event<ReportsUpdateEvent>)
  : AbstractPropertiesBar(reportsUpdateEvent) {

  internal val newWindowLink = Link()
  internal val fixedTextLabel = Label(SELECT_A_SINGLE_REPORT)

  @PostConstruct
  override fun setup() {
    super.setup()

    newWindowLink.apply {
      setIcon(EXTERNAL_LINK)
      setTargetName(NEW_WINDOW)
      addStyleName(ICON_AFTER_CAPTION)
    }

    fixedTextLabel.addStyleName(LABEL_GRAY_TEXT)

    summaryBar.addComponents(newWindowLink, fixedTextLabel)

    setSizeUndefined()
    isVisible = false
  }

  override fun setSelectedReports(reports: Set<Report>, updateControls: Boolean) {
    if (updateControls) refreshControls(ReportsUpdateEvent(reports))
  }

  override fun refreshControls(@Observes event: ReportsUpdateEvent) {
    val intersection = selectedReports.intersect(event.reports)
    if (intersection.isEmpty()) return

    val leftJoin = mutableSetOf<Report>().apply {
      addAll(selectedReports)
      removeAll(intersection)
      addAll(intersection)
    }

    updateProperties(ReportsSelectionEvent(leftJoin))
  }

  override fun saveReports() {
    getSelectedReports().forEach { report ->
      priorityControl.selectedItem.ifPresent { report.priority = priorityControl.selectedItem.get() }
      typeControl.selectedItem.ifPresent { report.type = typeControl.selectedItem.get() }
      statusControl.selectedItem.ifPresent { report.status = statusControl.selectedItem.get() }
      assigneeControl.selectedItem.ifPresent { report.assigned = assigneeControl.selectedItem.get() }
      versionControl.selectedItem.ifPresent { report.version = versionControl.selectedItem.get() }

      bugrapRepository.save(report)
    }

    setSelectedReports(emptySet())
    reportsUpdateEvent.fire(ReportsUpdateEvent(getSelectedReports()))
  }

  fun updateProperties(@Observes event: ReportsSelectionEvent) {
    synchronized(selectedReports) {
      selectedReports.clear()
      selectedReports.addAll(event.selectedReports)
    }

    isVisible = getSelectedReports().isNotEmpty()

    val selectedReportsCount = getSelectedReports().size
    newWindowLink.isVisible = selectedReportsCount == 1

    reportDetailLabel.value = "$selectedReportsCount reports selected"
    reportDetailLabel.isVisible = selectedReportsCount > 1
    fixedTextLabel.isVisible = selectedReportsCount > 1

    if (!selectedReports.isEmpty()) {
      newWindowLink.caption = getSelectedReports().first().summary
      newWindowLink.resource = ExternalResource(CONTEXT_ROOT + "detail?id=" + getSelectedReports().first().id)

      getSelectedReports().map { it.priority }.distinct().apply {
        priorityControl.setSelectedItem(if (count() == 1) first() else null)
      }

      getSelectedReports().map { it.type }.distinct().apply {
        typeControl.setSelectedItem(if (count() == 1) first() else null)
      }

      getSelectedReports().map { it.status }.distinct().apply {
        statusControl.setSelectedItem(if (count() == 1) first() else null)
      }

      getSelectedReports().map { it.assigned }.distinct().apply {
        assigneeControl.setSelectedItem(if (count() == 1) first() else null)
      }

      getSelectedReports().map { it.version }.distinct().apply {
        versionControl.setSelectedItem(if (count() == 1) first() else null)
      }
    }
  }

  fun updateProperties(@Observes event: ReportsRefreshEvent) {
    isVisible = getSelectedReports().isNotEmpty()
  }

  companion object {
    val reportsRefresh = ReportsRefreshEvent()
  }
}