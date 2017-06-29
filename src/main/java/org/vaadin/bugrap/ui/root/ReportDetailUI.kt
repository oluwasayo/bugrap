package org.vaadin.bugrap.ui.root

import com.vaadin.annotations.Theme
import com.vaadin.cdi.CDIUI
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.Label
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.ApplicationModel.Companion.bugrapRepository
import org.vaadin.bugrap.core.WHITE_BACKGROUND
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.ui.reportdetail.SingleReportPropertiesBar
import javax.inject.Inject
import javax.persistence.NoResultException

/**
 *
 * @author oladeji
 */
@CDIUI("detail")
@Theme("mytheme")
class ReportDetailUI() : UI() {

  private lateinit var propertiesBar: SingleReportPropertiesBar

  val breadcrumb = Label()

  @Inject
  constructor(applicationModel: ApplicationModel, propertiesBar: SingleReportPropertiesBar) : this() {
    this.propertiesBar = propertiesBar
  }

  override fun init(request: VaadinRequest) {
    val report = try {
      fetchReport(request.getParameter("id"))
    } catch (ex: Exception) {
      content = VerticalLayout().apply {
        addComponent(Label(ex.message))
      }

      return
    }

    breadcrumb.addStyleName(WHITE_BACKGROUND)
    breadcrumb.value = "${report.project.name}  >  ${report.version?.version ?: "No version"}"

    propertiesBar.setSelectedReports(setOf(report))

    content = VerticalLayout().apply {
      addComponents(propertiesBar)
    }

    page.setTitle("Bugrap - ${report.summary}")
  }

  private fun fetchReport(idParam: String) : Report {
    if (idParam.isEmpty()) {
      throw IllegalArgumentException("Please specify the ID of the report to show.")
    }

    val reportId = try {
      idParam.toLong()
    } catch (ex: NumberFormatException) {
      throw IllegalArgumentException("Invalid report ID. Please specify a numeric value.", ex)
    }

    val report = bugrapRepository.getReportById(reportId)
    if (report == null) {
      throw NoResultException("Report ${reportId} not found. Please double-check.")
    }

    return report
  }
}