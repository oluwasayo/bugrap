package org.vaadin.bugrap.ui.reportsoverview

import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.shared.ui.ContentMode.HTML
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.Label

/**
 *
 * @author oladeji
 */
class HorizontalRule : CustomComponent() {

  init {
    compositionRoot = Label("<hr />", HTML).apply {
      setHeight(1f, PIXELS)
      setWidth(100f, PERCENTAGE)
    }
  }
}