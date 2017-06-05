package com.vaadin.bugrap.events

import org.vaadin.bugrap.domain.entities.Project

/**
 *
 * @author oladeji
 */
class ProjectChangeEvent(
    var project: Project? = null
)