package io.bcs.fileserver.infrastructure.api.internal;

import io.bce.domain.errors.ApplicationException;
import io.bce.domain.errors.UnexpectedErrorException;
import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.Stream.Stat;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.interaction.streaming.binary.OutputStreamDestination;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.DefaultContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.infrastructure.api.HttpAsyncExecutor;
import io.bcs.fileserver.infrastructure.api.HttpResponseContext;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
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

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpAsyncExecutor.of(request, response).executeAsynchronously(asyncContext -> {
      Promises.<Stat>of(deferred -> {
        HttpGetContentAccessCommand getAccessCommand = new HttpGetContentAccessCommand(request);
        Source<BinaryChunk> source = getSource(getAccessCommand);
        Destination<BinaryChunk> destination = getDestination(response);
        streamer.createStream(source, destination).start().delegate(deferred);
      }).then(statistic -> {
        HttpResponseContext.of(response).writeSuccessContext();
      }).error(ApplicationException.class, error -> {
        HttpResponseContext.of(response).writeErrorContext(error,
            HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }).finalize(() -> asyncContext.complete());
    });
  }

  private Source<BinaryChunk> getSource(HttpGetContentAccessCommand getAccessCommand) {
    return fileStorage.getAccessOnRead(getAccessCommand.getContentLocator(),
        getAccessCommand.getFragment());
  }

  private Destination<BinaryChunk> getDestination(HttpServletResponse response) {
    return new OutputStreamDestination(getRequestOutputStream(response));
  }

  private ServletOutputStream getRequestOutputStream(HttpServletResponse response) {
    try {
      return response.getOutputStream();
    } catch (IOException error) {
      throw new UnexpectedErrorException(error);
    }
  }

  @RequiredArgsConstructor
  private static class HttpGetContentAccessCommand {
    private final HttpServletRequest request;

    public ContentLocator getContentLocator() {
      return new DefaultContentLocator(getStorageFileName(), getStorageName());
    }

    public ContentFragment getFragment() {
      return new ContentFragment() {
        @Override
        public Long getOffset() {
          return HttpGetContentAccessCommand.this.getOffset();
        }

        @Override
        public Long getLength() {
          return HttpGetContentAccessCommand.this.getLength();
        }
      };
    }

    private String getStorageName() {
      return request.getParameter(STORAGE_NAME_PARAM);
    }

    private String getStorageFileName() {
      return request.getParameter(STORAGE_FILE_NAME_PARAM);
    }

    private Long getOffset() {
      return Long.parseUnsignedLong(request.getParameter(OFFSET_PARAM));
    }

    private Long getLength() {
      return Long.parseUnsignedLong(request.getParameter(LENGTH_PARAM));
    }
  }
}
