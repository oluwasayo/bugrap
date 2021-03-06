package org.vaadin.bugrap.ui.root

import com.vaadin.annotations.Theme
import com.vaadin.cdi.CDIUI
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.ui.MarginInfo
import com.vaadin.ui.Button
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.JavaScript
import com.vaadin.ui.Label
import com.vaadin.ui.Notification
import com.vaadin.ui.TextArea
import com.vaadin.ui.UI
import com.vaadin.ui.Upload
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.themes.ValoTheme.BUTTON_DANGER
import com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY
import com.vaadin.ui.themes.ValoTheme.BUTTON_TINY
import org.vaadin.bugrap.cdi.events.ReportsUpdateEvent
import org.vaadin.bugrap.core.ApplicationModel
import org.vaadin.bugrap.core.ApplicationModel.Companion.bugrapRepository
import org.vaadin.bugrap.core.Clock.currentTimeAsDate
import org.vaadin.bugrap.core.NO_VERSION
import org.vaadin.bugrap.core.REPORT_DETAIL_NO_ID_ERROR
import org.vaadin.bugrap.core.SCROLLABLE
import org.vaadin.bugrap.core.SMALL_PADDING
import org.vaadin.bugrap.core.WRITE_A_COMMENT
import org.vaadin.bugrap.core.addTimestampToFilename
import org.vaadin.bugrap.domain.Attachment
import org.vaadin.bugrap.domain.UploadStatus.COMPLETED
import org.vaadin.bugrap.domain.UploadStatus.IN_PROGRESS
import org.vaadin.bugrap.domain.entities.Comment
import org.vaadin.bugrap.domain.entities.Comment.Type.ATTACHMENT
import org.vaadin.bugrap.domain.entities.Comment.Type.COMMENT
import org.vaadin.bugrap.domain.entities.Report
import org.vaadin.bugrap.ui.reportdetail.AttachmentsBar
import org.vaadin.bugrap.ui.reportdetail.CommentBar
import org.vaadin.bugrap.ui.reportdetail.DetailDescriptionBar
import org.vaadin.bugrap.ui.reportdetail.SingleReportPropertiesBar
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter
import javax.enterprise.event.Observes
import javax.enterprise.event.Reception.IF_EXISTS
import javax.inject.Inject
import javax.persistence.NoResultException

/**
 *
 * @author oladeji
 */
