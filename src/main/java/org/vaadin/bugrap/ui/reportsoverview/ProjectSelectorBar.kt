package org.vaadin.bugrap.ui.reportsoverview

import com.vaadin.icons.VaadinIcons.LINE_V
import com.vaadin.icons.VaadinIcons.USER
import com.vaadin.server.Sizeable.Unit.MM
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.shared.ui.ContentMode.HTML
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.NativeSelect
import com.vaadin.ui.themes.ValoTheme.LAYOUT_COMPONENT_GROUP
import org.vaadin.bugrap.cdi.events.ProjectChangeEvent
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.LOGOUT
import org.vaadin.bugrap.core.ROUNDED_EAST
import org.vaadin.bugrap.core.ROUNDED_WEST
import org.vaadin.bugrap.domain.entities.Project
import javax.annotation.PostConstruct
import javax.enterprise.context.SessionScoped
import javax.enterprise.event.Event
import javax.inject.Inject

/**
 *
 * @author oladeji
 */
@SessionScoped
class ProjectSelectorBar @Inject constructor(private val applicationModel: ApplicationModel,
                                             private val projectChangeEvent: Event<ProjectChangeEvent>)
  : CustomComponent() {

  @PostConstruct
  fun setup() {
    val projectSelector = NativeSelect<Project>(null, applicationModel.getProjects()).apply {
      setWidth(100f, MM)
      isEmptySelectionAllowed = false
      setSelectedItem(applicationModel.getProjects().first())

      addStyleName(ROUNDED_EAST)
      addStyleName(ROUNDED_WEST)
      setHeight(28f, PIXELS)

      addSelectionListener {
        projectChangeEvent.fire(ProjectChangeEvent(it.selectedItem.get()))
      }
    }

    val userLabel = Label("${USER.html} ${applicationModel.getUsername()}", HTML)
    val separator = Label(LINE_V.html, HTML)
    val logoutLabel = Label("${USER.html} ${LOGOUT}", HTML)

    val profileSection = HorizontalLayout().apply {
      addComponents(userLabel, separator, logoutLabel)
      styleName = LAYOUT_COMPONENT_GROUP
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