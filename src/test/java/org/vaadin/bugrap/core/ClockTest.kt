package org.vaadin.bugrap.core

import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * @author oladeji
 */
class ClockTest {

  @Before
  fun init() {
    Clock.unfreeze()
  }

  @Test
  fun freeze() {
    println("freeze")

    val frozenTime = Clock.freeze()
    assertTrue(Clock.frozen)
    assertEquals(frozenTime, Clock.currentTime())
  }

  @Test
  fun unfreeze() {
    println("unfreeze")

    val frozenTime = Clock.freeze(LocalDateTime.now().minusSeconds(2))
    Clock.unfreeze()
    assertFalse(Clock.frozen)
    assertNotEquals(frozenTime, Clock.currentTime())
  }
}