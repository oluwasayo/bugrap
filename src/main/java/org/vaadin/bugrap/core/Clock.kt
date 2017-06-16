package org.vaadin.bugrap.core

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date


/**
 *
 * @author oladeji
 */
class Clock {

  companion object {

    internal var frozen = false
    internal var frozenTime: LocalDateTime? = null

    @JvmStatic fun currentTime() = if (frozen) frozenTime!! else LocalDateTime.now()

    @JvmStatic fun currentTimeAsDate() = Date.from(currentTime().atZone(ZoneId.systemDefault()).toInstant())

    @JvmStatic fun freeze(date: LocalDateTime = LocalDateTime.now()): LocalDateTime {
      frozen = true
      frozenTime = date
      return date
    }

    @JvmStatic fun unfreeze() {
      frozen = false
      frozenTime = null
    }
  }
}