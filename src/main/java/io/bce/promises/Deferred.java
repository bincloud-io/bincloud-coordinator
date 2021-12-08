package io.bce.promises;

/**
 * This interface declares the triggers for promise resolution.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <T> The promise result type name
 */
public interface Deferred<T> {
  /**
   * Resolve promise with response.
   *
   * @param response The response
   */
  public void resolve(T response);

  /**
   * Reject promise with error.
   *
   * @param error The error
   */
  public void reject(Throwable error);

  /**
   * This interface declares the contract for the component which performs a deferred operation.
   *
   * @author Dmitry Mikhaylenko
   *
   * @param <T> The promise result type name
   */
  public interface DeferredFunction<T> {
    /**
     * Execute the deferred operation.
     *
     * @param deferred The deferred object
     */
    public void execute(Deferred<T> deferred);
  }
}