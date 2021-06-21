package io.bincloud.resources.port.adapter.endpoint.files;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletResponse;

import io.bincloud.common.domain.model.error.ApplicationException;
import io.bincloud.common.domain.model.error.UnexpectedSystemBehaviorException;
import io.bincloud.common.domain.model.error.AsyncErrorsHandler.ErrorInterceptor;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.resources.port.adapter.endpoint.ServletErrorResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServletResponseHandler {
	private final MessageProcessor messageProcessor;
	
	public ErrorInterceptor<AsyncContext, Exception> createDefaultErrorHandler(String context) {
		ErrorInterceptor<AsyncContext, UnexpectedSystemBehaviorException> defaultHandler = createErrorHandler(HttpServletResponse.SC_BAD_REQUEST);
		return (handlerContext, exception) -> defaultHandler.handleError(handlerContext, new UnexpectedSystemBehaviorException(context, exception));
	}
	
	public <E extends ApplicationException> ErrorInterceptor<AsyncContext, E> createErrorHandler(int httpCode) {
		return (context, error) -> {
			sendResponse(context, httpCode, new ServletErrorResponse(error, messageProcessor));
		};
	}
	
	public void sendResponse(AsyncContext asyncContext, int code, Properties properties) {
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
