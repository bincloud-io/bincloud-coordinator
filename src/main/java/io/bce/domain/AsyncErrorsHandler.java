package io.bce.domain;

import java.util.HashMap;
import java.util.Map;

import io.bce.domain.errors.UnexpectedErrorException;

public class AsyncErrorsHandler<C> {
	private final Class<C> contextType;
	private final Map<Class<?>, ErrorInterceptor<C, Exception>> errorHandlers;
	private ErrorInterceptor<C, Exception> defaultExceptionHandler = (context, error) -> {throw new UnexpectedErrorException(error);};
	
	private AsyncErrorsHandler(Class<C> contextType) {
		super();
		this.contextType = contextType;
		this.errorHandlers = new HashMap<>();
	}
	
	private AsyncErrorsHandler(AsyncErrorsHandler<C> proto) {
		super();
		this.contextType = proto.contextType;
		this.errorHandlers = new HashMap<>(proto.errorHandlers);
		this.defaultExceptionHandler = proto.defaultExceptionHandler;
	}
	
	public void handleError(C context, Exception error) {
			checkContextType(context);
			findErrorHandler(error.getClass()).handleError(context, error);
	}
	
	@SuppressWarnings("unchecked")
	public <E extends Exception> AsyncErrorsHandler<C> registerHandler(Class<E> errorType, ErrorInterceptor<C, E> handler) {
		AsyncErrorsHandler<C> result = new AsyncErrorsHandler<C>(this);
		result.addErrorHandler(errorType, (ErrorInterceptor<C, Exception>) handler);
		return result;
	}
	
	public AsyncErrorsHandler<C> registerDefaultHandler(ErrorInterceptor<C, Exception> errorHandler) {
		AsyncErrorsHandler<C> result = new AsyncErrorsHandler<C>(this);
		result.changeDefaultErrorHandler(errorHandler);
		return result;
	}
	
	public static <C> AsyncErrorsHandler<C> createFor(Class<C> contextType) {
		return new AsyncErrorsHandler<C>(contextType);
	}
	
	private void addErrorHandler(Class<?> errorType, ErrorInterceptor<C, Exception> handler) {
		this.errorHandlers.put(errorType, handler);
	}
	
	private void changeDefaultErrorHandler(ErrorInterceptor<C, Exception> defaultHandler) {
		this.defaultExceptionHandler = defaultHandler;
	}
	
	private void checkContextType(C context) {
		if (!contextType.isInstance(context)) {
			throw new IllegalArgumentException("The wrong context type has been received");
		}
	}
	
	private ErrorInterceptor<C, Exception> findErrorHandler(Class<?> errorType) {
		if (errorHandlers.containsKey(errorType)) {
			return errorHandlers.get(errorType);
		}
		return defaultExceptionHandler;
	}
	
	@FunctionalInterface
	public interface ErrorInterceptor<C, E> {
		public void handleError(C context, E error);
	}
}
