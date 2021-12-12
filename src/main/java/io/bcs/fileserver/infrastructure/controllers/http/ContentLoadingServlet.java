package io.bcs.fileserver.infrastructure.controllers.http;

import io.bce.domain.errors.ApplicationException;
import io.bce.domain.errors.ErrorDescriptor.ErrorCode;
import io.bce.domain.errors.UnexpectedErrorException;
import io.bce.interaction.streaming.Streamer;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise.ErrorHandler;
import io.bce.promises.Promise.ResponseHandler;
import io.bce.promises.Promises;
import io.bcs.fileserver.Constants;
import io.bcs.fileserver.domain.errors.ContentNotUploadedException;
import io.bcs.fileserver.domain.errors.ContentUploadedException;
import io.bcs.fileserver.domain.errors.FileDisposedException;
import io.bcs.fileserver.domain.errors.FileNotExistsException;
import io.bcs.fileserver.domain.errors.FileNotSpecifiedException;
import io.bcs.fileserver.domain.errors.UnsatisfiableRangeFormatException;
import io.bcs.fileserver.domain.model.file.content.ContentManagement;
import io.bcs.fileserver.domain.model.file.content.ContentReceiver;
import io.bcs.fileserver.domain.model.file.content.ContentUploader;
import io.bcs.fileserver.domain.model.file.lifecycle.Lifecycle.FileUploadStatistic;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import io.bcs.fileserver.infrastructure.file.HttpRanges;
import io.bcs.fileserver.infrastructure.file.content.FileMetadataProvider;
import io.bcs.fileserver.infrastructure.file.content.HttpDownloadCommand;
import io.bcs.fileserver.infrastructure.file.content.HttpFileContentUploader;
import io.bcs.fileserver.infrastructure.file.content.HttpFileDataReceiver;
import io.bcs.fileserver.infrastructure.file.content.HttpHeadersReceiver;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class implements the content management HTTP servlet.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class ContentLoadingServlet extends HttpServlet {
  private static final ApplicationLogger log =
      Loggers.applicationLogger(ContentLoadingServlet.class);

  private static final long serialVersionUID = 2026798739467262029L;
  private static final String HTTP_RANGES_HEADER = "Ranges";
  private static final String FILE_STORAGE_NAME_PARAMETER = "fileStorageName";
  private static final String BOUNDED_CONTEXT_HEADER = "X-BC-CONTEXT";
  private static final String ERROR_CODE_HEADER = "X-BC-ERR-CODE";
  private static final String ERROR_SEVERITY_HEADER = "X-BC-ERR-SEVERITY";
  private static final String UPLOADED_SIZE_HEADER = "X-BC-UPLOADED-SIZE";

  @Inject
  private Streamer streamer;

  @Inject
  private ContentManagement contentService;

  @Inject
  private FileServerConfigurationProperties contentLoadingProperties;

  @Inject
  private FileMetadataProvider metadataProvider;

  @Override
  protected void doHead(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    executeAsynchronously(request, response, asyncContext -> {
      downloadContent(asyncContext, request, response,
          () -> createHeadersOnlyContentReceiver(response));
    });
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    executeAsynchronously(request, response, asyncContext -> {
      downloadContent(asyncContext, request, response,
          () -> createFileDataContentReceiver(response));
    });
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    executeAsynchronously(request, response, asyncContext -> {
      uploadContent(asyncContext, request, response);
    });
  }

  private void uploadContent(AsyncContext asyncContext, HttpServletRequest request,
      HttpServletResponse response) {
    Promises.<FileUploadStatistic>of(deferred -> {
      ContentUploader contentUploader = createFileContentUploader(request);
      contentService.upload(getStorageFileNameParam(request), contentUploader).delegate(deferred);
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
      ContentReceiver receiver = receiverProvider.get();
      contentService.download(new HttpServletDownloadCommand(request), receiver).delegate(deferred);
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
      response.setHeader(BOUNDED_CONTEXT_HEADER, Constants.CONTEXT.toString());
      response.setHeader(ERROR_CODE_HEADER,
          ErrorCode.SUCCESSFUL_COMPLETED_CODE.extract().toString());
      response.setHeader(UPLOADED_SIZE_HEADER, result.getTotalLength().toString());
      response.setStatus(HttpServletResponse.SC_OK);
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
      response.setHeader(BOUNDED_CONTEXT_HEADER, error.getContextId().toString());
      response.setHeader(ERROR_CODE_HEADER, error.getErrorCode().extract().toString());
      response.setHeader(ERROR_SEVERITY_HEADER, error.getErrorSeverity().toString());
      response.setStatus(statusCode);
    };
  }

  private <T> void executeAsynchronously(HttpServletRequest servletRequest,
      HttpServletResponse response, Consumer<AsyncContext> methodExecutor) {
    methodExecutor.accept(servletRequest.startAsync(servletRequest, response));
  }

  private ContentReceiver createHeadersOnlyContentReceiver(HttpServletResponse response) {
    return new HttpHeadersReceiver(response, metadataProvider);
  }

  private ContentReceiver createFileDataContentReceiver(HttpServletResponse response) {
    try {
      return new HttpFileDataReceiver(streamer, response, metadataProvider);
    } catch (IOException error) {
      throw new UnexpectedErrorException(error);
    }
  }

  private ContentUploader createFileContentUploader(HttpServletRequest request) {
    try {
      return new HttpFileContentUploader(streamer, request,
          contentLoadingProperties.getBufferSize());
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
}
