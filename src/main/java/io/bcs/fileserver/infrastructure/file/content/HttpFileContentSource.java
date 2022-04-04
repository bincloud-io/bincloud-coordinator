package io.bcs.fileserver.infrastructure.file.content;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.Stream;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.interaction.streaming.binary.InputStreamSource;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.model.content.ContentLocator;
import io.bcs.fileserver.domain.model.content.ContentSource;
import io.bcs.fileserver.domain.model.content.FileUploadStatistic;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This class implements component, performing file content data uploading to the file storage.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class HttpFileContentSource implements ContentSource {
  private final Streamer streamer;
  private final Source<BinaryChunk> source;

  /**
   * Create file content uploader.
   *
   * @param streamer   A data stremer
   * @param request    The servlet request instance
   * @param bufferSize The IO buffer size
   * @throws IOException Throws if input stream couldn't be obtained from request
   */
  public HttpFileContentSource(Streamer streamer, HttpServletRequest request, int bufferSize)
      throws IOException {
    super();
    this.source = new InputStreamSource(request.getInputStream(), bufferSize);
    this.streamer = streamer;
  }

  @Override
  public Promise<FileUploadStatistic> sendContent(ContentLocator contentLocator,
      Destination<BinaryChunk> destination) {
    return Promises.of(deferred -> {
      uploadContent(contentLocator, streamer.createStream(source, destination)).delegate(deferred);
    });
  }

  private Promise<FileUploadStatistic> uploadContent(ContentLocator locator,
      Stream<BinaryChunk> stream) {
    return stream.start().chain(stat -> {
      return Promises.resolvedBy(new UploadStatistic(locator, stat.getSize()));
    });
  }

  @Getter
  @EqualsAndHashCode
  @RequiredArgsConstructor
  private static class UploadStatistic implements FileUploadStatistic {
    private final ContentLocator locator;
    private final Long totalLength;
  }
}
