package org.vaadin.bugrap.cdi.events

import org.vaadin.bugrap.domain.entities.Report

/**
 *
 * @author oladeji
 */
class ReportsSelectionEvent(val selectedReports: Set<Report>)