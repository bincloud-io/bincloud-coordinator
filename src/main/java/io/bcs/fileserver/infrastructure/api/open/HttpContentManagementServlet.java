package io.bcs.fileserver.infrastructure.api.open;

import io.bce.domain.errors.ApplicationException;
import io.bce.domain.errors.UnexpectedErrorException;
import io.bce.interaction.streaming.Streamer;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise.ErrorHandler;
import io.bce.promises.Promise.ResponseHandler;
import io.bce.promises.Promises;
import io.bcs.fileserver.domain.errors.ContentNotUploadedException;
import io.bcs.fileserver.domain.errors.ContentUploadedException;
import io.bcs.fileserver.domain.errors.FileDisposedException;
import io.bcs.fileserver.domain.errors.FileNotExistsException;
import io.bcs.fileserver.domain.errors.FileNotSpecifiedException;
import io.bcs.fileserver.domain.errors.UnsatisfiableRangeFormatException;
import io.bcs.fileserver.domain.model.file.content.download.ContentReceiver;
import io.bcs.fileserver.domain.model.file.content.upload.ContentSource;
import io.bcs.fileserver.domain.model.file.content.upload.FileUploadStatistic;
import io.bcs.fileserver.domain.services.ContentService;
import io.bcs.fileserver.domain.services.ContentService.UploadCommand;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import io.bcs.fileserver.infrastructure.api.HttpAsyncExecutor;
import io.bcs.fileserver.infrastructure.api.HttpResponseContext;
import io.bcs.fileserver.infrastructure.file.HttpRanges;
import io.bcs.fileserver.infrastructure.file.content.HttpDownloadCommand;
import io.bcs.fileserver.infrastructure.file.content.HttpFileContentSource;
import io.bcs.fileserver.infrastructure.file.content.HttpFileDataReceiver;
import io.bcs.fileserver.infrastructure.file.content.HttpHeadersReceiver;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the content management HTTP servlet.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class HttpContentManagementServlet extends HttpServlet {
  private static final ApplicationLogger log =
      Loggers.applicationLogger(HttpContentManagementServlet.class);

  private static final long serialVersionUID = 2026798739467262029L;
  private static final String HTTP_RANGES_HEADER = "Ranges";
  private static final String FILE_STORAGE_NAME_PARAMETER = "fileStorageName";
  private static final String UPLOADED_SIZE_HEADER = "X-BC-UPLOADED-SIZE";

  @Inject
  private Streamer streamer;

  @Inject
  @SuppressWarnings("cdi-ambiguous-dependency")
  private ContentService contentService;

  @Inject
  private FileServerConfigurationProperties contentLoadingProperties;

  @Override
  protected void doHead(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpAsyncExecutor.of(request, response).executeAsynchronously(asyncContext -> {
      downloadContent(asyncContext, request, response,
          () -> createHeadersOnlyContentReceiver(response));
    });

  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpAsyncExecutor.of(request, response).executeAsynchronously(asyncContext -> {
      downloadContent(asyncContext, request, response,
          () -> createFileDataContentReceiver(response));
    });
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    HttpAsyncExecutor.of(request, response).executeAsynchronously(asyncContext -> {
      uploadContent(asyncContext, request, response);
    });
  }

  private void uploadContent(AsyncContext asyncContext, HttpServletRequest request,
      HttpServletResponse response) {
    Promises.<FileUploadStatistic>of(deferred -> {
      contentService.upload(new HttpUploadCommand(request), createSender(request))
          .delegate(deferred);
    }).then(uploadSuccessHandler(response))
        .error(FileNotSpecifiedException.class,
            applicationError(response, HttpServletResponse.SC_BAD_REQUEST))
        .error(FileNotExistsException.class,
            applicationError(response, HttpServletResponse.SC_NOT_FOUND))
        .error(ContentUploadedException.class,
            applicationError(response, HttpServletResponse.SC_CONFLICT))
        .error(FileDisposedException.class,
            applicationError(response, HttpServletResponse.SC_NOT_FOUND))
        .error(unrecognizedErrorHandler(response)).finalize(() -> asyncContext.complete());
  }

  private void downloadContent(AsyncContext asyncContext, HttpServletRequest request,
      HttpServletResponse response, Supplier<ContentReceiver> receiverProvider) {
    Promises.<Void>of(deferred -> {
      contentService.download(new HttpServletDownloadCommand(request), receiverProvider.get())
          .delegate(deferred);
    }).error(FileNotSpecifiedException.class,
        applicationError(response, HttpServletResponse.SC_BAD_REQUEST))
        .error(FileNotExistsException.class,
            applicationError(response, HttpServletResponse.SC_NOT_FOUND))
        .error(UnsatisfiableRangeFormatException.class,
            applicationError(response, HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE))
        .error(ContentNotUploadedException.class,
            applicationError(response, HttpServletResponse.SC_NOT_FOUND))
        .error(FileDisposedException.class,
            applicationError(response, HttpServletResponse.SC_NOT_FOUND))
        .error(unrecognizedErrorHandler(response)).finalize(() -> asyncContext.complete());
  }

  private ResponseHandler<FileUploadStatistic> uploadSuccessHandler(HttpServletResponse response) {
    return result -> {
      response.setHeader(UPLOADED_SIZE_HEADER, result.getTotalLength().toString());
      HttpResponseContext.of(response).writeSuccessContext();
    };
  }

  private ErrorHandler<Throwable> unrecognizedErrorHandler(HttpServletResponse response) {
    ErrorHandler<UnexpectedErrorException> unexpectedErrorHandler =
        applicationError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    return error -> unexpectedErrorHandler.onError(new UnexpectedErrorException(error));
  }

  private <E extends ApplicationException> ErrorHandler<E> applicationError(
      HttpServletResponse response, int statusCode) {
    return error -> {
      log.error(error);
      HttpResponseContext.of(response).writeErrorContext(error, statusCode);
    };
  }

  private ContentReceiver createHeadersOnlyContentReceiver(HttpServletResponse response) {
    return new HttpHeadersReceiver(response);
  }

  private ContentReceiver createFileDataContentReceiver(HttpServletResponse response) {
    try {
      return new HttpFileDataReceiver(streamer, response);
    } catch (IOException error) {
      throw new UnexpectedErrorException(error);
    }
  }

  private ContentSource createSender(HttpServletRequest request) {
    try {
      return new HttpFileContentSource(streamer, request, contentLoadingProperties.getBufferSize());
    } catch (IOException error) {
      throw new UnexpectedErrorException(error);
    }
  }

  private static Optional<String> getStorageFileNameParam(HttpServletRequest request) {
    return normalizeParameterValue(
        Optional.ofNullable(request.getParameter(FILE_STORAGE_NAME_PARAMETER)));
  }

  private static HttpRanges getHttpRanges(HttpServletRequest request) {
    return new HttpRanges(
        normalizeParameterValue(Optional.ofNullable(request.getHeader(HTTP_RANGES_HEADER))));
  }

  private static Optional<String> normalizeParameterValue(Optional<String> notNormalizedValue) {
    return notNormalizedValue.map(String::trim).filter(value -> !value.isEmpty());
  }

  private static class HttpServletDownloadCommand extends HttpDownloadCommand {
    public HttpServletDownloadCommand(HttpServletRequest request) {
      super(getStorageFileNameParam(request), getHttpRanges(request));
    }
  }

  @RequiredArgsConstructor
  private static class HttpUploadCommand implements UploadCommand {
    private final HttpServletRequest request;

    @Override
    public Optional<String> getStorageFileName() {
      return getStorageFileNameParam(request);
    }

    @Override
    public Long getContentLength() {
      return request.getContentLengthLong();
    }
  }
}
