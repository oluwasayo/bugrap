package org.vaadin.bugrap

import com.vaadin.testbench.TestBenchTestCase
import com.vaadin.testbench.elements.ButtonElement
import com.vaadin.testbench.elements.GridElement
import com.vaadin.testbench.elements.MenuBarElement
import com.vaadin.testbench.elements.NativeSelectElement
import com.vaadin.testbench.elements.TextFieldElement
import com.vaadin.testbench.elementsbase.AbstractElement
import com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.chrome.ChromeDriver
import org.vaadin.bugrap.core.ALL_KINDS
import org.vaadin.bugrap.core.EVERYONE
import org.vaadin.bugrap.core.MENUBAR_THEMED
import org.vaadin.bugrap.core.ONLY_ME
import org.vaadin.bugrap.core.OPEN
import org.vaadin.bugrap.domain.entities.Report.Status.CANT_FIX
import org.vaadin.bugrap.domain.entities.Report.Status.INVALID
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author oladeji
 */
class ReportsOverviewUIIT : TestBenchTestCase() {

  @Before
  fun setup() {
    System.setProperty("webdriver.chrome.driver", "src/test/resources/bin/chromedriver_2.24")
    setDriver(ChromeDriver())
//    val chromeOptions = ChromeOptions().apply { addArguments("--start-maximized") }
//    val theDriver = ChromeDriver(chromeOptions).apply { manage().window().maximize() }
//    setDriver(theDriver)
//    System.setProperty("phantomjs.ghostdriver.path", "")
//    System.setProperty("phantomjs.binary.path", "")
//    setDriver(TestBench.createDriver(PhantomJSDriver(DesiredCapabilities.phantomjs())))
    driver.get("http://localhost:8080/")
  }

  @After
  fun teardown() = driver.quit()

  private fun <T : AbstractElement> select(clazz: KClass<T>) = `$`(clazz.java)

  @Test
  fun testProjectSelector() {
    println("testProjectSelector")

    println("  -> Verify grid only contains reports from selected project")
    select(NativeSelectElement::class).first().apply {
      options.forEach {
        it.text.apply {
          selectByText(this)
          val projects = select(GridElement::class).first().rows.map { it.getCell(8).text }.distinct()
          if (projects.isNotEmpty()) {
            assertEquals(1, projects.size)
            assertEquals(this, projects.first())
          }
        }
      }

      selectByText("Project 1")
    }
  }

  @Test
  fun testVersionSelector() {
    println("testVersionSelector")

    println("  -> Verfiy version column contains multiple versions")
    assertTrue(select(GridElement::class).first().rows.map { it.getCell(1).text }.distinct().size > 1)

    println("  -> Verify version column disappears on version selection")
    select(NativeSelectElement::class).get(1).apply {
      selectByText("Version 1")
      assertEquals("Priority", select(GridElement::class).first().getHeaderCell(0, 1).text)

      selectByText("All versions")
    }
  }

  @Test
  fun testSearch() {
    println("testSearch")

    println("  -> Verify all reports in grid match search term")
    select(TextFieldElement::class).first().setValue("report")
    select(GridElement::class).first().rows.map { it.getCell(6).text }.forEach {
      assertTrue(it.contains("report"))
    }

    println("  -> Verify clear search botton clears search field")
    select(ButtonElement::class).first().click()
    assertTrue(select(TextFieldElement::class).first().value.isEmpty())
  }

  @Test
  fun testAssigneeFilter() {
    println("testAssigneeFilter")

    println("  -> Verify only \"my reports\" are on grid when \"Only me\" button is clicked")
    select(ButtonElement::class).caption(ONLY_ME).first().click()
    var assignees = select(GridElement::class).first().rows.map { it.getCell(4).text }.distinct()
    assertEquals(1, assignees.size)
    assertEquals("developer", assignees.first())

    println("  -> Verify \"Only me\" button is visually selected")
    assertTrue(select(ButtonElement::class).caption(ONLY_ME).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select(ButtonElement::class).caption(EVERYONE).first().hasClassName(BUTTON_PRIMARY))

    println("  -> Verify reports from multiple assignees are on grid when \"Everyone\" button is clicked")
    select(ButtonElement::class).caption(EVERYONE).first().click()
    assignees = select(GridElement::class).first().rows.map { it.getCell(4).text }.distinct()
    assertTrue(assignees.size > 1)

    println("  -> Verify \"Only me\" button is visually selected")
    assertTrue(select(ButtonElement::class).caption(EVERYONE).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select(ButtonElement::class).caption(ONLY_ME).first().hasClassName(BUTTON_PRIMARY))
  }