@CDIUI("detail")
@Theme("mytheme")
class ReportDetailUI @Inject constructor(private val propertiesBar: SingleReportPropertiesBar,
                                         private val descriptionBar: DetailDescriptionBar,
                                         private val applicationModel: ApplicationModel)
  : UI() {

  private lateinit var report: Report

  val breadcrumb = Label()
  val attachmentsBar = AttachmentsBar()

  val commentsSection = VerticalLayout().apply {
    isSpacing = false
    margin = MarginInfo(false, false, false, true)
  }

  override fun init(request: VaadinRequest) {
    report = try {
      fetchReport(request.getParameter("id"))
    } catch (ex: Exception) {
      content = VerticalLayout().apply { addComponent(Label(ex.message)) }
      return
    }

    descriptionBar.setSizeFull()

    commentsSection.apply {
      bugrapRepository.findComments(report).forEach { addComponent(CommentBar(it)) }
      isVisible = iterator().hasNext()
    }

    val contentContainer = VerticalLayout().apply {
      addComponents(descriptionBar, commentsSection)
      addStyleName(SCROLLABLE)
      setHeight(100f, PERCENTAGE)
      setMargin(false)
    }

    content = VerticalLayout().apply {
      val newCommentArea = TextArea().apply {
        placeholder = WRITE_A_COMMENT
        setWidth(100f, PERCENTAGE)
        setMargin(true)
        addStyleName(SMALL_PADDING)
      }


      val controlsBar = HorizontalLayout()

      val doneButton = Button("Done").apply {
        addStyleName(BUTTON_TINY)
        addStyleName(BUTTON_PRIMARY)
        addClickListener {
          if (!newCommentArea.isEmpty) {
            val comment = saveComment(newCommentArea.value, ByteArray(0), null)
            commentsSection.addComponent(CommentBar(comment))
            newCommentArea.clear()
          }

          attachmentsBar.attachments.forEach {
            val data = ByteArray(it.key.length().toInt())
            BufferedInputStream(FileInputStream(it.key)).use { it.read(data) }
            val comment = saveComment(null, data, it.key.name)
            commentsSection.addComponent(CommentBar(comment))
          }

          attachmentsBar.clear()
          commentsSection.isVisible = commentsSection.iterator().hasNext()
        }
      }

      val attachmentsButton = Button("Attachment...").apply {
        addStyleName(BUTTON_TINY)
        addClickListener {
          var file = File("dummy")

          val upload = Upload(null) { filename, mimeType ->
            file = File(addTimestampToFilename(filename))
            PrintWriter(File("${file.name}.mime")).use {
              it.print(mimeType)
              it.flush()
            }

            attachmentsBar.add(file, Attachment().apply { status = IN_PROGRESS })
            BufferedOutputStream(FileOutputStream(file))
          }

          upload.addSucceededListener {
            attachmentsBar.get(file).apply {
              status = COMPLETED
              totalSize = file.length()
              uploaded = file.length()
            }

            controlsBar.removeComponent(upload)
            attachmentsBar.updateUI()
          }

          upload.addFailedListener {
            attachmentsBar.remove(file)
            controlsBar.removeComponent(upload)
            Notification.show("Failed to upload ${file.name}")
          }

          upload.addProgressListener { readBytes, contentLength ->
            attachmentsBar.get(file).apply {
              status = IN_PROGRESS
              uploaded = readBytes
              totalSize = contentLength
            }

            attachmentsBar.updateUI()
          }

          controlsBar.addComponent(upload)
          // TODO(oluwasayo): You can do better.
          JavaScript.getCurrent().execute("document.getElementsByClassName('gwt-FileUpload')[0].click()")
          doneButton.focus()
        }
      }

      val cancelButton = Button("Cancel").apply {
        addStyleName(BUTTON_TINY)
        addStyleName(BUTTON_DANGER)
        addClickListener {
          newCommentArea.clear()
          updateUI()
        }
      }

      controlsBar.addComponents(doneButton, attachmentsButton, cancelButton)
      addComponents(breadcrumb, propertiesBar, contentContainer, newCommentArea, attachmentsBar, controlsBar)

      setExpandRatio(contentContainer, 1f)
      setSizeFull()
    }

    page.setTitle("Bugrap - ${report.summary}")
    updateUI()
  }

  private fun saveComment(text: String?, attachment: ByteArray, attachmentName: String?): Comment {
    val comment = Comment().apply {
      if (attachment.isNotEmpty()) {
        this.attachment = attachment
        this.attachmentName = attachmentName
      }

      author = applicationModel.getUser()
      comment = text
      report = this@ReportDetailUI.report
      timestamp = currentTimeAsDate()
      type = if (attachment.isNotEmpty()) ATTACHMENT else COMMENT
    }

    return bugrapRepository.save(comment)
  }

  private fun fetchReport(idParam: String): Report {
    if (idParam.isEmpty()) {
      throw IllegalArgumentException(REPORT_DETAIL_NO_ID_ERROR)
    }

    val reportId = try {
      idParam.toLong()
    } catch (ex: NumberFormatException) {
      throw IllegalArgumentException("Invalid report ID ${idParam}. Please specify a numeric value.", ex)
    }

    return bugrapRepository.getReportById(reportId)
        ?: throw NoResultException("Report ${reportId} not found. Please double-check.")
  }

  fun updateUI(@Observes(notifyObserver = IF_EXISTS) event: ReportsUpdateEvent) {
    if (event.reports.contains(report)) {
      report = event.reports.first { it.id == report.id }
    }

    updateUI()
  }

  fun updateUI() {
    breadcrumb.value = "${report.project.name}  >  ${report.version?.version ?: NO_VERSION}"
    propertiesBar.setSelectedReports(setOf(report))
    descriptionBar.report = report
  }
}