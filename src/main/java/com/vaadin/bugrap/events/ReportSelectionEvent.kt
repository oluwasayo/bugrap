package com.vaadin.bugrap.events

import org.vaadin.bugrap.domain.entities.Report

/**
 *
 * @author oladeji
 */
class ReportSelectionEvent(
    var selectedReports: Set<Report>
)