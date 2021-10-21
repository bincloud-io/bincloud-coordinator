package io.bce.interaction.promises;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import io.bce.interaction.promises.Deferred.DeferredFunction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public final class Promises {

	/**
	 * Create the deferred function
	 * 
	 * @param <T>
	 * @param deferredOperationExecutor
	 * @return
	 */
	public static final <T> Promise<T> of(DeferredFunction<T> deferredOperationExecutor) {
		return new DeferredPromise<>(deferredOperationExecutor);
	}

	private static final class DeferredPromise<T> implements Promise<T> {
		private final Collection<ResponseHandlerDescriptor> responseHandlers = new LinkedList<>();
		private final Collection<ErrorHandlerDescriptor> errorHandlers = new LinkedList<>();
		private State state = new PendingState();

		public DeferredPromise(@NonNull DeferredFunction<T> deferredOperationExecutor) {
			super();
			ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
			singleThreadPool.execute(() -> {
				deferredOperationExecutor.execute(createDeferred());
				singleThreadPool.shutdown();
			});
		}

		@Override
		public Promise<T> then(@NonNull ResponseHandler<T> responseHandler) {
			state.addResponseHandler(responseHandler);
			return this;
		}

		@Override
		public <E extends Throwable> Promise<T> error(@NonNull Class<E> errorType,
				@NonNull ErrorHandler<E> errorHandler) {
			state.addErrorHandler(errorType, errorHandler);
			return this;
		}

		@Override
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
}
