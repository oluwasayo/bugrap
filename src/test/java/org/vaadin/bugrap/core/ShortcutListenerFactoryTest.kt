package org.vaadin.bugrap.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.vaadin.bugrap.core.ShortcutListenerFactory.newShortcutListener

/**
 * @author oladeji
 */
class ShortcutListenerFactoryTest {

  @Test
  fun testNewShortcutListener() {
    var actionPerformed = false
    val task = { o: Any?, o2: Any? -> actionPerformed = true }

    val listener = newShortcutListener("Sayo", 5, intArrayOf(1, 2, 3), task)
    assertEquals("Sayo", listener.caption)
    assertEquals(5, listener.keyCode)
    assertArrayEquals(intArrayOf(1, 2, 3), listener.modifiers)

    assertFalse(actionPerformed)
    listener.handleAction(null, null)
    assertTrue(actionPerformed)
  }
}