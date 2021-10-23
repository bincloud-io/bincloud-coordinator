package io.bce.promises;

/**
 * This interface describes the contract, promise abstraction. Promise is the
 * abstraction which can be resolved or rejected. Imagine the situation if your
 * method has to receive response asynchronously for example for long time
 * operations and you don't want waste your CPU resource on response awaiting.
 * In this situation you can return promise, which is resolved on successful
 * complete and rejected on failures. Your client code could subscribe to
 * resolve and reject operations and assign corresponding behavior on each of
 * them situation.
 * 
 * @author Dmitry Mikhaylenko
 *
 * @param <T> The promise resolution type
 */
public interface Promise<T> {

	public Promise<T> then(ResponseHandler<T> responseHandler);

	public Promise<T> then(Deferred<T> resolver);
	
	public <E extends Throwable> Promise<T> error(Class<E> errorType, ErrorHandler<E> errorHandler);

	public Promise<T> error(ErrorHandler<Throwable> errorHandler);

	public Promise<T> error(Deferred<T> rejector);
	
	public <C> Promise<C> chain(ChainingDeferredFunction<T, C> chainingDeferredFunction);
	
	public <C> Promise<C> chain(ChainingPromiseHandler<T, C> chainingPromiseProvider);
	
	public Promise<T> delegate(Deferred<T> deferred);

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

	/**
	 * This interface declares the contract for the component which performs a
	 * intermediate deferred operation on promise chaining
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <Q> The promise input request type
	 * @param <S> The promise output response resolving type
	 */
	public interface ChainingDeferredFunction<Q, S> {
		/**
		 * Execute intermediate operation
		 * 
		 * @param previousResult The previous promise result
		 * @param deferred     The deferred object
		 */
		public void execute(Q previousResult, Deferred<S> deferred);
	}

	/**
	 * This interface declares the contract for alternative way of response handling
	 * on a promise chaining
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <Q> The promise input request type
	 * @param <S> The promise output response resolving type
	 */
	public interface ChainingPromiseHandler<Q, S> {
		/**
		 * Execute intermediate operation
		 * 
		 * @param previousResult The previous promise result
		 * @return The derived promise object
		 */
		public Promise<S> derivePronise(Q previousResult);
	}
}