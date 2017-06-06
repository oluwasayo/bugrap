package org.vaadin.bugrap.ui.reportsoverview

import com.vaadin.server.Sizeable.Unit.MM
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.shared.ui.ContentMode.HTML
import com.vaadin.ui.CssLayout
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.NativeSelect
import com.vaadin.ui.themes.ValoTheme.LAYOUT_COMPONENT_GROUP
import org.vaadin.bugrap.core.ALL_VERSIONS
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.LABEL_DARK
import org.vaadin.bugrap.core.LABEL_GRAY
import org.vaadin.bugrap.core.LABEL_LIGHT
import org.vaadin.bugrap.core.LABEL_PROGRESS
import org.vaadin.bugrap.core.REPORT_FOR
import org.vaadin.bugrap.core.ROUNDED_EAST
import org.vaadin.bugrap.core.ROUNDED_WEST
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.events.ProjectChangeEvent
import org.vaadin.bugrap.events.VersionChangeEvent
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

  val darkProgress = Label()
  val middleProgress = Label()
  val lightProgress = Label()

  @PostConstruct
  fun setup() {
    versionSelector = NativeSelect<ProjectVersion>(null, applicationModel.getVersions()).apply {
      isEmptySelectionAllowed = true
      emptySelectionCaption = ALL_VERSIONS
      setSelectedItem(applicationModel.getSelectedVersion())

      setHeight(28f, PIXELS)
      setWidth(40f, MM)
      addStyleName(ROUNDED_EAST)
      addStyleName(ROUNDED_WEST)

      addSelectionListener { e ->
        versionChangeEvent.fire(VersionChangeEvent(e.selectedItem.orElse(null)))
        updateProgressBars()
      }
    }

    darkProgress.apply {
      contentMode = HTML
      addStyleName(LABEL_DARK)
      addStyleName(LABEL_PROGRESS)
      addStyleName(ROUNDED_WEST)
    }

    middleProgress.apply {
      contentMode = HTML
      addStyleName(LABEL_GRAY)
      addStyleName(LABEL_PROGRESS)
    }

    lightProgress.apply {
      contentMode = HTML
      addStyleName(LABEL_LIGHT)
      addStyleName(LABEL_PROGRESS)
      addStyleName(ROUNDED_EAST)
    }

    updateProgressBars()

    val progressIndicator = CssLayout().apply {
      addComponents(darkProgress, middleProgress, lightProgress)
      styleName = LAYOUT_COMPONENT_GROUP
      setWidth(100f, PERCENTAGE)
    }

    compositionRoot = HorizontalLayout().apply {
      addComponents(
          Label(REPORT_FOR).apply { setWidth(20f, MM) },
          versionSelector,
          Label().apply { setWidth(2f, MM) },
          progressIndicator
      )

      setExpandRatio(progressIndicator, 1f)
      setHeight(30f, PIXELS)
      setWidth(100f, PERCENTAGE)
    }

    setSizeUndefined()
  }

  fun updateVersionComponents(@Observes event: ProjectChangeEvent) {
    val versions = ApplicationModel.bugrapRepository.findProjectVersions(event.project).sortedBy { it.releaseDate }
    versionSelector.setItems(versions)
    versionSelector.setSelectedItem(null)
    updateProgressBars()
  }

  fun updateProgressBars() {
    var closed = 0L
    var open = 0L
    var unassigned = 0L

    if (applicationModel.getSelectedVersion() == null) {
      closed = ApplicationModel.bugrapRepository.countClosedReports(applicationModel.getSelectedProject())
      open = ApplicationModel.bugrapRepository.countOpenedReports(applicationModel.getSelectedProject())
      unassigned = ApplicationModel.bugrapRepository.countUnassignedReports(applicationModel.getSelectedProject())
    } else {
      closed = ApplicationModel.bugrapRepository.countClosedReports(applicationModel.getSelectedVersion())
      open = ApplicationModel.bugrapRepository.countOpenedReports(applicationModel.getSelectedVersion())
      unassigned = ApplicationModel.bugrapRepository.countUnassignedReports(applicationModel.getSelectedVersion())
    }

    val total = closed + open + unassigned

    darkProgress.apply {
      value = closed.toString()
      setWidth(if (total == 0L) 33f else (closed * 100f / total), PERCENTAGE)
    }

    middleProgress.apply {
      value = open.toString()
      setWidth(if (total == 0L) 33f else (open * 100f / total), PERCENTAGE)
    }

    lightProgress.apply {
      value = unassigned.toString()
      setWidth(if (total == 0L) 34f else (unassigned * 100f / total), PERCENTAGE)
    }
  }
}