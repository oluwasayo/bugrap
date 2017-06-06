package org.vaadin.bugrap.ui.reportsoverview

import com.vaadin.icons.VaadinIcons.BUG
import com.vaadin.icons.VaadinIcons.CLOSE_SMALL
import com.vaadin.icons.VaadinIcons.COG
import com.vaadin.icons.VaadinIcons.LINE_V
import com.vaadin.icons.VaadinIcons.PLUS
import com.vaadin.icons.VaadinIcons.SEARCH
import com.vaadin.server.Sizeable.Unit.MM
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.shared.ui.ContentMode.HTML
import com.vaadin.shared.ui.ValueChangeMode.LAZY
import com.vaadin.ui.Button
import com.vaadin.ui.CssLayout
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.TextField
import com.vaadin.ui.themes.ValoTheme.BUTTON_TINY
import com.vaadin.ui.themes.ValoTheme.LAYOUT_COMPONENT_GROUP
import com.vaadin.ui.themes.ValoTheme.TEXTFIELD_INLINE_ICON
import com.vaadin.ui.themes.ValoTheme.TEXTFIELD_TINY
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.LABEL_DARK
import org.vaadin.bugrap.core.MANAGE_PROJECT
import org.vaadin.bugrap.core.REPORT_A_BUG
import org.vaadin.bugrap.core.REQUEST_A_FEATURE
import org.vaadin.bugrap.core.ROUNDED_EAST
import org.vaadin.bugrap.core.ROUNDED_WEST
import org.vaadin.bugrap.core.SEARCH_REPORTS
import org.vaadin.bugrap.events.ProjectChangeEvent
import org.vaadin.bugrap.events.SearchEvent
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
class ActionsBar : CustomComponent() {

  @Inject
  private lateinit var applicationModel: ApplicationModel

  @Inject
  private lateinit var searchEvent: Event<SearchEvent>

  private val projectCountLabel = Label()

  @PostConstruct
  fun setup() {
    val reportBugLabel = Label("${BUG.html} ${REPORT_A_BUG}").apply { contentMode = HTML }
    val bugSeparatorLabel1 = Label(LINE_V.html).apply { contentMode = HTML }
    val featureLabel = Label("${PLUS.html} ${REQUEST_A_FEATURE}").apply { contentMode = HTML }
    val bugSeparatorLabel2 = Label(LINE_V.html).apply { contentMode = HTML }
    val manageProjectLabel = Label("${COG.html} ${MANAGE_PROJECT}").apply { contentMode = HTML }

    projectCountLabel.apply {
      addStyleName(ROUNDED_EAST)
      addStyleName(ROUNDED_WEST)
      addStyleName(LABEL_DARK)
      setWidth(50f, PIXELS)
    }

    updateReportCount(ProjectChangeEvent(applicationModel.getSelectedProject()))

    val searchField = TextField().apply {
      placeholder = SEARCH_REPORTS
      icon = SEARCH
      addStyleName(TEXTFIELD_INLINE_ICON)
      addStyleName(TEXTFIELD_TINY)
      addStyleName(ROUNDED_WEST)
      setWidth(88.2f, PERCENTAGE)

      valueChangeMode = LAZY
      addValueChangeListener { e -> searchEvent.fire(SearchEvent(e.value)) }
    }

    val clearSearchButton = Button(CLOSE_SMALL).apply {
      addStyleName(BUTTON_TINY)
      addStyleName(ROUNDED_EAST)
      addClickListener { e -> searchField.clear() }
    }

    val searchBar = CssLayout().apply {
      addComponents(searchField, clearSearchButton)
      styleName = LAYOUT_COMPONENT_GROUP
      setWidth(90f, MM)
    }

    compositionRoot = HorizontalLayout().apply {
      val space = Label()
      addComponents(
          reportBugLabel,
          bugSeparatorLabel1,
          featureLabel,
          bugSeparatorLabel2,
          manageProjectLabel,
          projectCountLabel,
          space,
          searchBar
      )

      styleName = LAYOUT_COMPONENT_GROUP
      setExpandRatio(space, 1f)
      setWidth(100f, PERCENTAGE)
      setHeight(10f, PIXELS)
    }

    setSizeUndefined()
  }

  private fun countReports(): String {
    if (applicationModel.getSelectedVersion() != null) {
      return ApplicationModel.bugrapRepository.countReports(applicationModel.getSelectedVersion()).toString()
    }

    return ApplicationModel.bugrapRepository.countReports(applicationModel.getSelectedProject()).toString()
  }

  fun updateReportCount(@Observes event: ProjectChangeEvent) {
    projectCountLabel.value = countReports()
  }

  fun updateReportCount(@Observes event: VersionChangeEvent) {
    projectCountLabel.value = countReports()
  }
}