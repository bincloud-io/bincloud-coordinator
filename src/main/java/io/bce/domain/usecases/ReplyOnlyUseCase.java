package io.bce.domain.usecases;

/**
 * This interface declares the reply-only use-case type. This use-case type returns response, but
 * doesn't pass request.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <S> The response type name
 */
public interface ReplyOnlyUseCase<S> {
  /**
   * Execute use-case.
   *
   * @return The response
   */
  public S execute();

  /**
   * This interface describes the reply-only use case decorator signature.
   *
   * @author Dmitry Mikhaylenko
   *
   * @param <S> The response type name
   */
  public interface Decorator<S> {
    /**
     * Apply decorator logic.
     *
     * @param original The original use-case
     * @return The returned response
     */
    public S execute(ReplyOnlyUseCase<S> original);
  }

  /**
   * Decorate the reply-only use-case.
   *
   * @param <S>       The response type name
   * @param original  The original use-case
   * @param decorator The use-case decorator
   * @return The derived use-case
   */
  public static <S> ReplyOnlyUseCase<S> decorate(ReplyOnlyUseCase<S> original,
      Decorator<S> decorator) {
    return () -> decorator.execute(original);
  }
}
