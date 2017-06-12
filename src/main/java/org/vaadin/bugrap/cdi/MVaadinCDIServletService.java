package org.vaadin.bugrap.cdi;

import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.cdi.internal.CDIUtil;
import com.vaadin.cdi.internal.VaadinSessionDestroyEvent;
import com.vaadin.cdi.internal.VaadinViewChangeCleanupEvent;
import com.vaadin.cdi.server.VaadinCDIServletService;
import com.vaadin.server.DeploymentConfiguration;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionDestroyListener;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.util.logging.Logger;

public class MVaadinCDIServletService extends VaadinServletService {

  private BeanManager beanManager = null;

  protected final class SessionListenerImpl implements SessionInitListener, SessionDestroyListener {

    @Override
    public void sessionInit(SessionInitEvent event) {
      getLogger().fine("Session init");
      event.getSession().addUIProvider(new MCDIUIProvider());
    }

    @Override
    public void sessionDestroy(SessionDestroyEvent event) {
      getLogger().fine("Firing session destroy event.");
      VaadinSessionDestroyEvent sessionDestroyEvent = new VaadinSessionDestroyEvent(
          CDIUtil.getSessionId(event.getSession()));
      getBeanManager().fireEvent(sessionDestroyEvent);
    }

  }

  public MVaadinCDIServletService(VaadinServlet servlet, DeploymentConfiguration deploymentConfiguration)
      throws ServiceException {

    super(servlet, deploymentConfiguration);

    SessionListenerImpl sessionListener = new SessionListenerImpl();
    addSessionInitListener(sessionListener);
    addSessionDestroyListener(sessionListener);
  }

  protected BeanManager getBeanManager() {
    if (beanManager == null) {
      beanManager = CDIUtil.lookupBeanManager();
    }

    if (beanManager == null) {
      beanManager = CDI.current().getBeanManager();
    }

    return beanManager;
  }

  private static Logger getLogger() {
    return Logger.getLogger(VaadinCDIServletService.class.getCanonicalName());
  }

  @Override
  public void handleRequest(VaadinRequest request, VaadinResponse response) throws ServiceException {
    super.handleRequest(request, response);
    VaadinViewChangeCleanupEvent event = CDIViewProvider.getCleanupEvent();
    if (event != null) {
      getLogger().fine("Cleaning up after View changing request.");
      getBeanManager().fireEvent(event);
      CDIViewProvider.removeCleanupEvent();
    }
  }
}
