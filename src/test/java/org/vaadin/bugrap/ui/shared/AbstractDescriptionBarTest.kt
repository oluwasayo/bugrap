package org.vaadin.bugrap.ui.shared

import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.vaadin.bugrap.core.userFriendlyTimeDiff
import org.vaadin.bugrap.ui.report1
import org.vaadin.bugrap.ui.reportdetail.DetailDescriptionBar
import kotlin.test.assertEquals

/**
 * @author oladeji
 */
class AbstractDescriptionBarTest {

  private lateinit var sut: AbstractDescriptionBar

  @Before
  fun init() {
    sut = spy(DetailDescriptionBar())
  }

  @Test
  fun setReport() {
    println("setReport")

    doNothing().whenever(sut).updateUI()

    sut.report = report1

    println("  -> Verify updateUI called")
    verify(sut, times(1)).updateUI()
  }

  @Test
  fun updateUI() {
    println("updateUI")

    report1.author.name = null
    sut.report = report1

    println("  -> Verify selected report description displayed in text area")
    assertEquals(report1.description, sut.descriptionArea.value)

    println("  -> Verify \"Unknown Reporter\" shown along with time diff when reporter's name is unknown")
    assertEquals("Unknown Reporter (${userFriendlyTimeDiff(report1.reportedTimestamp)})",
        sut.infoLabel.value)

    println("  -> Verify reporter's name shown along with time diff when reporter's name is known")
    report1.author.name = "Sayo Oladeji"

    sut.updateUI()
    assertEquals("Sayo Oladeji (${userFriendlyTimeDiff(report1.reportedTimestamp)})",
        sut.infoLabel.value)

    report1.author.name = null
  }
}