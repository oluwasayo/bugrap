package org.vaadin.bugrap.ui.reportsoverview

import com.vaadin.icons.VaadinIcons.KEY
import com.vaadin.icons.VaadinIcons.LINE_V
import com.vaadin.icons.VaadinIcons.USER
import com.vaadin.server.Sizeable.Unit.MM
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.shared.ui.ContentMode.HTML
import com.vaadin.ui.Button
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.NativeSelect
import com.vaadin.ui.themes.ValoTheme
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.LOGOUT
import org.vaadin.bugrap.core.ROUNDED_EAST
import org.vaadin.bugrap.core.ROUNDED_WEST
import org.vaadin.bugrap.domain.entities.Project
import org.vaadin.bugrap.events.LogoutEvent
import org.vaadin.bugrap.events.ProjectChangeEvent
import javax.annotation.PostConstruct
import javax.enterprise.context.SessionScoped
import javax.enterprise.event.Event
import javax.inject.Inject

/**
 *
 * @author oladeji
 */
@SessionScoped
class ProjectSelectorBar: CustomComponent() {

  @Inject
  private lateinit var applicationModel: ApplicationModel

  @Inject
  private lateinit var projectChangeEvent: Event<ProjectChangeEvent>

  @Inject
  private lateinit var logoutEvent: Event<LogoutEvent>

  @PostConstruct
  fun setup() {
    val projectSelector = NativeSelect<Project>(null, applicationModel.getProjects()).apply {
      setWidth(100f, MM)
      isEmptySelectionAllowed = false
      setSelectedItem(applicationModel.getProjects().first())

      addStyleName(ROUNDED_EAST)
      addStyleName(ROUNDED_WEST)
      setHeight(28f, PIXELS)

      addSelectionListener{ e -> projectChangeEvent.fire(ProjectChangeEvent(e.selectedItem.get())) }
    }

    val userLabel = Label("${USER.html} ${applicationModel.getUsername()}").apply { contentMode = HTML }
    val separator = Label(LINE_V.html).apply { contentMode = HTML }
    val logoutLabel = Label("${USER.html} ${LOGOUT}").apply { contentMode = HTML }

    val logoutButton = Button(LOGOUT).apply {
      setIcon(KEY)
      addStyleName(ValoTheme.BUTTON_QUIET)
      addStyleName(ValoTheme.BUTTON_TINY)
      addStyleName(ROUNDED_EAST)
      addStyleName(ROUNDED_WEST)

      addClickListener { e -> logoutEvent.fire(LogoutEvent()) }
    }

    val profileSection = HorizontalLayout().apply {
      addComponents(userLabel, separator, logoutLabel)
      styleName = ValoTheme.LAYOUT_COMPONENT_GROUP
    }

    compositionRoot = HorizontalLayout().apply {
      val space = Label()
      addComponents(projectSelector, space, profileSection)
      setExpandRatio(space, 0.95f)
      setHeight(30f, PIXELS)
      setWidth(100f, PERCENTAGE)
    }

    setSizeUndefined();
  }
}