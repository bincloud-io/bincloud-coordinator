package io.bincloud.resources.port.adapter.endpoint.files.upload;

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
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.port.adapters.io.transfer.sources.StreamSource;
import io.bincloud.common.port.adapters.web.AsyncServletOperationExecutor;
import io.bincloud.common.port.adapters.web.HttpServletAppliactionExceptionHandler;
import io.bincloud.resources.domain.model.Constants;
import io.bincloud.resources.domain.model.contracts.upload.FileUploader;
import io.bincloud.resources.domain.model.contracts.upload.FileUploader.UploadingCallback;
import io.bincloud.resources.domain.model.errors.ResourceDoesNotExistException;
import io.bincloud.resources.domain.model.errors.UnspecifiedResourceException;
import io.bincloud.resources.domain.model.file.FileUploadId;
import io.bincloud.resources.port.adapter.ServerContextProvider;

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
			UploadingCallback uploadingCallback = new HttpFileUploadCallback(context);
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
		private AsyncErrorsHandler<AsyncContext> errorHandler;

		public HttpFileUploadCallback(AsyncContext asyncContext) {
			super();
			this.asyncContext = asyncContext;
			this.errorHandler = createServletErrorHandler();
		}

		@Override
		public void onUpload(FileUploadId uploaded) {
			sendResponse(asyncContext, HttpServletResponse.SC_OK,
					new HttpFileUploadSuccessResponseProperties(serverContext.getRootURL(), uploaded));
		}

		@Override
		public void onError(Exception error) {
			errorHandler.handleError(asyncContext, error);
		}

		private AsyncErrorsHandler<AsyncContext> createServletErrorHandler() {
			return AsyncErrorsHandler.createFor(AsyncContext.class)
					.registerHandler(UnspecifiedResourceException.class,
							createErrorHandler(HttpServletResponse.SC_BAD_REQUEST))
					.registerHandler(ResourceDoesNotExistException.class,
							createErrorHandler(HttpServletResponse.SC_BAD_REQUEST))
					.registerHandler(ApplicationException.class,
							createErrorHandler(HttpServletResponse.SC_INTERNAL_SERVER_ERROR))
					.registerDefaultHandler(createDefaultErrorHandler(Constants.CONTEXT));
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
	
	private ErrorInterceptor<AsyncContext, Exception> createDefaultErrorHandler(String context) {
		return HttpServletAppliactionExceptionHandler.defaultErrorHandler(messageProcessor, context);
	}
	
	private <E extends ApplicationException> ErrorInterceptor<AsyncContext, E> createErrorHandler(int httpCode) {
		return HttpServletAppliactionExceptionHandler.applicationErrorHandler(messageProcessor, httpCode);
	}
	
	private void sendResponse(AsyncContext asyncContext, int code, Properties properties) {
		try {
			HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
			properties.store(response.getWriter(), null);
			response.setStatus(code);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			asyncContext.complete();
		}
	}
}
