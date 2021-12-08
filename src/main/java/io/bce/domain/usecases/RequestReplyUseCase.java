package io.bce.domain.usecases;

/**
 * This interface declares the request-reply use-case type. This use-case type passes request, and
 * returns response.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <Q> The request type name
 * @param <S> The response type name
 */
public interface RequestReplyUseCase<Q, S> {
  /**
   * Execute use-case.
   *
   * @param request The passed request
   * @return The response
   */
  public S execute(Q request);

  /**
   * Execute use-case.
   *
   * @author Dmitry Mikhaylenko
   *
   * @param <Q> The request type name
   * @param <S> The response type name
   */
  public interface Decorator<Q, S> {
    /**
     * Apply decorator logic.
     *
     * @param request  The passed request
     * @param original The original use-case
     * @return The response
     */
    public S execute(Q request, RequestReplyUseCase<Q, S> original);
  }

  /**
   * Decorate the request-only use-case.
   *
   * @param <Q>       The request type name
   * @param <S>       The response type name
   * @param original  The original use-case
   * @param decorator The use-case decorator
   * @return The derived use-case
   */
  public static <Q, S> RequestReplyUseCase<Q, S> decorate(RequestReplyUseCase<Q, S> original,
      Decorator<Q, S> decorator) {
    return request -> {
      return decorator.execute(request, original);
    };
  }
}
