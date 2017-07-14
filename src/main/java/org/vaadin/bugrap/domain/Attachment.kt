package org.vaadin.bugrap.domain

import org.vaadin.bugrap.domain.UploadStatus.PENDING

/**
 *
 * @author oladeji
 */
class Attachment {
  var totalSize: Long = 0L
  var uploaded = 0L
  var status: UploadStatus = PENDING
}

enum class UploadStatus {
  PENDING,
  IN_PROGRESS,
  COMPLETED,
  INTERRRUPTED
}