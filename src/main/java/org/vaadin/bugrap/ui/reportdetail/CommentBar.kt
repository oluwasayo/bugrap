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
import org.vaadin.bugrap.core.NEW_WINDOW
import org.vaadin.bugrap.core.SMALL_TOP_MARGIN
import org.vaadin.bugrap.core.TOP_BORDER
import org.vaadin.bugrap.core.UNKNOWN_COMMENTER
import org.vaadin.bugrap.core.WRAP_LINE
import org.vaadin.bugrap.core.removeTimestampFromFileName
import org.vaadin.bugrap.core.userFriendlyTimeDiff
import org.vaadin.bugrap.domain.entities.Comment
import java.net.URLEncoder


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
      addStyleName(WRAP_LINE)
      setWidth(90f, PERCENTAGE)
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
      addComponent(topBar)

      if (!comment.comment.isNullOrBlank()) {
        val descriptionLayout = HorizontalLayout().apply {
          val space = Label().apply { setWidth(17f, PIXELS) }
          addComponents(space, descriptionArea)
        }
        addComponents(descriptionLayout)
        setExpandRatio(descriptionLayout, 1f)
      }

      if (comment.attachment != null && comment.attachment.isNotEmpty()) {
        val space = Label().apply { setWidth(17f, PIXELS) }
        val link = Link(removeTimestampFromFileName(comment.attachmentName),
            ExternalResource(CONTEXT_ROOT + "attachment?file=/" + URLEncoder.encode(comment.attachmentName)))
        link.setTargetName(NEW_WINDOW);
        addComponents(HorizontalLayout().apply { addComponents(space, link) })
      }

      addStyleName(SMALL_TOP_MARGIN)
      addStyleName(TOP_BORDER)
      isSpacing = false
      setMargin(false)
      setSizeFull()
    }

    setSizeUndefined()
    setWidth(100f, PERCENTAGE)
  }
}