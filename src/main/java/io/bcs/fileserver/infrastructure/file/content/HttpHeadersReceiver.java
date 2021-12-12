package io.bcs.fileserver.infrastructure.file.content;

import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.model.file.content.ContentReceiver;
import io.bcs.fileserver.domain.model.file.content.FileContent;
import io.bcs.fileserver.domain.model.file.content.FileContent.ContentPart;
import io.bcs.fileserver.domain.model.file.metadata.FileMetadata;
import io.bcs.fileserver.domain.model.file.metadata.FileMetadata.Disposition;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * This class implements content receiver, which sets http headers only.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class HttpHeadersReceiver implements ContentReceiver {
  protected static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";
  protected static final String MULTIPART_MEDIA_TYPE =
      "multipart/byteranges; boundary=" + MULTIPART_BOUNDARY;
  private final HttpServletResponse servletResponse;
  private final FileMetadataProvider metadataProvider;

  @Override
  public Promise<Void> receiveFullContent(FileContent content) {
    FileMetadata fileMetadata = getMetadata(content);
    return Promises.of(deferred -> {
      setFileContentTypeHeader(fileMetadata);
      setDispositionHeader(content, fileMetadata);
      setAcceptRangesHeader();
      setFullContentLengthHeader(fileMetadata);
      setSuccessResponseCode();
      deferred.resolve(null);
    });
  }

  @Override
  public Promise<Void> receiveContentRange(FileContent content) {
    FileMetadata fileMetadata = getMetadata(content);
    return Promises.of(deferred -> {
      setFileContentTypeHeader(fileMetadata);
      setDispositionHeader(content, fileMetadata);
      setAcceptRangesHeader();
      setRangeContentLengthHeader(content);
      setContentRangeHeader(content, fileMetadata);
      setPartialContentResponseCode();
      deferred.resolve(null);
    });
  }

  @Override
  public Promise<Void> receiveContentRanges(FileContent content) {
    FileMetadata fileMetadata = getMetadata(content);
    return Promises.of(deferred -> {
      setMultipartContentTypeHeader();
      setDispositionHeader(content, fileMetadata);
      setAcceptRangesHeader();
      setPartialContentResponseCode();
      deferred.resolve(null);
    });
  }

  private FileMetadata getMetadata(FileContent fileContent) {
    return metadataProvider.getMetadataFor(fileContent.getLocator());
  }

  private void setContentRangeHeader(FileContent content, FileMetadata fileMetadata) {
    ContentPart contentPart = content.getParts().iterator().next();
    servletResponse.setHeader("Content-Range",
        new ContentRange(fileMetadata, contentPart).toString());
  }

  private void setRangeContentLengthHeader(FileContent content) {
    setContetLengthHeader(content.getParts().iterator().next().getContentFragment().getLength());
  }

  private void setFullContentLengthHeader(FileMetadata fileMetadata) {
    setContetLengthHeader(fileMetadata.getTotalLength());
  }

  private void setContetLengthHeader(Long length) {
    servletResponse.setHeader("Content-Length", String.valueOf(length));
  }

  private void setFileContentTypeHeader(FileMetadata fileMetadata) {
    servletResponse.setContentType(fileMetadata.getMediaType());
  }

  private void setMultipartContentTypeHeader() {
    servletResponse.setContentType(MULTIPART_MEDIA_TYPE);
  }

  private void setDispositionHeader(FileContent fileContent, FileMetadata fileMetadata) {
    servletResponse.setHeader("Content-Disposition",
        createContentDispositionHeaderValue(fileMetadata));
  }

  private String createContentDispositionHeaderValue(FileMetadata fileMetadata) {
    String fileName = fileMetadata.getFileName();
    return String.format("%s; filename=\"%s\"", getDisposition(fileMetadata), fileName);
  }

  private String getDisposition(FileMetadata fileMetadata) {
    Disposition disposition = fileMetadata.getContentDisposition();
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
