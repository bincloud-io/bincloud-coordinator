package io.bcs.fileserver.infrastructure.api.internal;

import io.bce.domain.errors.ApplicationException;
import io.bce.domain.errors.UnexpectedErrorException;
import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.Stream.Stat;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.interaction.streaming.binary.InputStreamSource;
import io.bce.interaction.streaming.binary.OutputStreamDestination;
import io.bce.promises.Deferred.DeferredFunction;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import io.bcs.fileserver.infrastructure.api.HttpAsyncExecutor;
import io.bcs.fileserver.infrastructure.api.HttpResponseContext;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * This servlet provides direct access to the storage.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class HttpRemoteContentAccessServlet extends HttpServlet {
  private static final long serialVersionUID = -9219714383304325501L;
  private static final String STORAGE_NAME_PARAM = "storageName";
  private static final String STORAGE_FILE_NAME_PARAM = "storageFileName";
  private static final String OFFSET_PARAM = "offset";
  private static final String LENGTH_PARAM = "length";

  @Inject
  private Streamer streamer;

  @Inject
  private FileStorage fileStorage;

  private FileServerConfigurationProperties fileServerConfigurationProperties;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpAsyncExecutor.of(request, response).executeAsynchronously(asyncContext -> {
      transferContent(asyncContext, deferred -> {
        HttpReadContentCommand getAccessCommand = new HttpReadContentCommand(request);
        Source<BinaryChunk> source = getReadSource(getAccessCommand);
        Destination<BinaryChunk> destination = getReadDestination(response);
        streamer.createStream(source, destination).start().delegate(deferred);
      });
    });
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpAsyncExecutor.of(request, response).executeAsynchronously(asyncContext -> {
      transferContent(asyncContext, deferred -> {
        HttpWriteContentCommand writeCommand = new HttpWriteContentCommand(request);
        Source<BinaryChunk> source = getWriteSource(request);
        Destination<BinaryChunk> destination = getWriteDestination(writeCommand);
        streamer.createStream(source, destination).start().delegate(deferred);
      });
    });
  }

  private void transferContent(AsyncContext context, DeferredFunction<Stat> deferred) {
    HttpServletResponse response = (HttpServletResponse) context.getResponse();
    Promises.<Stat>of(deferred).then(statistic -> {
      HttpResponseContext.of(response).writeSuccessContext();
    }).error(ApplicationException.class, error -> {
      HttpResponseContext.of(response).writeErrorContext(error,
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }).finalize(() -> context.complete());
  }

  private Source<BinaryChunk> getReadSource(HttpReadContentCommand readCommand) {
    return fileStorage.getAccessOnRead(readCommand.getContentLocator(), readCommand.getFragment());
  }

  private Destination<BinaryChunk> getReadDestination(HttpServletResponse response) {
    return new OutputStreamDestination(getResponseOutputStream(response));
  }

  private Source<BinaryChunk> getWriteSource(HttpServletRequest request) {
    return new InputStreamSource(getRequestInputStream(request),
        fileServerConfigurationProperties.getBufferSize());
  }

  private Destination<BinaryChunk> getWriteDestination(HttpWriteContentCommand writeCommand) {
    return fileStorage.getAccessOnWrite(writeCommand.getContextLocator());
  }

  private ServletInputStream getRequestInputStream(HttpServletRequest request) {
    try {
      return request.getInputStream();
    } catch (Exception error) {
      throw new UnexpectedErrorException(error);
    }
  }

  private ServletOutputStream getResponseOutputStream(HttpServletResponse response) {
    try {
      return response.getOutputStream();
    } catch (IOException error) {
      throw new UnexpectedErrorException(error);
    }
  }

  @RequiredArgsConstructor
  private static class HttpReadContentCommand {
    private final HttpServletRequest request;

    public ContentLocator getContentLocator() {
      return new RequestContextLocator(request);
    }

    public ContentFragment getFragment() {
      return new ContentFragment() {
        @Override
        public Long getOffset() {
          return HttpReadContentCommand.this.getOffset();
        }

        @Override
        public Long getLength() {
          return HttpReadContentCommand.this.getLength();
        }
      };
    }

    private Long getOffset() {
      return Long.parseUnsignedLong(request.getParameter(OFFSET_PARAM));
    }

    private Long getLength() {
      return Long.parseUnsignedLong(request.getParameter(LENGTH_PARAM));
    }
  }

  @RequiredArgsConstructor
  private static class HttpWriteContentCommand {
    private final HttpServletRequest request;
    
    public ContentLocator getContextLocator() {
      return new RequestContextLocator(request);
    }
  }

  @RequiredArgsConstructor
  private static class RequestContextLocator implements ContentLocator {
    private final HttpServletRequest request;

    @Override
    public String getStorageName() {
      return request.getParameter(STORAGE_NAME_PARAM);
    }

    @Override
    public String getStorageFileName() {
      return request.getParameter(STORAGE_FILE_NAME_PARAM);
    }
  }
}
