package io.bcs.storage.port.adapter.file.web.upload;

import java.io.IOException;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.bce.domain.BoundedContextId;
import io.bce.domain.errors.ApplicationException;
import io.bce.text.TextProcessor;
import io.bcs.common.domain.model.io.transfer.SourcePoint;
import io.bcs.common.domain.model.web.KeyValueParameters;
import io.bcs.common.port.adapters.io.transfer.sources.StreamSource;
import io.bcs.common.port.adapters.web.AsyncServletOperationExecutor;
import io.bcs.common.port.adapters.web.HttpServletAppliactionExceptionHandler;
import io.bcs.common.port.adapters.web.HttpServletRequestParameters;
import io.bcs.storage.domain.model.Constants;
import io.bcs.storage.domain.model.FileId;
import io.bcs.storage.domain.model.contracts.FileDescriptor;
import io.bcs.storage.domain.model.contracts.FilePointer;
import io.bcs.storage.domain.model.contracts.upload.FileUploadListener;
import io.bcs.storage.domain.model.contracts.upload.FileUploader;
import io.bcs.storage.port.adapter.ServerContextProvider;
import io.bcs.storage.port.adapter.file.web.AsyncErrorsHandler;
import io.bcs.storage.port.adapter.file.web.AsyncErrorsHandler.ErrorInterceptor;

public class HttpFileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = -8092602564530445635L;
	private static final String FILE_ID_PARAMETER_NAME = "fileId";

	@Inject
	private FileUploader fileUploader;

	@Inject
	private TextProcessor textProcessor;

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

//			FileUploadListener uploadingCallback = new HttpFileUploadCallback(context);
//			try {
//				SourcePoint sourcePoint = new StreamSource(context.getRequest().getInputStream(),
//						serverContext.getIOBufferSize());
//				fileUploader.uploadFileContent(getRevisionPointer(context), getContentSize(context), sourcePoint,
//						uploadingCallback);
//			} catch (IOException error) {
//				uploadingCallback.onError(error);
//			}
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
//					.registerHandler(UnspecifiedResourceException.class,
//							createErrorHandler(HttpServletResponse.SC_BAD_REQUEST))
//					.registerHandler(ResourceDoesNotExistException.class,
//							createErrorHandler(HttpServletResponse.SC_BAD_REQUEST))
					.registerHandler(ApplicationException.class,
							createErrorHandler(HttpServletResponse.SC_INTERNAL_SERVER_ERROR))
					.registerDefaultHandler(createDefaultErrorHandler(Constants.CONTEXT));
		}
	}

//	private FilePointer getRevisionPointer(AsyncContext asyncContext) {
//		KeyValueParameters parameters = new HttpServletRequestParameters(
//				(HttpServletRequest) asyncContext.getRequest());
//		return new FilePointer() {
//			@Override
//			public Optional<FileId> getFilesystemName() {
//				return parameters.getStringValue(FILE_ID_PARAMETER_NAME).map(FileId::new);
//			}
//		};
//	}
//
//	private Long getContentSize(AsyncContext asyncContext) {
//		return asyncContext.getRequest().getContentLengthLong();
//	}

	private ErrorInterceptor<AsyncContext, Exception> createDefaultErrorHandler(BoundedContextId context) {
		return HttpServletAppliactionExceptionHandler.defaultErrorHandler(textProcessor, context);
	}

	private <E extends ApplicationException> ErrorInterceptor<AsyncContext, E> createErrorHandler(int httpCode) {
		return HttpServletAppliactionExceptionHandler.applicationErrorHandler(textProcessor, httpCode);
	}
}
