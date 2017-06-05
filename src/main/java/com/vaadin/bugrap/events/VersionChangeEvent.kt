package com.vaadin.bugrap.events

import org.vaadin.bugrap.domain.entities.ProjectVersion

/**
 *
 * @author oladeji
 */
class VersionChangeEvent(
    var version: ProjectVersion
)