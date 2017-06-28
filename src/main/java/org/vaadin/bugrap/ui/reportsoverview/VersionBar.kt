package org.vaadin.bugrap.ui.reportsoverview

import com.vaadin.server.Sizeable.Unit.MM
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.ui.CssLayout
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.NativeSelect
import com.vaadin.ui.themes.ValoTheme.LAYOUT_COMPONENT_GROUP
import org.vaadin.bugrap.core.ALL_VERSIONS
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.ApplicationModel.Companion.bugrapRepository
import org.vaadin.bugrap.core.LABEL_DARK
import org.vaadin.bugrap.core.LABEL_GRAY
import org.vaadin.bugrap.core.LABEL_LIGHT
import org.vaadin.bugrap.core.LABEL_PROGRESS
import org.vaadin.bugrap.core.REPORT_FOR
import org.vaadin.bugrap.core.ROUNDED_EAST
import org.vaadin.bugrap.core.ROUNDED_WEST
import org.vaadin.bugrap.domain.entities.ProjectVersion
import org.vaadin.bugrap.cdi.events.ProjectChangeEvent
import org.vaadin.bugrap.cdi.events.VersionChangeEvent
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
class VersionBar() : CustomComponent() {

  private lateinit var applicationModel: ApplicationModel
  private lateinit var versionChangeEvent: Event<VersionChangeEvent>

  internal val versionSelector = NativeSelect<ProjectVersion>(null, emptySet<ProjectVersion>())

  internal val darkProgress = Label()
  internal val middleProgress = Label()
  internal val lightProgress = Label()

  @Inject
  constructor(applicationModel: ApplicationModel, versionChangeEvent: Event<VersionChangeEvent>) : this() {
    this.applicationModel = applicationModel
    this.versionChangeEvent = versionChangeEvent
  }

  @PostConstruct
  fun setup() {
    versionSelector.apply {
      this.setItems(applicationModel.getVersions())

      isEmptySelectionAllowed = true
      emptySelectionCaption = ALL_VERSIONS
      setSelectedItem(applicationModel.getSelectedVersion())

      setHeight(28f, PIXELS)
      setWidth(40f, MM)
      addStyleName(ROUNDED_EAST)
      addStyleName(ROUNDED_WEST)

      addSelectionListener {
        versionChangeEvent.fire(VersionChangeEvent(it.selectedItem.orElse(null)))
        updateDistributionBars()
      }
    }

    darkProgress.apply {
      addStyleName(LABEL_DARK)
      addStyleName(LABEL_PROGRESS)
      addStyleName(ROUNDED_WEST)
    }

    middleProgress.apply {
      addStyleName(LABEL_GRAY)
      addStyleName(LABEL_PROGRESS)
    }

    lightProgress.apply {
      addStyleName(LABEL_LIGHT)
      addStyleName(LABEL_PROGRESS)
      addStyleName(ROUNDED_EAST)
    }

    updateDistributionBars()

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
    val versions = bugrapRepository.findProjectVersions(event.project).sortedBy { it.releaseDate }
    versionSelector.setItems(versions)
    versionSelector.setSelectedItem(null)
    updateDistributionBars()
  }

  fun updateDistributionBars() {
    val closed: Long
    val open: Long
    val unassigned: Long

    if (applicationModel.getSelectedVersion() == null) {
      closed = bugrapRepository.countClosedReports(applicationModel.getSelectedProject())
      open = bugrapRepository.countOpenedReports(applicationModel.getSelectedProject())
      unassigned = bugrapRepository.countUnassignedReports(applicationModel.getSelectedProject())
    } else {
      closed = bugrapRepository.countClosedReports(applicationModel.getSelectedVersion())
      open = bugrapRepository.countOpenedReports(applicationModel.getSelectedVersion())
      unassigned = bugrapRepository.countUnassignedReports(applicationModel.getSelectedVersion())
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