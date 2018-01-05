package org.vaadin.bugrap.endpoint

import org.vaadin.bugrap.core.removeTimestampFromFileName
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileReader
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author oladeji
 */
@WebServlet(name = "AttachmentViewer", urlPatterns = ["/attachment"])
class AttachmentViewer : HttpServlet() {

  @Throws(ServletException::class, IOException::class)
  override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
    val file = File(request.getParameter("file").substring(1))
    val downloadName = removeTimestampFromFileName(file.name)
    response.setHeader("Content-Disposition", "inline; filename=\"$downloadName\"")
    BufferedReader(FileReader(File("${file.name}.mime"))).use {
      response.contentType = "${it.readLine()}; name=\"$downloadName\""
    }

    BufferedInputStream(FileInputStream(file)).use {
      var byte: Int
      while (true) {
        byte = it.read()
        if (byte == -1) break
        response.outputStream.write(byte)
      }
    }

    response.outputStream.flush()
  }
}
