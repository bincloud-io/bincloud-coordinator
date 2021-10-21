package io.bce.interaction.promises;


public interface Promise<T> {

	Promise<T> then(ResponseHandler<T> responseHandler);

	<E extends Throwable> Promise<T> error(Class<E> errorType, ErrorHandler<E> errorHandler);

	Promise<T> error(ErrorHandler<Throwable> errorHandler);

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