package io.bcs.port.adapters.file;

import javax.servlet.http.HttpServletResponse;

import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.domain.model.file.ContentReceiver;
import io.bcs.domain.model.file.FileContent;
import io.bcs.domain.model.file.FileContent.ContentPart;
import io.bcs.domain.model.file.FileMetadata.Disposition;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpHeadersReceiver implements ContentReceiver {
    protected static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
    protected static final String MULTIPART_MEDIA_TYPE = "multipart/byteranges; boundary=" + MULTIPART_BOUNDARY;
    private final HttpServletResponse servletResponse;

    @Override
    public Promise<Void> receiveFullContent(FileContent content) {
        return Promises.of(deferred -> {
            setFileContentTypeHeader(content);
            setDispositionHeader(content);
            setAcceptRangesHeader();
            setFullContentLengthHeader(content);
            setSuccessResponseCode();
            deferred.resolve(null);
        });
    }

    @Override
    public Promise<Void> receiveContentRange(FileContent content) {
        return Promises.of(deferred -> {
            setFileContentTypeHeader(content);
            setDispositionHeader(content);
            setAcceptRangesHeader();
            setRangeContentLengthHeader(content);
            setContentRangeHeader(content);
            setPartialContentResponseCode();
            deferred.resolve(null);
        });
    }

    @Override
    public Promise<Void> receiveContentRanges(FileContent content) {
        return Promises.of(deferred -> {
            setMultipartContentTypeHeader();
            setDispositionHeader(content);
            setAcceptRangesHeader();
            setPartialContentResponseCode();
            deferred.resolve(null);
        });
    }

    private void setContentRangeHeader(FileContent content) {
        ContentPart contentPart = content.getParts().iterator().next();
        servletResponse.setHeader("Content-Range", new ContentRange(content.getFileMetadata(), contentPart).toString());
    }

    private void setRangeContentLengthHeader(FileContent content) {
        setContetLengthHeader(content.getParts().iterator().next().getContentFragment().getLength());
    }

    private void setFullContentLengthHeader(FileContent content) {
        setContetLengthHeader(content.getFileMetadata().getTotalLength());
    }

    private void setContetLengthHeader(Long length) {
        servletResponse.setHeader("Content-Length", String.valueOf(length));
    }

    private void setFileContentTypeHeader(FileContent fileContent) {
        servletResponse.setContentType(fileContent.getFileMetadata().getMediaType());
    }

    private void setMultipartContentTypeHeader() {
        servletResponse.setContentType(MULTIPART_MEDIA_TYPE);
    }

    private void setDispositionHeader(FileContent fileContent) {
        servletResponse.setHeader("Content-Disposition", createContentDispositionHeaderValue(fileContent));
    }

    private String createContentDispositionHeaderValue(FileContent fileContent) {
        String fileName = fileContent.getFileMetadata().getFileName();
        return String.format("%s; filename=\"%s\"", getDisposition(fileContent), fileName);
    }

    private String getDisposition(FileContent content) {
        Disposition disposition = content.getFileMetadata().getDefaultDisposition();
        if (disposition == Disposition.INLINE) {
            return "inline";
        } else {
            return "attachment";
        }
    }

    private void setSuccessResponseCode() {
        servletResponse.setStatus(HttpServletResponse.SC_OK);
    }

    private void setPartialContentResponseCode() {
        servletResponse.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
    }

    private void setAcceptRangesHeader() {
        servletResponse.setHeader("Accept-Ranges", "bytes");
    }
}
