package com.vaadin.bugrap.core

import org.vaadin.bugrap.domain.entities.Report.Status
import java.io.Serializable
import javax.enterprise.context.SessionScoped

/**
 *
 * @author oladeji
 */
@SessionScoped
class Filter (
    var assignedOnlyMe: Boolean = false,
    val statuses: MutableSet<Status> = mutableSetOf()
) : Serializable {
  companion object {
    private const val serialVersionUID = 1L
  }
}