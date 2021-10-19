package io.bcs.common.port.adapters.web;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import io.bcs.common.domain.model.error.ApplicationException;
import io.bcs.common.domain.model.error.UnexpectedSystemBehaviorException;
import io.bcs.common.domain.model.error.AsyncErrorsHandler.ErrorInterceptor;
import io.bcs.common.domain.model.message.MessageProcessor;
import io.bcs.common.domain.model.message.templates.ErrorDescriptorTemplate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpServletAppliactionExceptionHandler<E extends ApplicationException>
		implements ErrorInterceptor<AsyncContext, E> {
	public static final String ERROR_BOUNDED_CONTEXT_HTTP_HEADER = "X-ERR-BND-CONTEXT";
	public static final String ERROR_CODE_HTTP_HEADER = "X-ERR-CODE";
	public static final String ERROR_SEVERITY_HEADER = "X-ERR-SEVERITY";

	private final MessageProcessor messageProcessor;
	private final Integer errorCode;

	@Override
	public void handleError(AsyncContext context, E error) {
		HttpServletResponse response = (HttpServletResponse) context.getResponse();
		try {
			response.setHeader(ERROR_BOUNDED_CONTEXT_HTTP_HEADER, error.getContext());
			response.setHeader(ERROR_CODE_HTTP_HEADER, String.valueOf(error.getErrorCode()));
			response.setHeader(ERROR_SEVERITY_HEADER, error.getSeverity().name());
			response.sendError(errorCode, messageProcessor.interpolate(new ErrorDescriptorTemplate(error)));
		} catch (IOException ioError) {
			throw new UnexpectedSystemBehaviorException(error);
		} finally {
			context.complete();
		}
	}
	
	public static final <E extends ApplicationException> ErrorInterceptor<AsyncContext, E> applicationErrorHandler(MessageProcessor messageProcessor, int errorCode) {
		return new HttpServletAppliactionExceptionHandler<>(messageProcessor, errorCode);
	}

	public static final ErrorInterceptor<AsyncContext, Exception> defaultErrorHandler(MessageProcessor messageProcessor,
			String boundedContext) {
		HttpServletAppliactionExceptionHandler<UnexpectedSystemBehaviorException> applicationErrorHandler = new HttpServletAppliactionExceptionHandler<>(
				messageProcessor, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return (context, error) -> {
			applicationErrorHandler.handleError(context, new UnexpectedSystemBehaviorException(boundedContext, error));
		};
	}
}
