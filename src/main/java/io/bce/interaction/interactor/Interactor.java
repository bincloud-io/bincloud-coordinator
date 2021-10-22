package io.bce.interaction.interactor;

import io.bce.promises.Promise;
import io.bce.timer.Timeout;
import lombok.NonNull;

/**
 * This interface describes the contract for interaction mechanism betweeb
 * components.
 * 
 * 
 * @author Dmitry Mikhaylenko
 * 
 * @param <Q> The request type name
 * @param <S> The reply type name
 */
public interface Interactor<Q, S> {

	/**
	 * Invoke a target point. The promise will be resolved if the called target
	 * point is handle request and returned response. The promise will be rejected
	 * if an error is happened during data delivering or target component returned
	 * error response
	 * 
	 * @param request The request object
	 * @return The result promise
	 */
	public Promise<S> invoke(Q request);

	public class WrongRequestTypeException extends RuntimeException {
		private static final long serialVersionUID = -7913320375639849714L;

		public WrongRequestTypeException(@NonNull Class<?> expectedType, @NonNull Object request) {
			super(String.format("Request type %s isn't matched to the expected %s", request.getClass(), expectedType));
		}
	}

	public class WrongResponseTypeException extends RuntimeException {
		private static final long serialVersionUID = 1708762115152750631L;

		public WrongResponseTypeException(@NonNull Class<?> expectedType, @NonNull Object response) {
			super(String.format("Response type %s isn't matched to the expected %s", response.getClass(),
					expectedType));
		}
	}

	/**
	 * This interface declares the contract of creating interactor, bound to the concrete
	 * target with specified request and response types
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 */
	public interface Factory {
		/**
		 * Create the interactor with specified parameters
		 * 
		 * @param <Q> The request type name
		 * @param <S> The response type name
		 * @param target The target address
		 * @param requestType The request type
		 * @param responseType The response type
		 * @param timeout The response waiting timeout
		 * @return The created interactor
		 */
		public <Q, S> Interactor<Q, S> createInteractor(TargetAddress target, Class<Q> requestType,
				Class<S> responseType, Timeout timeout);
	}
}
