package io.bcs.port.adapters.file

import javax.servlet.ServletOutputStream
import javax.servlet.WriteListener
import javax.servlet.http.HttpServletResponse


import io.bce.promises.Promise.ResponseHandler
import io.bce.promises.WaitingPromise
import io.bce.streaming.DirectStreamer
import io.bcs.domain.model.file.FileContent

class HttpFileDataReceiverSpec extends ContentReceiverSpecification {
    protected static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
    private static final String MULTIPART_SEPARATOR = "--" + MULTIPART_BOUNDARY;
    private static final String MULTIPART_ENDING = MULTIPART_SEPARATOR + "--";

    def "Scenario: receive data for full content"() {
        given: "The http servlet response"
        ByteArrayOutputStream destination = new ByteArrayOutputStream()
        ServletOutputStream servletStream = Mock(ServletOutputStream) {
            write(_) >> {destination.write(it[0])}
        }

        HttpServletResponse servletResponse = Mock(HttpServletResponse)
        servletResponse.getOutputStream() >> servletStream

        and: "The full size file content"
        FileContent fileContent = super.createFullSizeContent()

        and: "The file data receiver"
        HttpFileDataReceiver receiver = new HttpFileDataReceiver(new DirectStreamer(), servletResponse)

        and: "The promise resolve response handler"
        ResponseHandler<Void> responseHandler = Mock(ResponseHandler)

        when: "The full content is received"
        WaitingPromise.of(receiver.receiveFullContent(fileContent)).then(responseHandler).await()

        then: "The response handler should be resolved"
        1 * responseHandler.onResponse(_)

        and: "The full content should be transferred"
        destination.toString() == "Hello World!!!"
        
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

    def "Scenario: receive data for single-range partial content"() {
        given: "The http servlet response"
        ByteArrayOutputStream destination = new ByteArrayOutputStream()
        ServletOutputStream servletStream = Mock(ServletOutputStream) {
            write(_) >> {destination.write(it[0])}
        }

        HttpServletResponse servletResponse = Mock(HttpServletResponse)
        servletResponse.getOutputStream() >> servletStream

        and: "The single-range file content part"
        FileContent fileContent = super.createSingleRangeContent()

        and: "The file data receiver"
        HttpFileDataReceiver receiver = new HttpFileDataReceiver(new DirectStreamer(), servletResponse)

        and: "The promise resolve response handler"
        ResponseHandler<Void> responseHandler = Mock(ResponseHandler)

        when: "The single-range content is received"
        WaitingPromise.of(receiver.receiveContentRange(fileContent)).then(responseHandler).await()

        then: "The response handler should be resolved"
        1 * responseHandler.onResponse(_)

        and: "The content part should be transferred"
        destination.toString() == "Hello World!!!"
        
        and: "The \"Content-Type\" header should be set from file metadata"
        1 * servletResponse.setContentType(MEDIA_TYPE)

        and: "The \"Content-Disposition\" header should be set to the \"inline; filename=\"${FILE_NAME}\"\" value(from file metadata)"
        1 * servletResponse.setHeader("Content-Disposition", "attachment; filename=\"${FILE_NAME}\"")

        and: "The \"Accept-Ranges\" header should be set to the \"bytes\" value"
        1 * servletResponse.setHeader("Accept-Ranges", "bytes")

        and: "The \"Content-Length\" header should be set to the file total length"
        1 * servletResponse.setHeader("Content-Length", String.valueOf(14L))
        
        and: "The \"Content-Range\" header shoudl be set to the \"bytes 0-13/${DISTRIBUTIONING_CONTENT_LENGTH}\" value"
        1 * servletResponse.setHeader("Content-Range", "bytes 0-13/${DISTRIBUTIONING_CONTENT_LENGTH}")

        and: "The response status should be set to ${HttpServletResponse.SC_PARTIAL_CONTENT}"
        1 * servletResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT)
    }

    def "Scenario: receive data for multi-range partial content"() {
        given: "The http servlet response"
        ByteArrayOutputStream destination = new ByteArrayOutputStream()
        ServletOutputStream servletStream = Mock(ServletOutputStream) {
            write(_) >> {destination.write(it[0])}
        }

        HttpServletResponse servletResponse = Mock(HttpServletResponse)
        servletResponse.getOutputStream() >> servletStream

        and: "The multi-range file content parts"
        FileContent fileContent = super.createMultiRangeContent()

        and: "The file data receiver"
        HttpFileDataReceiver receiver = new HttpFileDataReceiver(new DirectStreamer(), servletResponse)

        and: "The promise resolve response handler"
        ResponseHandler<Void> responseHandler = Mock(ResponseHandler)

        when: "The multi-range content is received"
        WaitingPromise.of(receiver.receiveContentRanges(fileContent)).then(responseHandler).await()

        then: "The response handler should be resolved"
        1 * responseHandler.onResponse(_)

        and: "The multiple content parts should be transferred"
        destination.toString() == createContentPartsBody()
        
        and: "The \"Content-Type\" header should be set to \"${HttpHeadersReceiver.MULTIPART_MEDIA_TYPE}\" value"
        1 * servletResponse.setContentType(HttpHeadersReceiver.MULTIPART_MEDIA_TYPE)

        and: "The \"Content-Disposition\" header should be set to the \"inline; filename=\"${FILE_NAME}\"\" value(from file metadata)"
        1 * servletResponse.setHeader("Content-Disposition", "attachment; filename=\"${FILE_NAME}\"")

        and: "The \"Accept-Ranges\" header should be set to the \"bytes\" value"
        1 * servletResponse.setHeader("Accept-Ranges", "bytes")
        
        and: "The response status should be set to ${HttpServletResponse.SC_PARTIAL_CONTENT}"
        1 * servletResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT)
    }

    private String createContentPartsBody() {
        return new StringBuilder()
                .append("\n")
                .append("${MULTIPART_SEPARATOR}\n")
                .append("Content-Type: ${MEDIA_TYPE}\n")
                .append("Content-Range: bytes 0-13/${DISTRIBUTIONING_CONTENT_LENGTH}\n")
                .append("Hello World!!!")
                .append("\n")
                .append("${MULTIPART_SEPARATOR}\n")
                .append("Content-Type: ${MEDIA_TYPE}\n")
                .append("Content-Range: bytes 20-34/${DISTRIBUTIONING_CONTENT_LENGTH}\n")
                .append("Hello People!!!")
                .append("\n")
                .append(MULTIPART_ENDING + "\n")
                .toString()
    }
}
