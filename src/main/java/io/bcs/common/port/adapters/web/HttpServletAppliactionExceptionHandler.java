package io.bcs.common.port.adapters.web;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import io.bce.domain.BoundedContextId;
import io.bce.domain.ErrorDescriptorTemplate;
import io.bce.domain.errors.ApplicationException;
import io.bce.domain.errors.UnexpectedErrorException;
import io.bce.text.TextProcessor;
import io.bcs.storage.port.adapter.file.web.AsyncErrorsHandler.ErrorInterceptor;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpServletAppliactionExceptionHandler<E extends ApplicationException>
		implements ErrorInterceptor<AsyncContext, E> {
	public static final String ERROR_BOUNDED_CONTEXT_HTTP_HEADER = "X-ERR-BND-CONTEXT";
	public static final String ERROR_CODE_HTTP_HEADER = "X-ERR-CODE";
	public static final String ERROR_SEVERITY_HEADER = "X-ERR-SEVERITY";

	private final TextProcessor textProcessor;
	private final Integer errorCode;

	@Override
	public void handleError(AsyncContext context, E error) {
		HttpServletResponse response = (HttpServletResponse) context.getResponse();
		try {
			response.setHeader(ERROR_BOUNDED_CONTEXT_HTTP_HEADER, error.getContextId().toString());
			response.setHeader(ERROR_CODE_HTTP_HEADER, String.valueOf(error.getErrorCode()));
			response.setHeader(ERROR_SEVERITY_HEADER, error.getErrorSeverity().name());
			response.sendError(errorCode, textProcessor.interpolate(ErrorDescriptorTemplate.createFor(error)));
		} catch (IOException ioError) {
			throw new UnexpectedErrorException(ioError);
		} finally {
			context.complete();
		}
	}

	public static final <E extends ApplicationException> ErrorInterceptor<AsyncContext, E> applicationErrorHandler(
			TextProcessor messageProcessor, int errorCode) {
		return new HttpServletAppliactionExceptionHandler<>(messageProcessor, errorCode);
	}

	public static final ErrorInterceptor<AsyncContext, Exception> defaultErrorHandler(TextProcessor textProcessor,
			BoundedContextId boundedContext) {
		HttpServletAppliactionExceptionHandler<UnexpectedErrorException> applicationErrorHandler = new HttpServletAppliactionExceptionHandler<>(
				textProcessor, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return (context, error) -> {
			applicationErrorHandler.handleError(context,
					new UnexpectedErrorException(boundedContext, error));
		};
	}
}
