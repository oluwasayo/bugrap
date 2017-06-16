package org.vaadin.bugrap.ui.reportsoverview

import com.vaadin.icons.VaadinIcons.EXTERNAL_LINK
import com.vaadin.server.ExternalResource
import com.vaadin.server.Sizeable.Unit.MM
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.ui.Button
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.ItemCaptionGenerator
import com.vaadin.ui.Label
import com.vaadin.ui.Link
import com.vaadin.ui.NativeSelect
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.themes.ValoTheme.BUTTON_DANGER
import com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY
import com.vaadin.ui.themes.ValoTheme.BUTTON_TINY
import org.vaadin.bugrap.core.ALIGN_BOTTOM
import org.vaadin.bugrap.core.ASSIGNED_TO
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.ApplicationModel.Companion.bugrapRepository
import org.vaadin.bugrap.core.CONTEXT_ROOT
import org.vaadin.bugrap.core.ICON_AFTER_CAPTION
import org.vaadin.bugrap.core.ISSUE_TYPE
import org.vaadin.bugrap.core.LABEL_GRAY_TEXT
import org.vaadin.bugrap.core.NEW_WINDOW
import org.vaadin.bugrap.core.PRIORITY
import org.vaadin.bugrap.core.REVERT
import org.vaadin.bugrap.core.SELECT_A_SINGLE_REPORT
import org.vaadin.bugrap.core.STATUS
import org.vaadin.bugrap.core.UPDATE
import org.vaadin.bugrap.core.VERSION
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.domain.entities.Report.Priority
import org.vaadin.bugrap.domain.entities.Report.Status
import org.vaadin.bugrap.domain.entities.Report.Type
import org.vaadin.bugrap.domain.entities.Reporter
import org.vaadin.bugrap.events.ReportsRefreshEvent
import org.vaadin.bugrap.events.ReportsSelectionEvent
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
class PropertiesBar() : CustomComponent() {

  private lateinit var applicationModel: ApplicationModel
  private lateinit var reportsRefreshEvent: Event<ReportsRefreshEvent>

  internal val newWindowLink = Link()
  internal val reportDetailLabel = Label()
  internal val fixedTextLabel = Label(SELECT_A_SINGLE_REPORT)

  private val controlsBar = HorizontalLayout()
  internal val priorityControl = NativeSelect<Priority>(PRIORITY.toUpperCase(), Priority.values().asList())
  internal val typeControl = NativeSelect<Type>(ISSUE_TYPE.toUpperCase(), Type.values().asList())
  internal val statusControl = NativeSelect<Status>(STATUS.toUpperCase(), Status.values().asList())
  internal val assigneeControl = NativeSelect<Reporter>(ASSIGNED_TO.toUpperCase())
  internal val versionControl = NativeSelect<ProjectVersion>(VERSION.toUpperCase())

  @Inject
  constructor(applicationModel: ApplicationModel, reportsRefreshEvent: Event<ReportsRefreshEvent>) : this() {
    this.applicationModel = applicationModel
    this.reportsRefreshEvent = reportsRefreshEvent
  }

  @PostConstruct
  fun setup() {
    newWindowLink.apply {
      setIcon(EXTERNAL_LINK)
      setTargetName(NEW_WINDOW)
      addStyleName(ICON_AFTER_CAPTION)
    }

    fixedTextLabel.addStyleName(LABEL_GRAY_TEXT)

    val summaryBar = HorizontalLayout().apply {
      setHeight(20f, PIXELS)
      addComponents(newWindowLink, reportDetailLabel, fixedTextLabel)
    }

    priorityControl.setWidth(100F, PIXELS)
    typeControl.setWidth(100f, PIXELS)
    statusControl.setWidth(120f, PIXELS)
    assigneeControl.setWidth(120f, PIXELS)
    versionControl.setWidth(100f, PIXELS)

    assigneeControl.setItems(bugrapRepository.findReporters())
    versionControl.setItems(bugrapRepository.findProjectVersions(applicationModel.getSelectedProject()))

    val updateButton = Button(UPDATE).apply {
      addStyleName(BUTTON_TINY)
      addStyleName(BUTTON_PRIMARY)
      addClickListener {
        applicationModel.getSelectedReports().forEach { report ->
          priorityControl.selectedItem.ifPresent { report.priority = priorityControl.selectedItem.get() }
          typeControl.selectedItem.ifPresent { report.type = typeControl.selectedItem.get() }
          statusControl.selectedItem.ifPresent { report.status = statusControl.selectedItem.get() }
          assigneeControl.selectedItem.ifPresent { report.assigned = assigneeControl.selectedItem.get() }
          versionControl.selectedItem.ifPresent { report.version = versionControl.selectedItem.get() }

          bugrapRepository.save(report)
        }

        reportsRefreshEvent.fire(reportsRefresh)
      }
    }

    val revertButton = Button(REVERT).apply {
      addStyleName(BUTTON_TINY)
      addStyleName(BUTTON_DANGER)
      addClickListener { updateProperties(ReportsSelectionEvent(applicationModel.getSelectedReports())) }
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
    isVisible = false
  }

  fun updateProperties(@Observes event: ReportsSelectionEvent) {
    isVisible = !event.selectedReports.isEmpty()

    val selectedReportsCount = event.selectedReports.size
    newWindowLink.isVisible = selectedReportsCount == 1

    reportDetailLabel.value = "$selectedReportsCount reports selected"
    reportDetailLabel.isVisible = selectedReportsCount > 1
    fixedTextLabel.isVisible = selectedReportsCount > 1

    if (!event.selectedReports.isEmpty()) {
      newWindowLink.caption = event.selectedReports.first().summary
      newWindowLink.resource = ExternalResource(CONTEXT_ROOT + event.selectedReports.first().id)

      event.selectedReports.map { it.priority }.distinct().apply {
        priorityControl.setSelectedItem(if (count() == 1) first() else null)
      }

      event.selectedReports.map { it.type }.distinct().apply {
        typeControl.setSelectedItem(if (count() == 1) first() else null)
      }

      event.selectedReports.map { it.status }.distinct().apply {
        statusControl.setSelectedItem(if (count() == 1) first() else null)
      }

      event.selectedReports.map { it.assigned }.distinct().apply {
        assigneeControl.setSelectedItem(if (count() == 1) first() else null)
      }

      event.selectedReports.map { it.version }.distinct().apply {
        versionControl.setSelectedItem(if (count() == 1) first() else null)
      }
    }
  }

  fun updateProperties(@Observes event: ReportsRefreshEvent) {
    isVisible = !applicationModel.getSelectedReports().isEmpty()
  }

  companion object {
    val reportsRefresh = ReportsRefreshEvent()
  }
}