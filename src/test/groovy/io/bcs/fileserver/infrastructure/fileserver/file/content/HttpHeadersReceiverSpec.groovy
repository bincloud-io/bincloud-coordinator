package io.bcs.fileserver.infrastructure.fileserver.file.content

import javax.servlet.http.HttpServletResponse

import io.bce.promises.WaitingPromise
import io.bce.promises.Promise.ResponseHandler
import io.bcs.fileserver.domain.model.file.content.FileContent
import io.bcs.fileserver.domain.model.file.metadata.FileMetadata.Disposition
import io.bcs.fileserver.infrastructure.file.content.FileMetadataProvider
import io.bcs.fileserver.infrastructure.file.content.HttpHeadersReceiver

class HttpHeadersReceiverSpec extends ContentReceiverSpecification {
  private FileMetadataProvider metadataProvider

  def setup() {
    this.metadataProvider = Mock(FileMetadataProvider)
  }

  def "Scenario: receive headers for full content"() {
    given: "The http servlet response"
    HttpServletResponse servletResponse = Mock(HttpServletResponse)

    and: "The full size file content"
    FileContent fileContent = super.createFullSizeContent()
    1 * metadataProvider.getMetadataFor(_) >> createFileMetadata(Disposition.INLINE, 14L)

    and: "The http headers receiver"
    HttpHeadersReceiver receiver = new HttpHeadersReceiver(servletResponse, metadataProvider)

    and: "The promise resolve response handler"
    ResponseHandler<Void> responseHandler = Mock(ResponseHandler)

    when: "The full content is received"
    WaitingPromise.of(receiver.receiveFullContent(fileContent)).then(responseHandler).await()

    then: "The response handler should be resolved"
    1 * responseHandler.onResponse(_)

    and: "The \"Content-Type\" header should be set from file metadata"
    1 * servletResponse.setContentType(MEDIA_TYPE)

    and: "The \"Content-Disposition\" header should be set to the \"inline; filename=\"${FILE_NAME}\"\" value(from file metadata)"
    1 * servletResponse.setHeader("Content-Disposition", "inline; filename=\"${FILE_NAME}\"")

    and: "The \"Accept-Ranges\" header should be set to the \"bytes\" value"
    1 * servletResponse.setHeader("Accept-Ranges", "bytes")

    and: "The \"Content-Length\" header should be set to the file total length"
    1 * servletResponse.setHeader("Content-Length", String.valueOf(14L))

    and: "The response status should be set to ${HttpServletResponse.SC_OK}"
    1 * servletResponse.setStatus(HttpServletResponse.SC_OK)
  }

  def "Scenario: receive headers for single-range partial content"() {
    given: "The http servlet response"
    HttpServletResponse servletResponse = Mock(HttpServletResponse)

    and: "The single-range file content part"
    FileContent fileContent = super.createSingleRangeContent()
    1 * metadataProvider.getMetadataFor(_) >> createFileMetadata(Disposition.ATTACHMENT, DISTRIBUTIONING_CONTENT_LENGTH)

    and: "The http headers receiver"
    HttpHeadersReceiver receiver = new HttpHeadersReceiver(servletResponse, metadataProvider)

    and: "The promise resolve response handler"
    ResponseHandler<Void> responseHandler = Mock(ResponseHandler)

    when: "The single-range content is received"
    WaitingPromise.of(receiver.receiveContentRange(fileContent)).then(responseHandler).await()

    then: "The response handler should be resolved"
    1 * responseHandler.onResponse(_)

    and: "The \"Content-Type\" header should be set from file metadata"
    1 * servletResponse.setContentType(MEDIA_TYPE)

    and: "The \"Content-Disposition\" header should be set to the \"inline; filename=\"${FILE_NAME}\"\" value(from file metadata)"
    1 * servletResponse.setHeader("Content-Disposition", "attachment; filename=\"${FILE_NAME}\"")

    and: "The \"Accept-Ranges\" header should be set to the \"bytes\" value"
    1 * servletResponse.setHeader("Accept-Ranges", "bytes")

    and: "The \"Content-Length\" header should be set to the file total length"
    1 * servletResponse.setHeader("Content-Length", "14")

    and: "The \"Content-Range\" header should be set to the \"bytes 0-13/14\" value"
    1 * servletResponse.setHeader("Content-Range", "bytes 0-13/${DISTRIBUTIONING_CONTENT_LENGTH}")

    and: "The response status should be set to ${HttpServletResponse.SC_PARTIAL_CONTENT}"
    1 * servletResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT)
  }

  def "Scenario: receive headers for multi-range partial content"() {
    given: "The http servlet response"
    HttpServletResponse servletResponse = Mock(HttpServletResponse)

    and: "The multi-range file content parts"
    FileContent fileContent = super.createMultiRangeContent()
    1 * metadataProvider.getMetadataFor(_) >> createFileMetadata(Disposition.ATTACHMENT, DISTRIBUTIONING_CONTENT_LENGTH)

    and: "The http headers receiver"
    HttpHeadersReceiver receiver = new HttpHeadersReceiver(servletResponse, metadataProvider)

    and: "The promise resolve response handler"
    ResponseHandler<Void> responseHandler = Mock(ResponseHandler)

    when: "The multi-range content is received"
    WaitingPromise.of(receiver.receiveContentRanges(fileContent)).then(responseHandler).await()

    then: "The response handler should be resolved"
    1 * responseHandler.onResponse(_)

    and: "The \"Content-Type\" header should be set to \"${HttpHeadersReceiver.MULTIPART_MEDIA_TYPE}\" value"
    1 * servletResponse.setContentType(HttpHeadersReceiver.MULTIPART_MEDIA_TYPE)

    and: "The \"Content-Disposition\" header should be set to the \"inline; filename=\"${FILE_NAME}\"\" value(from file metadata)"
    1 * servletResponse.setHeader("Content-Disposition", "attachment; filename=\"${FILE_NAME}\"")

    and: "The \"Accept-Ranges\" header should be set to the \"bytes\" value"
    1 * servletResponse.setHeader("Accept-Ranges", "bytes")

    and: "The response status should be set to ${HttpServletResponse.SC_PARTIAL_CONTENT}"
    1 * servletResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT)
  }
}
