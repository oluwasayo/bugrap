package com.vaadin.bugrap.ui.reportsoverview

import com.vaadin.bugrap.core.ApplicationModel
import com.vaadin.bugrap.events.ProjectChangeEvent
import com.vaadin.bugrap.events.VersionChangeEvent
import com.vaadin.server.Sizeable
import com.vaadin.server.Sizeable.Unit.MM
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.shared.ui.ContentMode.HTML
import com.vaadin.ui.CssLayout
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.NativeSelect
import com.vaadin.ui.themes.ValoTheme
import org.vaadin.bugrap.domain.entities.ProjectVersion
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
class VersionBar: CustomComponent() {

  @Inject
  private lateinit var applicationModel: ApplicationModel

  @Inject
  private lateinit var versionChangeEvent: Event<VersionChangeEvent>

  private lateinit var versionSelector: NativeSelect<ProjectVersion>

  @PostConstruct
  fun setup() {
    versionSelector = NativeSelect<ProjectVersion>(null, applicationModel.getVersions()).apply {
      setSelectedItem(applicationModel.getSelectedVersion())
      isEmptySelectionAllowed = true
      emptySelectionCaption = "All versions"

//      setHeight(28f, PIXELS)
      setWidth(40f, MM)
      addStyleName("rounded-east")
      addStyleName("rounded-west")

      addSelectionListener { e -> versionChangeEvent.fire(VersionChangeEvent(e.selectedItem.orElse(null))) }
    }

    val darkProgress = Label("0").apply {
      contentMode = HTML
      setWidth(33f, PERCENTAGE)
      addStyleName("label-dark")
      addStyleName("rounded-west")
    }
    val middleProgress = Label("0").apply {
      contentMode = HTML
      setWidth(33f, PERCENTAGE)
      addStyleName("label-gray")
    }
    val lightProgress = Label().apply {
      contentMode = HTML
      setWidth(34f, PERCENTAGE)
      addStyleName("label-light")
      addStyleName("rounded-east")
    }

    val progressIndicator = CssLayout().apply {
      addComponents(darkProgress, middleProgress, lightProgress)
      styleName = ValoTheme.LAYOUT_COMPONENT_GROUP
      setWidth("100%")
    }

    compositionRoot = HorizontalLayout().apply {
      addComponents(
          Label("Report for ").apply { setWidth(20f, MM) },
          versionSelector,
          Label().apply { setWidth(2f, MM) },
          progressIndicator
      )

      setExpandRatio(progressIndicator, 1f)
      setHeight(30f, Sizeable.Unit.PIXELS)
      setWidth(100f, Sizeable.Unit.PERCENTAGE)
    }

    setSizeUndefined()
  }

  fun updateVersionComponents(@Observes event: ProjectChangeEvent) {
    val versions = ApplicationModel.bugrapRepository.findProjectVersions(event.project).sortedBy { it.releaseDate }
    versionSelector.setItems(versions)
    versionSelector.setSelectedItem(null)
  }
}