package io.bincloud.files.port.adapter.file.web.upload;

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
import io.bincloud.common.domain.model.error.AsyncErrorsHandler.ErrorInterceptor;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.domain.model.web.KeyValueParameters;
import io.bincloud.common.port.adapters.io.transfer.sources.StreamSource;
import io.bincloud.common.port.adapters.web.AsyncServletOperationExecutor;
import io.bincloud.common.port.adapters.web.HttpServletAppliactionExceptionHandler;
import io.bincloud.common.port.adapters.web.HttpServletRequestParameters;
import io.bincloud.files.domain.model.contracts.FilePointer;
import io.bincloud.files.domain.model.contracts.FileDescriptor;
import io.bincloud.files.domain.model.contracts.upload.FileUploadListener;
import io.bincloud.files.domain.model.contracts.upload.FileUploader;
import io.bincloud.resources.domain.model.Constants;
import io.bincloud.resources.domain.model.resource.ResourceDoesNotExistException;
import io.bincloud.resources.domain.model.resource.UnspecifiedResourceException;
import io.bincloud.resources.port.adapter.ServerContextProvider;

public class HttpFileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = -8092602564530445635L;
	private static final String FILE_ID_PARAMETER_NAME = "fileId";

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
			FileUploadListener uploadingCallback = new HttpFileUploadCallback(context);
			try {
				SourcePoint sourcePoint = new StreamSource(context.getRequest().getInputStream(),
						serverContext.getIOBufferSize());
				fileUploader.uploadFileContent(getRevisionPointer(context), getContentSize(context), sourcePoint,
						uploadingCallback);
			} catch (IOException error) {
				uploadingCallback.onError(error);
			}
		});
	}

	private class HttpFileUploadCallback implements FileUploadListener {
		private AsyncContext asyncContext;
		private AsyncErrorsHandler<AsyncContext> errorHandler;

		public HttpFileUploadCallback(AsyncContext asyncContext) {
			super();
			this.asyncContext = asyncContext;
			this.errorHandler = createServletErrorHandler();
		}

		@Override
		public void onUpload(FileDescriptor uploaded) {
			HttpServletResponse response = (HttpServletResponse) asyncContext.getResponse();
			response.setStatus(HttpServletResponse.SC_OK);
			asyncContext.complete();
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

	private FilePointer getRevisionPointer(AsyncContext asyncContext) {
		KeyValueParameters parameters = new HttpServletRequestParameters(
				(HttpServletRequest) asyncContext.getRequest());
		return new FilePointer() {
			@Override
			public Optional<String> getFilesystemName() {
				return parameters.getStringValue(FILE_ID_PARAMETER_NAME);
			}
		};
	}

	private Long getContentSize(AsyncContext asyncContext) {
		return asyncContext.getRequest().getContentLengthLong();
	}

	private ErrorInterceptor<AsyncContext, Exception> createDefaultErrorHandler(String context) {
		return HttpServletAppliactionExceptionHandler.defaultErrorHandler(messageProcessor, context);
	}

	private <E extends ApplicationException> ErrorInterceptor<AsyncContext, E> createErrorHandler(int httpCode) {
		return HttpServletAppliactionExceptionHandler.applicationErrorHandler(messageProcessor, httpCode);
	}
}
