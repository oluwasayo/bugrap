package org.vaadin.bugrap.ui.reportdetail

import com.vaadin.icons.VaadinIcons
import com.vaadin.server.ExternalResource
import com.vaadin.server.Page
import com.vaadin.ui.Button
import com.vaadin.ui.CssLayout
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.ProgressBar
import com.vaadin.ui.themes.ValoTheme.BUTTON_BORDERLESS
import com.vaadin.ui.themes.ValoTheme.BUTTON_TINY
import com.vaadin.ui.themes.ValoTheme.LAYOUT_HORIZONTAL_WRAPPING
import org.vaadin.bugrap.core.ATTACHMENT_BAR
import org.vaadin.bugrap.core.CONTEXT_ROOT
import org.vaadin.bugrap.core.NEW_WINDOW
import org.vaadin.bugrap.core.NO_LEFT_PADDING
import org.vaadin.bugrap.core.NO_VISUAL_FOCUS
import org.vaadin.bugrap.core.removeTimestampFromFileName
import org.vaadin.bugrap.domain.Attachment
import org.vaadin.bugrap.domain.UploadStatus.IN_PROGRESS
import java.io.File
import java.net.URLEncoder

/**
 *
 * @author oladeji
 */
class AttachmentsBar : CustomComponent() {

  val attachments = mutableMapOf<File, Attachment>()

  val container = HorizontalLayout().apply { addStyleName(LAYOUT_HORIZONTAL_WRAPPING) }

  init {
    compositionRoot = container
    updateUI()
    setSizeUndefined()
  }

  fun add(file: File, attachment: Attachment) {
    attachments.put(file, attachment)
    updateUI()
  }

  fun get(file: File) = attachments.get(file)!!

  fun remove(file: File) {
    attachments.remove(file)
    updateUI()
  }

  fun updateUI() {
    val keysInUI = container.iterator().asSequence().toSet()
    val toRemove = keysInUI.filterNot { attachments.containsKey((it as AttachmentComponent).file) }
    val toAdd = attachments.filterNot { k -> keysInUI.map { (it as AttachmentComponent).file }.contains(k.key) }

    toRemove.forEach { container.removeComponent(it) }
    toAdd.forEach { container.addComponent(AttachmentComponent(it.key, it.value)) }
    container.iterator().forEach { (it as AttachmentComponent).updateUI() }

    isVisible = attachments.isNotEmpty()
  }
}

class AttachmentComponent(val file: File, private val attachment: Attachment) : CustomComponent() {

  val nameButton = Button(removeTimestampFromFileName(file.name))
  val cancelButton = Button(VaadinIcons.CLOSE_SMALL)
  val progressBar = ProgressBar()

  init {
    addStyleName(ATTACHMENT_BAR)

    nameButton.apply {
      addStyleName(BUTTON_BORDERLESS)
      addStyleName(BUTTON_TINY)
      addStyleName(NO_VISUAL_FOCUS)
      addClickListener {
        Page.getCurrent().open(ExternalResource(CONTEXT_ROOT + "attachment?file=/"
            + URLEncoder.encode(file.name, Charsets.UTF_8.name())), NEW_WINDOW, false)
      }
    }

    cancelButton.apply {
      addStyleName(BUTTON_TINY)
      addStyleName(BUTTON_BORDERLESS)
      addStyleName(NO_VISUAL_FOCUS)
      addStyleName(NO_LEFT_PADDING)
    }

    updateUI()
    compositionRoot = CssLayout().apply { addComponents(nameButton, progressBar, cancelButton) }
  }

  fun updateUI() {
    progressBar.apply {
      isVisible = attachment.status == IN_PROGRESS
      isIndeterminate = attachment.totalSize <= 0
      value = Math.max(0.toFloat(), attachment.uploaded.toFloat() / attachment.totalSize)
    }
  }
}