  @Test
  fun testStatusFilter() {
    println("testStatusFilter")

    println("  -> Verify only \"open\" are on grid when \"Open\" button is clicked")
    select(ButtonElement::class).caption(OPEN).first().click()
    var statuses = select(GridElement::class).first().rows.map { it.getCell(5).text }.distinct()
    println("Statuses: $statuses")
    assertEquals(1, statuses.size)
    assertEquals(OPEN, statuses.first())

    println("  -> Verify \"Open\" button is visually selected")
    assertTrue(select(ButtonElement::class).caption(OPEN).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select(ButtonElement::class).caption(ALL_KINDS).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select(MenuBarElement::class).first().hasClassName(MENUBAR_THEMED))

    println("  -> Verify reports with multiple statuses are on grid when \"All kinds\" button is clicked")
    select(ButtonElement::class).caption(ALL_KINDS).first().click()
    statuses = select(GridElement::class).first().rows.map { it.getCell(5).text }.distinct()
    assertTrue(statuses.size > 1)

    println("  -> Verify \"All kinds\" button is visually selected")
    assertTrue(select(ButtonElement::class).caption(ALL_KINDS).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select(ButtonElement::class).caption(OPEN).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select(MenuBarElement::class).first().hasClassName(MENUBAR_THEMED))

    println("  -> Verify custom filter works")
    select(MenuBarElement::class).first().click()
    select(MenuBarElement::class).first().waitForVaadin()
    Thread.sleep(1000) // Horrible hack as the waitForVaadin above did you help matters.
    select(MenuBarElement::class).first().clickItem(INVALID.toString())
    statuses = select(GridElement::class).first().rows.map { it.getCell(5).text }.distinct()
    assertEquals(1, statuses.size)
    assertEquals(INVALID.toString(), statuses.first())

    select(MenuBarElement::class).first().click()
    select(MenuBarElement::class).first().waitForVaadin()
    Thread.sleep(1000) // Horrible hack as the waitForVaadin above did you help matters.
    select(MenuBarElement::class).first().clickItem(CANT_FIX.toString())
    select(GridElement::class).first().waitForVaadin()
    statuses = select(GridElement::class).first().rows.map { it.getCell(5).text }.distinct()
    assertEquals(2, statuses.size)
    assertEquals(setOf(INVALID.toString(), CANT_FIX.toString()), HashSet(statuses))

    println("  -> Verify \"Custom\" is visually selected")
    assertTrue(select(MenuBarElement::class).first().hasClassName(MENUBAR_THEMED))
    assertFalse(select(ButtonElement::class).caption(ALL_KINDS).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select(ButtonElement::class).caption(OPEN).first().hasClassName(BUTTON_PRIMARY))

    println("  -> Verify filter goes back to \"All kinds\" when all custom filters are unselected")
    select(MenuBarElement::class).first().click()
    select(MenuBarElement::class).first().waitForVaadin()
    Thread.sleep(1000) // Horrible hack as the waitForVaadin above did you help matters.
    select(MenuBarElement::class).first().clickItem(INVALID.toString())
    select(MenuBarElement::class).first().waitForVaadin()
    Thread.sleep(1000) // Horrible hack as the waitForVaadin above did you help matters.
    select(MenuBarElement::class).first().click()
    select(MenuBarElement::class).first().waitForVaadin()
    Thread.sleep(1000) // Horrible hack as the waitForVaadin above did you help matters.
    select(MenuBarElement::class).first().clickItem(CANT_FIX.toString())
    select(MenuBarElement::class).first().waitForVaadin()
    assertTrue(select(ButtonElement::class).caption(ALL_KINDS).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select(ButtonElement::class).caption(OPEN).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select(MenuBarElement::class).first().hasClassName(MENUBAR_THEMED))
  }

  fun testGridSelection() {

  }

  fun testReportProperties() {

  }
}
