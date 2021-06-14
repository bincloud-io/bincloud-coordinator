package io.bincloud.resources.port.adapter.endpoint.upload;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.bincloud.common.domain.model.error.ApplicationException;
import io.bincloud.common.domain.model.error.AsyncErrorsHandler;
import io.bincloud.common.domain.model.error.AsyncErrorsHandler.ErrorInterceptor;
import io.bincloud.common.domain.model.error.UnexpectedSystemBehaviorException;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.port.adapters.io.transfer.sources.StreamSource;
import io.bincloud.common.port.adapters.web.AsyncServletOperationExecutor;
import io.bincloud.resources.domain.model.Constants;
import io.bincloud.resources.domain.model.contracts.FileUploader;
import io.bincloud.resources.domain.model.contracts.FileUploader.UploadingCallback;
import io.bincloud.resources.domain.model.errors.ResourceDoesNotExistException;
import io.bincloud.resources.domain.model.file.FileUploadId;
import io.bincloud.resources.port.adapter.ServerContextProvider;
import io.bincloud.resources.port.adapter.endpoint.ServletErrorResponse;

public class ResourceUploadingServlet extends HttpServlet {
	private static final long serialVersionUID = -8092602564530445635L;
	private static final String RESOURCE_ID_PARAMETER_NAME = "resourceId";

	@Inject
	private FileUploader fileUploader;

	@Inject
	private MessageProcessor messageProcessor;

	@Inject
	private ServerContextProvider serverContext;

	private AsyncErrorsHandler<AsyncContext> errorsHandler;

	public ResourceUploadingServlet() {
		super();
		this.errorsHandler = createServletErrorHandler();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		AsyncServletOperationExecutor asyncOperationExecutor = new AsyncServletOperationExecutor(request, response);
		asyncOperationExecutor.execute(context -> {
			UploadingCallback uploadingCallback = createUploadingCallback(context, errorsHandler);
			try {
				SourcePoint sourcePoint = new StreamSource(context.getRequest().getInputStream(),
						serverContext.getIOBufferSize());
				fileUploader.uploadFile(getResourceId(context), sourcePoint, uploadingCallback);
			} catch (IOException error) {
				uploadingCallback.onError(error);
			}
		});
	}

	private AsyncErrorsHandler<AsyncContext> createServletErrorHandler() {
		return AsyncErrorsHandler.createFor(AsyncContext.class)
				.registerHandler(ResourceDoesNotExistException.class, errorHandler(HttpServletResponse.SC_BAD_REQUEST))
				.registerHandler(ApplicationException.class, errorHandler(HttpServletResponse.SC_INTERNAL_SERVER_ERROR))
				.registerDefaultHandler(defaultErrorHandler());
	}

	
	private <E extends ApplicationException> ErrorInterceptor<AsyncContext, E> errorHandler(int errorCode) {
		return (context, error) -> {
			sendApplicationError(context, errorCode, error);
		};
	}

	private ErrorInterceptor<AsyncContext, Exception> defaultErrorHandler() {
		final ErrorInterceptor<AsyncContext, ApplicationException> defaultHandler = errorHandler(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return (context, error) -> {
			defaultHandler.handleError(context, new UnexpectedSystemBehaviorException(Constants.CONTEXT, error));
		};
	}

	private UploadingCallback createUploadingCallback(AsyncContext asyncContext,
			AsyncErrorsHandler<AsyncContext> errorsHandler) {
		return new UploadingCallback() {
			@Override
			public void onUpload(FileUploadId uploaded) {
				sendResponse(asyncContext, HttpServletResponse.SC_OK,
						new ResourceUploadingSuccessResponse(serverContext.getRootURL(), uploaded));
				asyncContext.complete();
			}

			@Override
			public void onError(Exception error) {
				errorsHandler.handleError(asyncContext, error);
				asyncContext.complete();
			}
		};
	}

	private void sendApplicationError(AsyncContext context, int code, ApplicationException applicationError) {
		sendResponse(context, code, new ServletErrorResponse(applicationError, messageProcessor));
	}

	private void sendResponse(AsyncContext context, int code, Properties properties) {
		try {
			HttpServletResponse response = (HttpServletResponse) context.getResponse();
			properties.store(response.getWriter(), null);
			response.setStatus(code);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Optional<Long> getResourceId(AsyncContext asyncContext) {
		try {
			return Optional.ofNullable(asyncContext.getRequest().getParameter(RESOURCE_ID_PARAMETER_NAME))
					.map(value -> Long.valueOf(value));
		} catch (NumberFormatException error) {
			return Optional.empty();
		}
	}
}
