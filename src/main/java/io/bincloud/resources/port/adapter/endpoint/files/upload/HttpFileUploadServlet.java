package io.bincloud.resources.port.adapter.endpoint.files.upload;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.bincloud.common.domain.model.error.ApplicationException;
import io.bincloud.common.domain.model.error.AsyncErrorsHandler;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.port.adapters.io.transfer.sources.StreamSource;
import io.bincloud.common.port.adapters.web.AsyncServletOperationExecutor;
import io.bincloud.resources.domain.model.Constants;
import io.bincloud.resources.domain.model.contracts.upload.FileUploader;
import io.bincloud.resources.domain.model.contracts.upload.FileUploader.UploadingCallback;
import io.bincloud.resources.domain.model.errors.ResourceDoesNotExistException;
import io.bincloud.resources.domain.model.errors.UnspecifiedResourceException;
import io.bincloud.resources.domain.model.file.FileUploadId;
import io.bincloud.resources.port.adapter.ServerContextProvider;
import io.bincloud.resources.port.adapter.endpoint.files.ServletResponseHandler;

public class HttpFileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = -8092602564530445635L;
	private static final String RESOURCE_ID_PARAMETER_NAME = "resourceId";

	@Inject
	private FileUploader fileUploader;

	@Inject
	private MessageProcessor messageProcessor;

	@Inject
	private ServerContextProvider serverContext;

	public HttpFileUploadServlet() {
		super();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		AsyncServletOperationExecutor asyncOperationExecutor = new AsyncServletOperationExecutor(request, response);
		asyncOperationExecutor.execute(context -> {
			UploadingCallback uploadingCallback = new HttpFileUploadCallback(context, new ServletResponseHandler(messageProcessor));
			try {
				SourcePoint sourcePoint = new StreamSource(context.getRequest().getInputStream(),
						serverContext.getIOBufferSize());
				fileUploader.uploadFile(getResourceId(context), sourcePoint, uploadingCallback);
			} catch (IOException error) {
				uploadingCallback.onError(error);
			}
		});
	}

	private class HttpFileUploadCallback implements UploadingCallback {
		private AsyncContext asyncContext;
		private ServletResponseHandler responseHandler;
		private AsyncErrorsHandler<AsyncContext> errorHandler;

		public HttpFileUploadCallback(AsyncContext asyncContext, ServletResponseHandler responseHandler) {
			super();
			this.asyncContext = asyncContext;
			this.responseHandler = responseHandler;
			this.errorHandler = createServletErrorHandler(responseHandler);
		}

		@Override
		public void onUpload(FileUploadId uploaded) {
			responseHandler.sendResponse(asyncContext, HttpServletResponse.SC_OK,
					new HttpFileUploadSuccessResponseProperties(serverContext.getRootURL(), uploaded));
		}

		@Override
		public void onError(Exception error) {
			errorHandler.handleError(asyncContext, error);
		}

		private AsyncErrorsHandler<AsyncContext> createServletErrorHandler(ServletResponseHandler responseHandler) {
			return AsyncErrorsHandler.createFor(AsyncContext.class)
					.registerHandler(UnspecifiedResourceException.class,
							responseHandler.createErrorHandler(HttpServletResponse.SC_BAD_REQUEST))
					.registerHandler(ResourceDoesNotExistException.class,
							responseHandler.createErrorHandler(HttpServletResponse.SC_BAD_REQUEST))
					.registerHandler(ApplicationException.class,
							responseHandler.createErrorHandler(HttpServletResponse.SC_INTERNAL_SERVER_ERROR))
					.registerDefaultHandler(responseHandler.createDefaultErrorHandler(Constants.CONTEXT));
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
