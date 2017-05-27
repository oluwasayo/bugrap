package com.vaadin.bugrap

import org.vaadin.bugrap.domain.BugrapRepository

/**
 *
 * @author oladeji
 */
class DbSetup {
  companion object {
    @JvmStatic fun main(args: Array<String>) {
      BugrapRepository("~/bugrap/bugrap.db").populateWithTestData()
    }
  }
}