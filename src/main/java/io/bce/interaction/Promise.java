package io.bce.interaction;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public final class Promise<T> {
	private final Collection<ResponseHandlerDescriptor> responseHandlers = new LinkedList<>();
	private final Collection<ErrorHandlerDescriptor> errorHandlers = new LinkedList<>();
	private State state = new PendingState();

	public Promise(@NonNull DeferredFunction<T> deferredOperationExecutor) {
		super();
		ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
		singleThreadPool.execute(() -> {
			deferredOperationExecutor.execute(createDeferred());
			singleThreadPool.shutdown();
		});
	}

	public Promise<T> then(@NonNull ResponseHandler<T> responseHandler) {
		state.addResponseHandler(responseHandler);
		return this;
	}

	public <E extends Throwable> Promise<T> error(@NonNull Class<E> errorType, @NonNull ErrorHandler<E> errorHandler) {
		state.addErrorHandler(errorType, errorHandler);
		return this;
	}

	public Promise<T> error(@NonNull ErrorHandler<Throwable> errorHandler) {
		return error(Throwable.class, errorHandler);
	}

	private Deferred<T> createDeferred() {
		return new Deferred<T>() {
			@Override
			public void resolve(T response) {
				state.resolve(response);
			}

			@Override
			public void reject(Throwable error) {
				state.reject(error);
			}
		};
	}

	private synchronized void handleResponse(T response) {
		responseHandlers.forEach(descriptor -> descriptor.apply(response));
	}

	private synchronized void handleError(Throwable error) {
		getAcceptableDescriptorsFor(error).forEach(descriptor -> descriptor.apply(error));
	}

	private Collection<ErrorHandlerDescriptor> getAcceptableDescriptorsFor(Throwable error) {
		return errorHandlers.stream().filter(handlerDescriptor -> handlerDescriptor.isAcceptableFor(error))
				.collect(Collectors.toList());
	}

	public static class PromiseHasAlreadyBeenRejectedException extends RuntimeException {
		private static final long serialVersionUID = 2952805140695891072L;

		public PromiseHasAlreadyBeenRejectedException() {
			super("Promise has already been rejected");
		}
	}

	public static class PromiseHasAlreadyBeenResolvedException extends RuntimeException {
		private static final long serialVersionUID = 2952805140695891072L;

		public PromiseHasAlreadyBeenResolvedException() {
			super("Promise has already been resolved");
		}
	}

	private abstract class State implements Deferred<T> {
		public void addResponseHandler(ResponseHandler<T> responseHandler) {
			responseHandlers.add(new ResponseHandlerDescriptor(responseHandler));
		}

		@SuppressWarnings("unchecked")
		public <E extends Throwable> void addErrorHandler(Class<E> errorType, ErrorHandler<E> errorHandler) {
			errorHandlers.add(new ErrorHandlerDescriptor(errorType, (ErrorHandler<Throwable>) errorHandler));
		}
	}

	private class PendingState extends State {
		@Override
		public void resolve(T response) {
			state = new ResolvedState(response);
			handleResponse(response);
		}

		@Override
		public void reject(Throwable error) {
			state = new RejectedState(error);
			handleError(error);
		}
	}

	@RequiredArgsConstructor
	private class ResolvedState extends State {
		private final T response;

		@Override
		public void resolve(T response) {
			throw new PromiseHasAlreadyBeenResolvedException();
		}

		@Override
		public void reject(Throwable error) {
			throw new PromiseHasAlreadyBeenResolvedException();
		}

		@Override
		public void addResponseHandler(ResponseHandler<T> responseHandler) {
			super.addResponseHandler(responseHandler);
			handleResponse(response);
		}
	}

	@RequiredArgsConstructor
	private class RejectedState extends State {
		private final Throwable error;

		@Override
		public void resolve(T response) {
			throw new PromiseHasAlreadyBeenRejectedException();
		}

		@Override
		public void reject(Throwable error) {
			throw new PromiseHasAlreadyBeenRejectedException();
		}

		@Override
		public <E extends Throwable> void addErrorHandler(Class<E> errorType, ErrorHandler<E> errorHandler) {
			super.addErrorHandler(errorType, errorHandler);
			handleError(error);
		}
	}

	private abstract class HandlerDescriptor<S> {
		private boolean passed = false;

		public void apply(S response) {
			if (!passed) {
				doAccept(response);
				passed = true;
			}
		}

		protected abstract void doAccept(S response);
	}

	@RequiredArgsConstructor
	private class ResponseHandlerDescriptor extends HandlerDescriptor<T> {
		private final ResponseHandler<T> responseHandler;

		@Override
		protected void doAccept(T response) {
			responseHandler.onResponse(response);
		}
	}

	@RequiredArgsConstructor
	private class ErrorHandlerDescriptor extends HandlerDescriptor<Throwable> {
		private final Class<?> errorType;
		private final ErrorHandler<Throwable> errorHandler;

		public boolean isAcceptableFor(Throwable error) {
			return errorType.isInstance(error);
		}

		@Override
		protected void doAccept(Throwable response) {
			errorHandler.onError(response);
		}
	}

	/**
	 * This interface declares the triggers for promise resolution
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <T> The promise result type name
	 */
	public interface Deferred<T> {
		/**
		 * Resolve promise with response
		 * 
		 * @param response The response
		 */
		public void resolve(T response);

		/**
		 * Reject promise with error
		 * 
		 * @param error The error
		 */
		public void reject(Throwable error);
	}

	/**
	 * This interface declares the contract for the component which performs a
	 * deferred operation
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <T> The promise result type name
	 */
	public interface DeferredFunction<T> {
		/**
		 * Execute the deferred operation
		 * 
		 * @param deferred The deferred object
		 */
		public void execute(Deferred<T> deferred);
	}

	/**
	 * This interface declares the contract for the component which handles result
	 * on resolving
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <T> The promise result type name
	 */
	public interface ResponseHandler<T> {
		/**
		 * Receive response asynchronously
		 * 
		 * @param response The response object
		 */
		public void onResponse(T response);
	}

	/**
	 * This interface declares the contract for the component which handles error on
	 * rejecting
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <E> The error handler
	 */
	public interface ErrorHandler<E extends Throwable> {
		/**
		 * Receive error asynchronously
		 * 
		 * @param error The error object
		 */
		public void onError(E error);
	}
}
