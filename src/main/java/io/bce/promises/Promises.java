package io.bce.promises;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import io.bce.promises.Deferred.DeferredFunction;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class Promises {

	/**
	 * Create a deferred function
	 * 
	 * @param <T>               The promise resolving type name
	 * @param deferredOperation The deferred operation function
	 * @return The promise for deferred operation
	 */
	public static final <T> Promise<T> of(DeferredFunction<T> deferredOperation) {
		return new DeferredPromise<>(deferredOperation);
	}

	/**
	 * Create a promise resolved by the specified value
	 * 
	 * @param <T>   The promise resolving type name
	 * @param value The promise resolving value
	 * @return The resolved promise
	 */
	public static final <T> Promise<T> resolvedBy(T value) {
		return of(deferred -> deferred.resolve(value));
	}

	/**
	 * Create a promise rejected by the specified error
	 * 
	 * @param <T>The promise resolving type name
	 * @param <E>The promise rejection error name
	 * @param error  The promise rejection error
	 * @return The rejected promise
	 */
	public static final <T, E extends Throwable> Promise<T> rejectedBy(E error) {
		return of(deferred -> deferred.reject(error));
	}

	private static final class DeferredPromise<T> implements Promise<T> {
		private final Collection<ResponseHandlerDescriptor> responseHandlers = new LinkedList<>();
		private final Collection<ErrorHandlerDescriptor> errorHandlers = new LinkedList<>();
		private State state = new PendingState();

		public DeferredPromise(@NonNull DeferredFunction<T> deferredOperationExecutor) {
			super();
			executeDeferredOperation(deferredOperationExecutor);
		}

		public DeferredPromise(@NonNull DeferredFunction<T> deferredOperationExecutor,
				Collection<ErrorHandlerDescriptorState> descriptors) {
			super();
			appendAllErrorHandlers(descriptors);
			executeDeferredOperation(deferredOperationExecutor);
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

		@Override
		public <C> Promise<C> chain(ChainingDeferredFunction<T, C> chainingDeferredFunction) {
			return new DeferredPromise<C>(createChainingDeferredFunctionExecutor(chainingDeferredFunction),
					getDescriptorStates());
		}

		@Override
		public <C> Promise<C> chain(ChainingPromiseHandler<T, C> chainingPromiseProvider) {
			return new DeferredPromise<C>(createChainingDeferredFunctionExecutor((previousResult, deferred) -> {
				chainingPromiseProvider.derivePronise(previousResult).delegate(deferred);
			}));
		}

		@Override
		public Promise<T> then(Deferred<T> resolver) {
			return then(response -> resolver.resolve(response));
		}

		@Override
		public Promise<T> error(Deferred<T> rejector) {
			return error(error -> rejector.reject(error));
		}

		@Override
		public Promise<T> delegate(Deferred<T> deferred) {
			return this.then(deferred).error(deferred);
		}

		private <C> DeferredFunction<C> createChainingDeferredFunctionExecutor(
				ChainingDeferredFunction<T, C> chainingDeferredFunction) {
			return deferred -> {
				then(response -> {
					chainingDeferredFunction.execute(response, deferred);
				}).error(error -> deferred.reject(error));
			};
		}

		private Collection<ErrorHandlerDescriptorState> getDescriptorStates() {
			return errorHandlers.stream()
					.collect(Collectors.mapping(ErrorHandlerDescriptorState.class::cast, Collectors.toList()));
		}

		private void executeDeferredOperation(DeferredFunction<T> deferredOperationExecutor) {
			ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
			singleThreadPool.execute(() -> {
				executeDeferredOperationSafely(deferredOperationExecutor, createDeferred());
				singleThreadPool.shutdown();
			});
		}

		private void executeDeferredOperationSafely(DeferredFunction<T> deferredOperationExecutor,
				Deferred<T> deferred) {
			try {
				deferredOperationExecutor.execute(deferred);
			} catch (Throwable error) {
				deferred.reject(error);
			}
		}

		private void appendAllErrorHandlers(Collection<ErrorHandlerDescriptorState> descriptors) {
			for (ErrorHandlerDescriptorState handlerState : descriptors) {
				this.errorHandlers.add(new ErrorHandlerDescriptor(handlerState));
			}
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

		private interface ErrorHandlerDescriptorState {
			public Class<?> getErrorType();

			public ErrorHandler<Throwable> getErrorHandler();
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
		private class ErrorHandlerDescriptor extends HandlerDescriptor<Throwable>
				implements ErrorHandlerDescriptorState {
			@Getter
			private final Class<?> errorType;
			@Getter
			private final ErrorHandler<Throwable> errorHandler;

			public ErrorHandlerDescriptor(ErrorHandlerDescriptorState proto) {
				this(proto.getErrorType(), proto.getErrorHandler());
			}

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
