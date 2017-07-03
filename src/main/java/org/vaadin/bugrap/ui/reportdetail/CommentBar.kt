package org.vaadin.bugrap.ui.reportdetail

import com.vaadin.icons.VaadinIcons.USER
import com.vaadin.server.ExternalResource
import com.vaadin.server.Sizeable.Unit.PERCENTAGE
import com.vaadin.server.Sizeable.Unit.PIXELS
import com.vaadin.shared.ui.ContentMode.HTML
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.Link
import com.vaadin.ui.VerticalLayout
import org.vaadin.bugrap.core.CONTEXT_ROOT
import org.vaadin.bugrap.core.LABEL_GRAY_TEXT
import org.vaadin.bugrap.core.SMALL_TOP_MARGIN
import org.vaadin.bugrap.core.TOP_BORDER
import org.vaadin.bugrap.core.UNKNOWN_COMMENTER
import org.vaadin.bugrap.core.userFriendlyTimeDiff
import org.vaadin.bugrap.domain.entities.Comment


/**
 *
 * @author oladeji
 */
class CommentBar(private val comment: Comment) : CustomComponent() {

  init {
    val infoLabel = Label().apply {
      addStyleName(LABEL_GRAY_TEXT)
      val name = comment.author?.name ?: UNKNOWN_COMMENTER
      value = "$name (${userFriendlyTimeDiff(comment.timestamp)})"
    }

    val descriptionArea = Label().apply {
      value = comment.comment
      addStyleName("wrap-line")
      setWidth(100f, PERCENTAGE)
    }

    val topBar = HorizontalLayout().apply {
      val space = Label()

      addComponents(
          Label(USER.html, HTML),
          infoLabel,
          space
      )

      setHeight(2f, PIXELS)
      setWidth(100f, PERCENTAGE)
      setExpandRatio(space, 1f)
    }

    compositionRoot = VerticalLayout().apply {
      val descriptionLayout = HorizontalLayout().apply {
        val space = Label("&nbsp;&nbsp;").apply { contentMode = HTML }
        addComponents(space, descriptionArea)
      }

      addComponents(topBar, descriptionLayout)

      if (comment.attachment != null && comment.attachment.isNotEmpty()) {
        val space = Label().apply { setWidth(20f, PIXELS) }
        val link = Link(comment.attachmentName, ExternalResource(CONTEXT_ROOT + "attachment/" + comment.id))
        addComponents(HorizontalLayout().apply { addComponents(space, link) })
      }

      addStyleName(SMALL_TOP_MARGIN)
      addStyleName(TOP_BORDER)
      isSpacing = false
      setExpandRatio(descriptionLayout, 1f)
      setMargin(false)
      setSizeFull()
    }

    setSizeUndefined()
    setWidth(100f, PERCENTAGE)
  }
}