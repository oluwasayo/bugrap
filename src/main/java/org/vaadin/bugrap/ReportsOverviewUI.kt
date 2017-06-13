package org.vaadin.bugrap

import com.vaadin.annotations.Theme
import com.vaadin.cdi.CDIUI
import com.vaadin.data.provider.GridSortOrder.asc
import com.vaadin.event.ShortcutAction.KeyCode
import com.vaadin.server.ExternalResource
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.data.sort.SortDirection.DESCENDING
import com.vaadin.ui.Grid
import com.vaadin.ui.Grid.SelectionMode.MULTI
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.VerticalSplitPanel
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.CONTEXT_ROOT
import org.vaadin.bugrap.core.DESCRIPTION
import org.vaadin.bugrap.core.ID
import org.vaadin.bugrap.core.NEW_WINDOW
import org.vaadin.bugrap.core.PRIORITY
import org.vaadin.bugrap.core.SMALL_TOP_MARGIN
import org.vaadin.bugrap.core.ShortcutListenerFactory.newShortcutListener
import org.vaadin.bugrap.core.VERSION
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.events.ReportsRefreshEvent
import org.vaadin.bugrap.events.ReportsSelectionEvent
import org.vaadin.bugrap.ui.reportsoverview.ActionsBar
import org.vaadin.bugrap.ui.reportsoverview.FilterBar
import org.vaadin.bugrap.ui.reportsoverview.HorizontalRule
import org.vaadin.bugrap.ui.reportsoverview.ProjectSelectorBar
import org.vaadin.bugrap.ui.reportsoverview.PropertiesBar
import org.vaadin.bugrap.ui.reportsoverview.ReportDescriptionBar
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

  @Inject lateinit var descriptionBar: ReportDescriptionBar

  @Inject
  private lateinit var reportSelectionEvent: Event<ReportsSelectionEvent>

  private val reportsRefresh = ReportsRefreshEvent()

  private val table = Grid<Report>(Report::class.java)

  private val split = VerticalSplitPanel()

  override fun init(vaadinRequest: VaadinRequest) {
    val verticalLayout = VerticalLayout()

    table.apply {
      setSizeFull()
      setSelectionMode(MULTI)
      removeColumn(DESCRIPTION)

      addSelectionListener { reportSelectionEvent.fire(ReportsSelectionEvent(it.allSelectedItems)) }

      addItemClickListener {
        if (it.mouseEventDetails.isDoubleClick) {
          page.open(ExternalResource(CONTEXT_ROOT + it.item.id.toString()), NEW_WINDOW, false)
        }
      }

      addShortcutListener(newShortcutListener("Enter", KeyCode.ENTER, intArrayOf()) { sender, target ->
        if (target is Grid<*> && target.selectedItems.size >= 1) {
          page.open(ExternalResource(CONTEXT_ROOT + (target as Grid<Report>).selectedItems.last().id.toString()),
              NEW_WINDOW, false)
        }
      })

      updateGrid(reportsRefresh)
    }

    split.apply {
      firstComponent = table
      secondComponent = VerticalLayout().apply {
        addComponents(propertiesBar, descriptionBar)
        addStyleName(SMALL_TOP_MARGIN)
        setExpandRatio(descriptionBar, 1f)
        setMargin(false)
        setWidth(100f, PERCENTAGE)
      }

      isLocked = true
      setSplitPosition(100f, PERCENTAGE)
    }

    content = verticalLayout.apply {
      addComponents(
          projectSelectorBar.apply { setWidth(100f, PERCENTAGE) },
          actionsBar.apply { setWidth(100f, PERCENTAGE) },
          HorizontalRule(),
          versionBar.apply { setWidth(100f, PERCENTAGE) },
          filterBar,
          split
      )

      setExpandRatio(split, 1f)
    }

    page.setTitle("Bugrap")
    content.setSizeFull()
  }

  fun updateGrid(@Observes event: ReportsRefreshEvent) {
    table.deselectAll()
    table.setItems(applicationModel.getReports())

    if (applicationModel.getSelectedVersion() == null) {
      if (table.getColumn(VERSION) == null) table.addColumn(VERSION)
      table.setColumnOrder(VERSION, PRIORITY, ID)
      table.setSortOrder(
          asc(table.getColumn(VERSION))
          .thenDesc(table.getColumn(PRIORITY))
          .build()
      )
    } else {
      if (table.getColumn(VERSION) != null) table.removeColumn(VERSION)
      table.setColumnOrder(PRIORITY, ID)
      table.sort(PRIORITY, DESCENDING)
    }

    updateSplit(ReportsSelectionEvent(applicationModel.getSelectedReports()))
  }

  fun updateSplit(@Observes event: ReportsSelectionEvent) {
    when (event.selectedReports.size) {
      0 -> {
        split.setSplitPosition(100f, PERCENTAGE)
        split.isLocked = true
      }
      1 -> {
        split.setSplitPosition(250f, PIXELS, true)
        split.isLocked = false
      }
      else -> {
        split.setSplitPosition(105f, PIXELS, true)
        split.isLocked = true
      }
    }
  }
}
