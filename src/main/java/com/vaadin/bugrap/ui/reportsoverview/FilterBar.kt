package com.vaadin.bugrap.ui.reportsoverview

import com.vaadin.bugrap.core.Filter
import com.vaadin.bugrap.events.FilterChangeEvent
import com.vaadin.server.Sizeable.Unit.MM
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.ui.Button
import com.vaadin.ui.CssLayout
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.MenuBar
import com.vaadin.ui.themes.ValoTheme
import org.vaadin.bugrap.domain.entities.Report.Status
import javax.annotation.PostConstruct
import javax.enterprise.context.SessionScoped
import javax.enterprise.event.Event
import javax.inject.Inject

/**
 *
 * @author oladeji
 */
@SessionScoped
class FilterBar: CustomComponent() {

  @Inject
  private lateinit var filter: Filter

  @Inject
  private lateinit var filterChangeEvent: Event<FilterChangeEvent>

  val filterChange = FilterChangeEvent()

  @PostConstruct
  private fun setup() {
    val assigneesLabel = Label("Assignees")

    val onlyMeButton = Button("Only me").apply {
      addStyleName(ValoTheme.BUTTON_TINY)
      addStyleName("rounded-west")
    }

    val everyoneButton = Button("Everyone").apply {
      addStyleName(ValoTheme.BUTTON_TINY)
      addStyleName("rounded-east")

      addClickListener {
        filter.assignedOnlyMe = false
        addStyleName(ValoTheme.BUTTON_PRIMARY)
        onlyMeButton.removeStyleName(ValoTheme.BUTTON_PRIMARY)
        filterChangeEvent.fire(filterChange)
      }
    }

    onlyMeButton.addClickListener {
      filter.assignedOnlyMe = true
      addStyleName(ValoTheme.BUTTON_PRIMARY)
      everyoneButton.removeStyleName(ValoTheme.BUTTON_PRIMARY)
      filterChangeEvent.fire(filterChange)
    }

    val assigneeControls = CssLayout().apply {
      addComponents(onlyMeButton, everyoneButton)
      setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP)
    }

    val assigneeStatusSpacing = Label().apply { setWidth(4f, MM) }

    val statusLabel = Label("Status")

    val openButton = Button("Open").apply {
      addStyleName(ValoTheme.BUTTON_TINY)
      addStyleName("rounded-west")

      addClickListener{ e ->
        filter.statuses.clear()
        filter.statuses.add(Status.OPEN)
        filterChangeEvent.fire(filterChange)
      }
    }

    val allKindsButton = Button("All kinds").apply {
      addStyleName(ValoTheme.BUTTON_TINY)

      addClickListener {
        filter.statuses.clear()
        filterChangeEvent.fire(filterChange)
      }
    }

    val customMenu = MenuBar().apply {
      addItem("Custom", null).apply {
        Status.values().forEach { addItem(it.toString(), null) }
      }

      addStyleName("menubar-tiny")
      addStyleName("rounded-east")
      setHeight(28f, PIXELS)
    }

    val statusControls = CssLayout().apply {
      addComponents(openButton, allKindsButton, customMenu)
      setStyleName(ValoTheme.LAYOUT_COMPONENT_GROUP)
    }

    compositionRoot = HorizontalLayout().apply {
      addComponents(assigneesLabel, assigneeControls, assigneeStatusSpacing, statusLabel, statusControls)
    }

    setSizeUndefined()
  }
}