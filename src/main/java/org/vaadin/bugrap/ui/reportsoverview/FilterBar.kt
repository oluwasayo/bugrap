package org.vaadin.bugrap.ui.reportsoverview

import com.vaadin.server.Sizeable.Unit.MM
import com.vaadin.ui.Button
import com.vaadin.ui.CssLayout
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.MenuBar
import com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY
import com.vaadin.ui.themes.ValoTheme.BUTTON_SMALL
import com.vaadin.ui.themes.ValoTheme.LAYOUT_COMPONENT_GROUP
import com.vaadin.ui.themes.ValoTheme.MENUBAR_SMALL
import org.vaadin.bugrap.core.ALL_KINDS
import org.vaadin.bugrap.core.ASSIGNEES
import org.vaadin.bugrap.core.CUSTOM
import org.vaadin.bugrap.core.EVERYONE
import org.vaadin.bugrap.core.Filter
import org.vaadin.bugrap.core.MENUBAR_THEMED
import org.vaadin.bugrap.core.ONLY_ME
import org.vaadin.bugrap.core.OPEN
import org.vaadin.bugrap.core.ROUNDED_EAST
import org.vaadin.bugrap.core.ROUNDED_WEST
import org.vaadin.bugrap.core.STATUS
import org.vaadin.bugrap.domain.entities.Report.Status
import org.vaadin.bugrap.events.FilterChangeEvent
import javax.annotation.PostConstruct
import javax.enterprise.context.SessionScoped
import javax.enterprise.event.Event
import javax.inject.Inject

/**
 *
 * @author oladeji
 */
@SessionScoped
class FilterBar : CustomComponent() {

  @Inject
  private lateinit var filter: Filter

  @Inject
  private lateinit var filterChangeEvent: Event<FilterChangeEvent>

  val filterChange = FilterChangeEvent()

  @PostConstruct
  private fun setup() {
    val assigneesLabel = Label(ASSIGNEES)

    val onlyMeButton = Button(ONLY_ME).apply {
      addStyleName(BUTTON_SMALL)
      addStyleName(ROUNDED_WEST)
    }

    val everyoneButton = Button(EVERYONE).apply {
      addStyleName(BUTTON_SMALL)
      addStyleName(BUTTON_PRIMARY)
      addStyleName(ROUNDED_EAST)

      addClickListener {
        filter.assignedOnlyMe = false
        addStyleName(BUTTON_PRIMARY)
        onlyMeButton.removeStyleName(BUTTON_PRIMARY)
        filterChangeEvent.fire(filterChange)
      }
    }

    onlyMeButton.addClickListener {
      filter.assignedOnlyMe = true
      onlyMeButton.addStyleName(BUTTON_PRIMARY)
      everyoneButton.removeStyleName(BUTTON_PRIMARY)
      filterChangeEvent.fire(filterChange)
    }

    val assigneeControls = CssLayout().apply {
      addComponents(onlyMeButton, everyoneButton)
      setStyleName(LAYOUT_COMPONENT_GROUP)
    }

    val assigneeStatusSpacing = Label().apply { setWidth(4f, MM) }

    val statusLabel = Label(STATUS)

    val openButton = Button(OPEN).apply {
      addStyleName(BUTTON_SMALL)
      addStyleName(ROUNDED_WEST)
    }

    val allKindsButton = Button(ALL_KINDS).apply {
      addStyleName(BUTTON_SMALL)
      addStyleName(BUTTON_PRIMARY)
    }

    val customMenu = MenuBar().apply {
      val rootItem = addItem(CUSTOM, null)
      Status.values().forEach {
        val menuItem = rootItem.addItem(it.toString(), null)
        menuItem.isCheckable = true
        menuItem.command = MenuBar.Command {
          filter.statuses.clear()

          rootItem.children.filter { it.isChecked }.forEach {
            filter.statuses.add(statusMap.get(it.text)!!)
          }

          if (filter.statuses.isEmpty()) {
            this@apply.removeStyleName(MENUBAR_THEMED)
            allKindsButton.addStyleName(BUTTON_PRIMARY)
            allKindsButton.focus()
          } else {
            this@apply.addStyleName(ROUNDED_EAST)
            this@apply.addStyleName(MENUBAR_THEMED)
            allKindsButton.removeStyleName(BUTTON_PRIMARY)
          }

          openButton.removeStyleName(BUTTON_PRIMARY)
          filterChangeEvent.fire(filterChange)
        }
      }

      addStyleName(MENUBAR_SMALL)
      addStyleName(ROUNDED_EAST)
    }

    openButton.apply {
      addClickListener {
        addStyleName(BUTTON_PRIMARY)
        allKindsButton.removeStyleName(BUTTON_PRIMARY)
        customMenu.removeStyleName(MENUBAR_THEMED)
        filter.statuses.clear()
        filter.statuses.add(Status.OPEN)
        filterChangeEvent.fire(filterChange)
      }
    }

    allKindsButton.apply {
      addClickListener {
        addStyleName(BUTTON_PRIMARY)
        openButton.removeStyleName(BUTTON_PRIMARY)
        customMenu.removeStyleName(MENUBAR_THEMED)
        filter.statuses.clear()
        filterChangeEvent.fire(filterChange)
      }
    }

    val statusControls = CssLayout().apply {
      addComponents(openButton, allKindsButton, customMenu)
      setStyleName(LAYOUT_COMPONENT_GROUP)
    }

    compositionRoot = HorizontalLayout().apply {
      addComponents(assigneesLabel, assigneeControls, assigneeStatusSpacing, statusLabel, statusControls)
    }

    setSizeUndefined()
  }

  companion object {
    private val statusMap = hashMapOf<String, Status>()

    init {
      Status.values().forEach { statusMap.put(it.toString(), it) }
    }
  }
}