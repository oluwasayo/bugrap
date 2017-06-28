package org.vaadin.bugrap.ui.reportdetail

import com.vaadin.server.Sizeable.Unit.MM
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.ui.Button
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.ItemCaptionGenerator
import com.vaadin.ui.Label
import com.vaadin.ui.NativeSelect
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.themes.ValoTheme
import org.vaadin.bugrap.cdi.Proxyable
import org.vaadin.bugrap.cdi.events.ReportsUpdateEvent
import org.vaadin.bugrap.core.ALIGN_BOTTOM
import org.vaadin.bugrap.core.ASSIGNED_TO
import org.vaadin.bugrap.core.ApplicationModel.Companion.bugrapRepository
import org.vaadin.bugrap.core.ISSUE_TYPE
import org.vaadin.bugrap.core.PRIORITY
import org.vaadin.bugrap.core.REVERT
import org.vaadin.bugrap.core.STATUS
import org.vaadin.bugrap.core.UPDATE
import org.vaadin.bugrap.core.VERSION
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.domain.entities.Report.Priority
import org.vaadin.bugrap.domain.entities.Report.Status
import org.vaadin.bugrap.domain.entities.Report.Type
import org.vaadin.bugrap.domain.entities.Reporter
import javax.annotation.PostConstruct
import javax.enterprise.context.Dependent
import javax.enterprise.event.Event
import javax.enterprise.event.Observes
import javax.inject.Inject

/**
 *
 * @author oladeji
 */
@Proxyable
@Dependent
class BasePropertiesBar() : CustomComponent() {

  private lateinit var reportsUpdateEvent: Event<ReportsUpdateEvent>

  internal val selectedReports = mutableSetOf<Report>()
  internal val reportDetailLabel = Label()

  private val controlsBar = HorizontalLayout()
  internal val priorityControl = NativeSelect<Priority>(PRIORITY.toUpperCase(), Priority.values().asList())
  internal val typeControl = NativeSelect<Type>(ISSUE_TYPE.toUpperCase(), Type.values().asList())
  internal val statusControl = NativeSelect<Status>(STATUS.toUpperCase(), Status.values().asList())
  internal val assigneeControl = NativeSelect<Reporter>(ASSIGNED_TO.toUpperCase())
  internal val versionControl = NativeSelect<ProjectVersion>(VERSION.toUpperCase())

  @Inject
  constructor(reportsRefreshEvent: Event<ReportsUpdateEvent>) : this() {
    this.reportsUpdateEvent = reportsRefreshEvent
  }

  @PostConstruct
  fun setup() {
    val summaryBar = HorizontalLayout().apply {
      setHeight(20f, PIXELS)
      addComponents(reportDetailLabel)
    }

    priorityControl.setWidth(100F, PIXELS)
    typeControl.setWidth(100f, PIXELS)
    statusControl.setWidth(120f, PIXELS)
    assigneeControl.setWidth(120f, PIXELS)
    versionControl.setWidth(100f, PIXELS)

    assigneeControl.setItems(bugrapRepository.findReporters())

    val updateButton = Button(UPDATE).apply {
      addStyleName(ValoTheme.BUTTON_TINY)
      addStyleName(ValoTheme.BUTTON_PRIMARY)
      addClickListener { saveReports() }
    }

    val revertButton = Button(REVERT).apply {
      addStyleName(ValoTheme.BUTTON_TINY)
      addStyleName(ValoTheme.BUTTON_DANGER)
      addClickListener { refreshControls(ReportsUpdateEvent(selectedReports)) }
    }

    controlsBar.apply {
      addComponents(priorityControl, typeControl, statusControl)
      iterator().forEach {
        (it as NativeSelect<Enum<*>>).itemCaptionGenerator = ItemCaptionGenerator {
          it.name.toLowerCase().capitalize().replace("_", " ")
        }
      }

      addComponents(assigneeControl, versionControl)
      iterator().forEach { (it as NativeSelect<*>).isEmptySelectionAllowed = true }

      addComponentsAndExpand(
          Label().apply { setWidth(4f, MM) },
          updateButton,
          revertButton
      )

      addStyleName(ALIGN_BOTTOM)
    }

    compositionRoot = VerticalLayout().apply {
      addComponents(summaryBar, controlsBar)
      setMargin(false)
      setWidth(100f, PERCENTAGE)
    }

    setSizeUndefined()
  }

  fun setSelectedReports(reports: Set<Report>) {
    synchronized(selectedReports) {
      selectedReports.clear()
      selectedReports.addAll(reports)
    }

    refreshControls(ReportsUpdateEvent(reports))
  }

  protected fun refreshControls(@Observes event: ReportsUpdateEvent) {
    if (selectedReports.intersect(event.reports).isEmpty()) return

    with(selectedReports.first()) {
      reportDetailLabel.value = summary
      priorityControl.setSelectedItem(priority)
      typeControl.setSelectedItem(type)
      statusControl.setSelectedItem(status)
      assigneeControl.setSelectedItem(assigned)
      versionControl.setItems(bugrapRepository.findProjectVersions(project))
    }
  }

  protected fun saveReports() {
    selectedReports.first().apply {
      priority = priorityControl.selectedItem.orElse(null)
      type = typeControl.selectedItem.orElse(null)
      status = statusControl.selectedItem.orElse(null)
      assigned = assigneeControl.selectedItem.orElse(null)
      version = versionControl.selectedItem.orElse(null)

      bugrapRepository.save(this)
    }

    reportsUpdateEvent.fire(ReportsUpdateEvent(selectedReports))
  }
}
