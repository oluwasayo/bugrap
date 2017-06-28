package org.vaadin.bugrap.ui

import com.vaadin.testbench.TestBenchTestCase
import com.vaadin.testbench.elements.ButtonElement
import com.vaadin.testbench.elements.GridElement
import com.vaadin.testbench.elements.LabelElement
import com.vaadin.testbench.elements.LinkElement
import com.vaadin.testbench.elements.MenuBarElement
import com.vaadin.testbench.elements.NativeSelectElement
import com.vaadin.testbench.elements.TextAreaElement
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
import org.vaadin.bugrap.core.PRIORITY
import org.vaadin.bugrap.core.REVERT
import org.vaadin.bugrap.core.UPDATE
import org.vaadin.bugrap.domain.entities.Report.Priority.BLOCKER
import org.vaadin.bugrap.domain.entities.Report.Status.CANT_FIX
import org.vaadin.bugrap.domain.entities.Report.Status.INVALID
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
    driver.get("http://localhost:8080/home")
  }

  @After
  fun teardown() = driver.quit()

  private inline fun <reified T : AbstractElement> select() = `$`(T::class.java)

  @Test
  fun testProjectSelector() {
    println("testProjectSelector")

    println("  -> Verify grid only contains reports from selected project")
    select<NativeSelectElement>().first().apply {
      options.forEach {
        it.text.apply {
          selectByText(this)
          val projects = select<GridElement>().first().rows.map { it.getCell(8).text }.distinct()
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
    assertTrue(select<GridElement>().first().rows.map { it.getCell(1).text }.distinct().size > 1)

    println("  -> Verify version column disappears on version selection")
    select<NativeSelectElement>().get(1).apply {
      selectByText("Version 1")
      assertEquals("Priority", select<GridElement>().first().getHeaderCell(0, 1).text)

      selectByText("All versions")
    }
  }

  @Test
  fun testSearch() {
    println("testSearch")

    println("  -> Verify all reports in grid match search term")
    select<TextFieldElement>().first().setValue("report")
    select<GridElement>().first().rows.map { it.getCell(6).text }.forEach {
      assertTrue(it.contains("report"))
    }

    println("  -> Verify clear search botton clears search field")
    select<ButtonElement>().first().click()
    assertTrue(select<TextFieldElement>().first().value.isEmpty())
  }

  @Test
  fun testAssigneeFilter() {
    println("testAssigneeFilter")

    println("  -> Verify only \"my reports\" are on grid when \"Only me\" button is clicked")
    select<ButtonElement>().caption(ONLY_ME).first().click()
    var assignees = select<GridElement>().first().rows.map { it.getCell(4).text }.distinct()
    assertEquals(1, assignees.size)
    assertEquals("developer", assignees.first())

    println("  -> Verify \"Only me\" button is visually selected")
    assertTrue(select<ButtonElement>().caption(ONLY_ME).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select<ButtonElement>().caption(EVERYONE).first().hasClassName(BUTTON_PRIMARY))

    println("  -> Verify reports from multiple assignees are on grid when \"Everyone\" button is clicked")
    select<ButtonElement>().caption(EVERYONE).first().click()
    assignees = select<GridElement>().first().rows.map { it.getCell(4).text }.distinct()
    assertTrue(assignees.size > 1)

    println("  -> Verify \"Only me\" button is visually selected")
    assertTrue(select<ButtonElement>().caption(EVERYONE).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select<ButtonElement>().caption(ONLY_ME).first().hasClassName(BUTTON_PRIMARY))
  }

  @Test
  fun testStatusFilter() {
    println("testStatusFilter")

    println("  -> Verify only \"open\" are on grid when \"Open\" button is clicked")
    select<ButtonElement>().caption(OPEN).first().click()
    var statuses = select<GridElement>().first().rows.map { it.getCell(5).text }.distinct()
    println("Statuses: $statuses")
    assertEquals(1, statuses.size)
    assertEquals(OPEN, statuses.first())

    println("  -> Verify \"Open\" button is visually selected")
    assertTrue(select<ButtonElement>().caption(OPEN).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select<ButtonElement>().caption(ALL_KINDS).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select<MenuBarElement>().first().hasClassName(MENUBAR_THEMED))

    println("  -> Verify reports with multiple statuses are on grid when \"All kinds\" button is clicked")
    select<ButtonElement>().caption(ALL_KINDS).first().click()
    statuses = select<GridElement>().first().rows.map { it.getCell(5).text }.distinct()
    assertTrue(statuses.size > 1)

    println("  -> Verify \"All kinds\" button is visually selected")
    assertTrue(select<ButtonElement>().caption(ALL_KINDS).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select<ButtonElement>().caption(OPEN).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select<MenuBarElement>().first().hasClassName(MENUBAR_THEMED))

    println("  -> Verify custom filter works")
    select<MenuBarElement>().first().click()
    select<MenuBarElement>().first().waitForVaadin()
    Thread.sleep(1000) // Horrible hack as the waitForVaadin above did you help matters.
    select<MenuBarElement>().first().clickItem(INVALID.toString())
    statuses = select<GridElement>().first().rows.map { it.getCell(5).text }.distinct()
    assertEquals(1, statuses.size)
    assertEquals(INVALID.toString(), statuses.first())

    select<MenuBarElement>().first().click()
    select<MenuBarElement>().first().waitForVaadin()
    Thread.sleep(1000) // Horrible hack as the waitForVaadin above did you help matters.
    select<MenuBarElement>().first().clickItem(CANT_FIX.toString())
    select<GridElement>().first().waitForVaadin()
    statuses = select<GridElement>().first().rows.map { it.getCell(5).text }.distinct()
    assertEquals(2, statuses.size)
    assertEquals(setOf(INVALID.toString(), CANT_FIX.toString()), HashSet(statuses))

    println("  -> Verify \"Custom\" is visually selected")
    assertTrue(select<MenuBarElement>().first().hasClassName(MENUBAR_THEMED))
    assertFalse(select<ButtonElement>().caption(ALL_KINDS).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select<ButtonElement>().caption(OPEN).first().hasClassName(BUTTON_PRIMARY))

    println("  -> Verify filter goes back to \"All kinds\" when all custom filters are unselected")
    select<MenuBarElement>().first().click()
    select<MenuBarElement>().first().waitForVaadin()
    Thread.sleep(1000) // Horrible hack as the waitForVaadin above did you help matters.
    select<MenuBarElement>().first().clickItem(INVALID.toString())
    select<MenuBarElement>().first().waitForVaadin()
    Thread.sleep(1000) // Horrible hack as the waitForVaadin above did you help matters.
    select<MenuBarElement>().first().click()
    select<MenuBarElement>().first().waitForVaadin()
    Thread.sleep(1000) // Horrible hack as the waitForVaadin above did you help matters.
    select<MenuBarElement>().first().clickItem(CANT_FIX.toString())
    select<MenuBarElement>().first().waitForVaadin()
    assertTrue(select<ButtonElement>().caption(ALL_KINDS).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select<ButtonElement>().caption(OPEN).first().hasClassName(BUTTON_PRIMARY))
    assertFalse(select<MenuBarElement>().first().hasClassName(MENUBAR_THEMED))
  }

  @Test
  fun testGridSelection() {
    println("testGridSelection")

    val summary = select<GridElement>().first().getCell(1, 6).text

    println("  -> Verify report description and properties bars are displayed when one row is selected")
    select<GridElement>().first().getCell(1, 0).click()
    val detailArea = select<TextAreaElement>()
    assertTrue(detailArea.exists())
    assertTrue(detailArea.first().isDisplayed)
    val summaryLink = select<LinkElement>().caption(summary)
    assertTrue(summaryLink.exists())
    assertTrue(summaryLink.first().isDisplayed)

    println("  -> Verify report description and properties bars disappear when the only selected row is deselected")
    select<GridElement>().first().getCell(1, 0).click()
    assertFalse(select<TextAreaElement>().exists())
    assertFalse(select<LinkElement>().exists())

    println("  -> Verify report description bar is not displayed when more than one row is selected")
    select<GridElement>().first().getCell(1, 0).click()
    select<GridElement>().first().getCell(2, 0).click()
    assertFalse(select<TextAreaElement>().exists())
    assertFalse(select<LinkElement>().exists())
    assertTrue(select<LabelElement>().all().map { it.text }.filter { it.equals("2 reports selected") }.isNotEmpty())
  }

  @Test
  fun testReportProperties() {
    println("testReportProperties")

    var blockers = select<GridElement>().first().rows.filter { it.getCell(2).text == BLOCKER.name }.count()
    assertEquals(0, blockers)

    println("  -> Verify \"revert\" button")
    select<GridElement>().first().getCell(1, 0).click()
    select<GridElement>().first().getCell(2, 0).click()
    select<NativeSelectElement>().caption(PRIORITY.toUpperCase()).first().selectByText("Blocker")
    select<ButtonElement>().caption(REVERT).first().click()
    assertEquals("Critical", select<NativeSelectElement>().caption(PRIORITY.toUpperCase()).first().value)

    println("  -> Verify \"update\" button")
    select<NativeSelectElement>().caption(PRIORITY.toUpperCase()).first().selectByText("Blocker")
    select<ButtonElement>().caption(UPDATE).first().click()
    select<NativeSelectElement>().first().waitForVaadin()
    blockers = select<GridElement>().first().rows.filter { it.getCell(2).text == BLOCKER.name }.count()
    assertEquals(2, blockers)

    println("  -> Revert DB to initial state before test")
    select<GridElement>()
        .first()
        .rows
        .find { it.getCell(2).text == BLOCKER.name }!!
        .getCell(0)
        .click()

    select<GridElement>()
        .first()
        .rows
        .find { it.getCell(2).text == BLOCKER.name && !it.isSelected }!!
        .getCell(0)
        .click()

    select<NativeSelectElement>().caption(PRIORITY.toUpperCase()).first().selectByText("Critical")
    select<ButtonElement>().caption(UPDATE).first().click()
  }
}
