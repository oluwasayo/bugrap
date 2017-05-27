package com.vaadin.bugrap

import com.vaadin.annotations.Theme
import com.vaadin.annotations.VaadinServletConfiguration
import com.vaadin.server.VaadinRequest
import com.vaadin.server.VaadinServlet
import com.vaadin.ui.Button
import com.vaadin.ui.Label
import com.vaadin.ui.TextField
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout

import javax.servlet.annotation.WebServlet

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 *
 * The UI is initialized using [.init]. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
class MyUI : UI() {

  override fun init(vaadinRequest: VaadinRequest) {
    val layout = VerticalLayout()
    val name = TextField().apply { caption = "Type your name here:" }
    val button = Button("Click Me").apply {
      addClickListener { e -> layout.addComponent(Label("Thanks ${name.value}, it works!")) }
    }

    content = layout.apply { addComponents(name, button) }
  }

  @WebServlet(urlPatterns = arrayOf("/*"), name = "MyUIServlet", asyncSupported = true)
  @VaadinServletConfiguration(ui = MyUI::class, productionMode = false)
  class MyUIServlet : VaadinServlet()
}
