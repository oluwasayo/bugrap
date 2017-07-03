package org.vaadin.bugrap.ui.shared

import com.vaadin.icons.VaadinIcons.USER
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.shared.ui.ContentMode.HTML
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.TextArea
import com.vaadin.ui.VerticalLayout
import org.vaadin.bugrap.cdi.Proxyable
import org.vaadin.bugrap.core.LABEL_GRAY_TEXT
import org.vaadin.bugrap.core.SMALL_TOP_MARGIN
import org.vaadin.bugrap.core.TOP_BORDER
import org.vaadin.bugrap.core.UNKNOWN_REPORTER
import org.vaadin.bugrap.core.WIDE_TEXTAREA
import org.vaadin.bugrap.core.userFriendlyTimeDiff
import org.vaadin.bugrap.domain.entities.Report
import javax.annotation.PostConstruct

/**
 *
 * @author oladeji
 */
@Proxyable
abstract class AbstractDescriptionBar() : CustomComponent() {

  internal val infoLabel = Label()
  internal val descriptionArea = TextArea()

  internal var report: Report = Report()
    set(value) {
      field = value
      isVisible = true
      updateUI()
    }

  @PostConstruct
  fun setup() {
    infoLabel.addStyleName(LABEL_GRAY_TEXT)
    descriptionArea.addStyleName(WIDE_TEXTAREA)
    descriptionArea.setWidth(100f, PERCENTAGE)
//    descriptionArea.rows
    descriptionArea.setHeight(92f, PERCENTAGE)

    val topBar = HorizontalLayout().apply {
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
      setSizeFull()
    }

    setSizeFull()
    setWidth(100f, PERCENTAGE)
    isVisible = false
  }

  fun updateUI() {
    val name = report.author?.name ?: UNKNOWN_REPORTER
    infoLabel.value = "$name (${userFriendlyTimeDiff(report.reportedTimestamp)})"
    descriptionArea.value = report.description
  }
}