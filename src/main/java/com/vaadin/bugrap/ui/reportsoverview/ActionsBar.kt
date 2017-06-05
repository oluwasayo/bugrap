package com.vaadin.bugrap.ui.reportsoverview

import com.vaadin.bugrap.core.ApplicationModel
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
import javax.enterprise.context.SessionScoped

/**
 *
 * @author oladeji
 */
@SessionScoped
class ActionsBar: CustomComponent() {

  @javax.inject.Inject
  private lateinit var applicationModel: ApplicationModel

  @javax.annotation.PostConstruct
  fun setup() {
    val reportBugLabel = Label("${BUG.html} Report a bug").apply { contentMode = HTML }
    val bugSeparatorLabel1 = Label(LINE_V.html).apply { contentMode = HTML }
    val featureLabel = Label("${PLUS.html} Request a feature").apply { contentMode = HTML }
    val bugSeparatorLabel2 = Label(LINE_V.html).apply { contentMode = HTML }
    val manageProjectLabel = Label("${COG.html} Manage project").apply { contentMode = HTML }

    val projectCountLabel = Label("127").apply {
      addStyleName("rounded-east")
      addStyleName("rounded-west")
      addStyleName("label-dark")
      setWidth(50f, PIXELS)
    }

    val searchField = TextField().apply {
      placeholder = "Search reports..."
      icon = SEARCH
      addStyleName(TEXTFIELD_INLINE_ICON)
      addStyleName(TEXTFIELD_TINY)
      addStyleName("rounded-west")
      setWidth(88.2f, PERCENTAGE)

      valueChangeMode = LAZY
      addValueChangeListener { e -> println("Stuff typed in search field: ${e.value}") }
    }

    val clearSearchButton = Button(CLOSE_SMALL).apply {
      addStyleName(BUTTON_TINY)
      addStyleName("rounded-east")
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
}