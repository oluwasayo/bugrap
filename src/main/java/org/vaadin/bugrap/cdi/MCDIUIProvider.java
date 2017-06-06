package org.vaadin.bugrap.cdi;

import com.vaadin.cdi.CDIUIProvider;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

public class MCDIUIProvider extends CDIUIProvider {
    BeanManager beanManager = null;

    @Override
    public BeanManager getBeanManager() {

        if (beanManager == null) {
            beanManager = CDI.current().getBeanManager();
        }
        if (beanManager == null) {
            beanManager = super.getBeanManager();
        }
        return beanManager;
    }
}
