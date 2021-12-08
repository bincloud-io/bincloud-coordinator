package io.bce.domain.usecases;

/**
 * This interface declares the request-only use-case type. This use-case type passes request, but
 * doesn't return response.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <Q> The request type name
 */
public interface RequestOnlyUseCase<Q> {
  /**
   * Execute use-case.
   *
   * @param request The request
   */
  public void execute(Q request);

  /**
   * This interface describes the request-only use case decorator signature.
   *
   * @author Dmitry Mikhaylenko
   *
   * @param <Q> The request type name
   */
  public interface Decorator<Q> {
    /**
     * Apply decorator logic.
     *
     * @param request  The passed request
     * @param original The original use-case
     */
    public void execute(Q request, RequestOnlyUseCase<Q> original);
  }

  /**
   * Decorate the request-only use-case.
   *
   * @param <Q>       The request type name
   * @param original  The original use-case
   * @param decorator The use-case decorator
   * @return The derived use-case
   */
  public static <Q> RequestOnlyUseCase<Q> decorate(RequestOnlyUseCase<Q> original,
      Decorator<Q> decorator) {
    return request -> decorator.execute(request, original);
  }
}
