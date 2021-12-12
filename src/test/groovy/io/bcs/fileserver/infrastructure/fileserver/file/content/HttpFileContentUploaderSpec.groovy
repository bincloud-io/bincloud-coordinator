package io.bcs.fileserver.infrastructure.fileserver.file.content


import io.bce.interaction.streaming.Destination
import io.bce.interaction.streaming.binary.BinaryChunk
import io.bce.interaction.streaming.binary.OutputStreamDestination
import io.bce.promises.WaitingPromise
import io.bce.promises.Promise.ResponseHandler
import io.bce.streaming.DirectStreamer
import io.bcs.fileserver.domain.model.file.content.ContentUploader
import io.bcs.fileserver.domain.model.file.lifecycle.Lifecycle.FileUploadStatistic
import io.bcs.fileserver.domain.model.storage.ContentLocator
import io.bcs.fileserver.infrastructure.file.content.HttpFileContentUploader
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import spock.lang.Specification

class HttpFileContentUploaderSpec extends Specification {
  private static final String STORAGE_FILE_NAME = "flskdh1213120000.1234"
  private static final String STORAGE_NAME = "storage-1"
  private static final String TRANSFERRED_DATA = "Hello world!"
  def "Scenario: upload content"() {
    FileUploadStatistic uploadStatistic
    given: "The servlet request of file downloading"
    HttpServletRequest servletRequest = Stub(HttpServletRequest)
    servletRequest.getInputStream() >> Mock(ServletInputStream) {
      read(_) >> {
        byte[] array = TRANSFERRED_DATA.getBytes()
        System.arraycopy(array, 0, it[0], 0, array.length)
        return array.length
      } >> -1
    }


    and: "The content locator"
    ContentLocator locator = Stub(ContentLocator) {
      getStorageFileName() >> STORAGE_FILE_NAME
      getStorageName() >> STORAGE_NAME
    }

    and: "The destination"
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
    Destination<BinaryChunk> destination = new OutputStreamDestination(outputStream)

    and: "The http file content uploader"
    ContentUploader contentUploader = new HttpFileContentUploader(new DirectStreamer(), servletRequest, 100)

    and: "The response handler"
    ResponseHandler responseHandler = Mock(ResponseHandler)

    when: "The promise is transmitted"
    WaitingPromise.of(contentUploader.upload(locator, destination)).then(responseHandler).await()

    then: "The content should be transmitted"
    new String(outputStream.toByteArray()) == TRANSFERRED_DATA

    and: "The promise should be resolved"
    1 * responseHandler.onResponse(_) >> {uploadStatistic = it[0]}
    uploadStatistic.getLocator().getStorageFileName() == STORAGE_FILE_NAME
    uploadStatistic.getTotalLength() == TRANSFERRED_DATA.getBytes().length
  }
}
