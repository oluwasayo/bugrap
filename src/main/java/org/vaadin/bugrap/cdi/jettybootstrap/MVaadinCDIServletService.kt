package org.vaadin.bugrap.cdi.jettybootstrap

import com.vaadin.cdi.CDIViewProvider
import com.vaadin.cdi.internal.CDIUtil
import com.vaadin.cdi.internal.VaadinSessionDestroyEvent
import com.vaadin.external.org.slf4j.LoggerFactory
import com.vaadin.server.DeploymentConfiguration
import com.vaadin.server.ServiceException
import com.vaadin.server.SessionDestroyEvent
import com.vaadin.server.SessionDestroyListener
import com.vaadin.server.SessionInitEvent
import com.vaadin.server.SessionInitListener
import com.vaadin.server.VaadinRequest
import com.vaadin.server.VaadinResponse
import com.vaadin.server.VaadinServlet
import com.vaadin.server.VaadinServletService
import javax.enterprise.inject.spi.CDI

class MVaadinCDIServletService @Throws(ServiceException::class)
  constructor(servlet: VaadinServlet, configuration: DeploymentConfiguration)
    : VaadinServletService(servlet, configuration) {

  companion object {
    private val L = LoggerFactory.getLogger(MVaadinCDIServletService::class.java)
  }

  private val beanManager by lazy { CDIUtil.lookupBeanManager() ?: CDI.current().beanManager }

  init {
    val sessionListener = SessionListenerImpl()
    addSessionInitListener(sessionListener)
    addSessionDestroyListener(sessionListener)
  }

  @Throws(ServiceException::class)
  override fun handleRequest(request: VaadinRequest, response: VaadinResponse) {
    super.handleRequest(request, response)
    val event = CDIViewProvider.getCleanupEvent()
    if (event != null) {
      L.debug("Cleaning up after View changing request")
      beanManager.fireEvent(event)
      CDIViewProvider.removeCleanupEvent()
    }
  }

  private inner class SessionListenerImpl : SessionInitListener, SessionDestroyListener {

    override fun sessionInit(event: SessionInitEvent) {
      MVaadinCDIServletService.L.debug("Session init")
      event.session.addUIProvider(MCDIUIProvider())
    }

    override fun sessionDestroy(event: SessionDestroyEvent) {
      MVaadinCDIServletService.L.debug("Firing session destroy event")
      val sessionDestroyEvent = VaadinSessionDestroyEvent(
          CDIUtil.getSessionId(event.session))
      beanManager.fireEvent(sessionDestroyEvent)
    }
  }
}
