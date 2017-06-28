package org.vaadin.bugrap.ui

import com.vaadin.annotations.Theme
import com.vaadin.cdi.CDIUI
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.Label
import com.vaadin.ui.UI
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.ApplicationModel.Companion.bugrapRepository
import org.vaadin.bugrap.ui.reportdetail.BasePropertiesBar
import javax.inject.Inject

/**
 *
 * @author oladeji
 */
@CDIUI("detail")
@Theme("mytheme")
class ReportDetailUI() : UI() {

  private lateinit var propertiesBar: BasePropertiesBar

  val breadcrumb = Label()

  @Inject
  constructor(applicationModel: ApplicationModel, propertiesBar: BasePropertiesBar) : this() {
    this.propertiesBar = propertiesBar
  }

  override fun init(request: VaadinRequest) {
    val report = bugrapRepository.getReportById(request.getParameter("id").first().toLong())
    propertiesBar.setSelectedReports(setOf(report))

    content = propertiesBar
    page.setTitle("Bugrap - ${report.summary}")
  }
}