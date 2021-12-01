package io.bcs.port.adapters.file;

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

import io.bce.domain.errors.UnexpectedErrorException;
import io.bce.interaction.streaming.Streamer;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.application.FileService;
import io.bcs.domain.model.file.ContentReceiver;
import io.bcs.domain.model.file.ContentUploader;
import io.bcs.port.adapters.ContentLoadingProperties;

public class ContentLoadingServlet extends HttpServlet {
    private static final long serialVersionUID = 2026798739467262029L;
    private static final String HTTP_RANGES_HEADER = "Ranges";
    private static final String FILE_STORAGE_NAME_PARAMETER = "fileStorageName";

    @Inject
    private Streamer streamer;
    
    @Inject
    private FileService fileService;
    
    @Inject
    private ContentLoadingProperties contentLoadingProperties;

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        executeAsynchronously(request, asyncContext -> {
            downloadContent(request, () -> createHeadersOnlyContentReceiver(response))
                    .finalize(() -> asyncContext.complete());
        });
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        executeAsynchronously(request, asyncContext -> {
            downloadContent(request, () -> createFileDataContentReceiver(response))
                    .finalize(() -> asyncContext.complete());
        });
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        executeAsynchronously(request, asyncContext -> {
            uploadContent(request, response).finalize(() -> asyncContext.complete());
        });
    }

    private Promise<Void> uploadContent(HttpServletRequest request, HttpServletResponse response) {
        return Promises.of(deferred -> {
            ContentUploader contentUploader = createFileContentUploader(request);
            fileService.upload(contentUploader).execute(getStorageFileNameParam(request)).chain(statistic -> {
                response.setContentLengthLong(statistic.getContentLength());
                return Promises.<Void>resolvedBy(null);
            }).delegate(deferred);
        });
    }

    private Promise<Void> downloadContent(HttpServletRequest request, Supplier<ContentReceiver> receiverProvider) {
        return Promises.of(deferred -> {
            ContentReceiver receiver = receiverProvider.get();
            fileService.download(receiver).execute(new HttpServletDownloadCommand(request)).delegate(deferred);
        });
    }

    private <T> void executeAsynchronously(HttpServletRequest servletRequest, Consumer<AsyncContext> methodExecutor) {
        methodExecutor.accept(servletRequest.getAsyncContext());
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

    private ContentUploader createFileContentUploader(HttpServletRequest request) {
        try {
            return new HttpFileContentUploader(streamer, request, contentLoadingProperties.getBufferSize());
        } catch (IOException error) {
            throw new UnexpectedErrorException(error);
        }
    }

    private static Optional<String> getStorageFileNameParam(HttpServletRequest request) {
        return Optional.ofNullable(request.getParameter(FILE_STORAGE_NAME_PARAMETER));
    }

    private static HttpRanges getHttpRanges(HttpServletRequest request) {
        return new HttpRanges(Optional.ofNullable(request.getHeader(HTTP_RANGES_HEADER)));
    }

    private static class HttpServletDownloadCommand extends HttpDownloadCommand {
        public HttpServletDownloadCommand(HttpServletRequest request) {
            super(getStorageFileNameParam(request), getHttpRanges(request));
        }
    }
}
