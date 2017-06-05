package com.vaadin.bugrap

import com.vaadin.annotations.Theme
import com.vaadin.bugrap.core.ApplicationModel
import com.vaadin.bugrap.events.ReportSelectionEvent
import com.vaadin.bugrap.ui.reportsoverview.ActionsBar
import com.vaadin.bugrap.ui.reportsoverview.FilterBar
import com.vaadin.bugrap.ui.reportsoverview.ProjectSelectorBar
import com.vaadin.bugrap.ui.reportsoverview.VersionBar
import com.vaadin.cdi.CDIUI
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.ui.ContentMode.HTML
import com.vaadin.ui.Grid
import com.vaadin.ui.Grid.SelectionMode
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import org.vaadin.bugrap.domain.entities.Report
import javax.enterprise.event.Event
import javax.inject.Inject


@CDIUI("")
@Theme("mytheme")
class ReportsOverviewUI : UI() {

  @Inject
  private lateinit var applicationModel: ApplicationModel

  @Inject
  private lateinit var projectSelectorBar: ProjectSelectorBar

  @Inject
  private lateinit var actionsBar: ActionsBar

  @Inject
  private lateinit var versionBar: VersionBar

  @Inject lateinit var filterBar: FilterBar

  @Inject
  private lateinit var reportSelectionEvent: Event<ReportSelectionEvent>

  override fun init(vaadinRequest: VaadinRequest) {
    val verticalLayout = VerticalLayout()

    val horizontalRuleBar = newFullPageHorizontalRule()

    var table = Grid<Report>(Report::class.java).apply {
      setSizeFull()
      setItems(applicationModel.getReports())
      setSelectionMode(SelectionMode.SINGLE)
      addSelectionListener { e -> reportSelectionEvent.fire(ReportSelectionEvent(e.allSelectedItems)) }
    }

    content = verticalLayout.apply {
      addComponents(projectSelectorBar, actionsBar, horizontalRuleBar, versionBar, filterBar, table)
      setExpandRatio(table, 1f)
    }

    content.setSizeFull()
  }

  private fun newFullPageHorizontalRule(): HorizontalLayout {
    return HorizontalLayout().apply {
      val horizontalRuleLabel = Label("<hr />").apply {
        contentMode = HTML
        setWidth(100f, PERCENTAGE)
        setMargin(false)
      }
      addComponent(horizontalRuleLabel)
      setExpandRatio(horizontalRuleLabel, 1f)
      setHeight(1f, PIXELS)
      setWidth(100f, PERCENTAGE)
      setMargin(false)
    }
  }
}
