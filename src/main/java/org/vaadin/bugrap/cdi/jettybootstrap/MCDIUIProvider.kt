package org.vaadin.bugrap.cdi.jettybootstrap

import com.vaadin.cdi.CDIUIProvider
import javax.enterprise.inject.spi.CDI

class MCDIUIProvider : CDIUIProvider() {

  private val manager by lazy { CDI.current().beanManager ?: super.getBeanManager() }

  override fun getBeanManager() = manager
}
