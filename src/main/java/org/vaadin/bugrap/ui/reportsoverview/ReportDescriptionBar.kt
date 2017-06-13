package org.vaadin.bugrap.ui.reportsoverview

import com.vaadin.icons.VaadinIcons.USER
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.shared.ui.ContentMode.HTML
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.TextArea
import com.vaadin.ui.VerticalLayout
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.LABEL_GRAY_TEXT
import org.vaadin.bugrap.core.SMALL_TOP_MARGIN
import org.vaadin.bugrap.core.TOP_BORDER
import org.vaadin.bugrap.core.WIDE_TEXTAREA
import org.vaadin.bugrap.events.ReportsRefreshEvent
import org.vaadin.bugrap.events.ReportsSelectionEvent
import java.util.Date
import javax.annotation.PostConstruct
import javax.enterprise.context.SessionScoped
import javax.enterprise.event.Observes
import javax.inject.Inject

/**
 *
 * @author oladeji
 */
@SessionScoped
class ReportDescriptionBar : CustomComponent() {

  @Inject
  private lateinit var applicationModel: ApplicationModel

  var infoLabel = Label().apply {
    addStyleName(LABEL_GRAY_TEXT)
  }

  var descriptionArea = TextArea().apply {
    addStyleName(WIDE_TEXTAREA)
    setWidth(100f, PERCENTAGE)
  }

  @PostConstruct
  fun setup() {
    var topBar = HorizontalLayout().apply {
      val space = Label()

      addComponents(
          Label(USER.html, HTML),
          infoLabel,
          space
      )

      setHeight(2f, PIXELS)
      setWidth(100f, PERCENTAGE)
      setExpandRatio(space, 1f)
    }

    compositionRoot = VerticalLayout().apply {
      addComponents(topBar, descriptionArea)
      addStyleName(SMALL_TOP_MARGIN)
      addStyleName(TOP_BORDER)
      isSpacing = false
      setExpandRatio(descriptionArea, 1f)
      setMargin(false)
      setWidth(100f, PERCENTAGE)
    }

    setSizeUndefined()
    setWidth(100f, PERCENTAGE)
    isVisible = false
  }

  fun updateUI(@Observes event: ReportsSelectionEvent) {
    isVisible = event.selectedReports.size == 1

    if (isVisible) {
      val report = event.selectedReports.first()
      val name = report.author?.name ?: "Unknown Reporter"
      infoLabel.caption = "$name (${userFriendlyTimeDiff(report.reportedTimestamp)})"
      descriptionArea.value = report.description
    }
  }

  fun updateUI(@Observes event: ReportsRefreshEvent) {
    isVisible = !applicationModel.getSelectedReports().isEmpty()
  }

  companion object {
    fun userFriendlyTimeDiff(date: Date): String {
      val diff = Date().time - date.time
      return when(diff) {
        in 0..20_000 -> "Just now"
        in 20_001..60_000 -> "${diff / 1_000} seconds ago"
        in 60_001..3_600_000 -> "${diff / 60_000} minutes ago"
        in 3_600_001..86_400_000 -> "${diff / 3_600_000} hours ago"
        in 86_400_401..604_800_800 -> "${diff / 86_400_000} days ago"
        in 604_800_801..2_678_400_000L -> "${diff / 604_800_800} weeks ago"
        in 2_678_400_000L..31_557_600_000L -> "${diff / 2_678_400_000L} months ago"
        else -> "${diff / 31_557_600_000L} years ago"
      }
    }
  }
}