package org.vaadin.bugrap.core

import javax.enterprise.event.Observes
import kotlin.reflect.KClass
import kotlin.test.assertTrue

/**
 *
 * @author oladeji
 */
fun <T : Any> verifyObserver(sut: T, function: String, event: KClass<*>) {
  val sutName = sut.javaClass.simpleName
  println("  -> Verify ${sutName.substring(0, sutName.indexOf('$'))}.$function observes ${event.simpleName}")
  val parameter = sut.javaClass.getMethod(function, event.javaObjectType).parameters[0]
  assertTrue(parameter.isAnnotationPresent(Observes::class.java))
}