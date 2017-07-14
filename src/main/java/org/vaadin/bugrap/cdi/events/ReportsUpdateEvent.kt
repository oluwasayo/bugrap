package org.vaadin.bugrap.cdi.events

import org.vaadin.bugrap.domain.entities.Report

/**
 *
 * @author oladeji
 */
class ReportsUpdateEvent(val reports: Set<Report>)