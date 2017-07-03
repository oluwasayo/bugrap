package org.vaadin.bugrap.core

import javax.enterprise.event.Observes
import kotlin.reflect.KClass
import kotlin.test.assertTrue

/**
 *
 * @author oladeji
 */
fun <T : Any> verifyObserver(sut: T, function: String, event: KClass<*>) {
  var sutName = sut.javaClass.simpleName
  sutName = if (sutName.contains('$')) sutName.substring(0, sutName.indexOf('$')) else sutName
  println("  -> Verify ${sutName}.$function observes ${event.simpleName}")
  val parameter = sut.javaClass.getMethod(function, event.javaObjectType).parameters[0]
  assertTrue(parameter.isAnnotationPresent(Observes::class.java))
}

inline fun <reified T : Any> verifyObserver(sut: Any, function: String, event: Class<T>)
    = verifyObserver(sut, function, event.kotlin)