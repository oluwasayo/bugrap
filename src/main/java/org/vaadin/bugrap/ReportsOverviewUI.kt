package org.vaadin.bugrap

import com.vaadin.annotations.Theme
import com.vaadin.cdi.CDIUI
import com.vaadin.data.provider.GridSortOrder.asc
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.data.sort.SortDirection.DESCENDING
import com.vaadin.ui.Grid
import com.vaadin.ui.Grid.SelectionMode.MULTI
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.PRIORITY
import org.vaadin.bugrap.core.VERSION
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.events.ReportsRefreshEvent
import org.vaadin.bugrap.events.ReportsSelectionEvent
import org.vaadin.bugrap.ui.reportsoverview.ActionsBar
import org.vaadin.bugrap.ui.reportsoverview.FilterBar
import org.vaadin.bugrap.ui.reportsoverview.HorizontalRule
import org.vaadin.bugrap.ui.reportsoverview.ProjectSelectorBar
import org.vaadin.bugrap.ui.reportsoverview.PropertiesBar
import org.vaadin.bugrap.ui.reportsoverview.VersionBar
import javax.enterprise.event.Event
import javax.enterprise.event.Observes
import javax.inject.Inject

/**
 *
 * @author oladeji
 */
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

  @Inject
  private lateinit var filterBar: FilterBar

  @Inject
  private lateinit var propertiesBar: PropertiesBar

  @Inject
  private lateinit var reportSelectionEvent: Event<ReportsSelectionEvent>

  private var table = Grid<Report>(Report::class.java)

  override fun init(vaadinRequest: VaadinRequest) {
    val verticalLayout = VerticalLayout()

    table.apply {
      setSizeFull()
      setSelectionMode(MULTI)
      addSelectionListener { reportSelectionEvent.fire(ReportsSelectionEvent(it.allSelectedItems)) }
      updateGrid(ReportsRefreshEvent())
    }

    content = verticalLayout.apply {
      addComponents(
          projectSelectorBar.apply { setWidth(100f, PERCENTAGE) },
          actionsBar.apply { setWidth(100f, PERCENTAGE) },
          HorizontalRule(),
          versionBar.apply { setWidth(100f, PERCENTAGE) },
          filterBar,
          table,
          propertiesBar
      )

      setExpandRatio(table, 1f)
    }

    content.setSizeFull()
  }

  fun updateGrid(@Observes event: ReportsRefreshEvent) {
    table.deselectAll()
    table.setItems(applicationModel.getReports())

    if (applicationModel.getSelectedVersion() == null) {
      if (table.getColumn(VERSION) == null) table.addColumn(VERSION)
      table.setColumnOrder(VERSION, PRIORITY)
      table.setSortOrder(
          asc(table.getColumn(VERSION))
          .thenDesc(table.getColumn(PRIORITY))
          .build()
      )
    } else {
      if (table.getColumn(VERSION) != null) table.removeColumn(VERSION)
      table.setColumnOrder(PRIORITY)
      table.sort(PRIORITY, DESCENDING)
    }
  }
}
