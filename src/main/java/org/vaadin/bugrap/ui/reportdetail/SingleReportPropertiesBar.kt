package org.vaadin.bugrap.ui.reportdetail

import org.vaadin.bugrap.cdi.events.ReportsUpdateEvent
import org.vaadin.bugrap.ui.shared.AbstractPropertiesBar
import javax.enterprise.context.Dependent
import javax.enterprise.event.Event
import javax.inject.Inject

/**
 *
 * @author oladeji
 */
@Dependent
class SingleReportPropertiesBar @Inject constructor(reportsRefreshEvent: Event<ReportsUpdateEvent>)
  : AbstractPropertiesBar(reportsRefreshEvent)