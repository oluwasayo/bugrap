package org.vaadin.bugrap.cdi.jettybootstrap

import com.vaadin.cdi.server.VaadinCDIServlet
import com.vaadin.server.DeploymentConfiguration
import com.vaadin.server.ServiceException

class MVaadinCDIServlet : VaadinCDIServlet() {

  @Throws(ServiceException::class)
  override fun createServletService(configuration: DeploymentConfiguration)
     = MVaadinCDIServletService(this, configuration).apply { init() }
}
