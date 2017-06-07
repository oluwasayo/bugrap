package org.vaadin.bugrap.ui.reportsoverview

import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.shared.ui.ContentMode.HTML
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label

/**
 *
 * @author oladeji
 */
class HorizontalRule : HorizontalLayout() {

  init {
    val horizontalRuleLabel = Label("<hr />").apply {
      contentMode = HTML
      setWidth(100f, PERCENTAGE)
      setMargin(false)
    }

    addComponent(horizontalRuleLabel)
    setExpandRatio(horizontalRuleLabel, 1f)
    setHeight(1f, PIXELS)
    setWidth(100f, PERCENTAGE)
    setMargin(false)
  }
}