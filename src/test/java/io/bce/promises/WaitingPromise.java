package io.bce.promises;

import io.bce.promises.Promise.ErrorHandler;
import io.bce.promises.Promise.FinalizingHandler;
import io.bce.promises.Promise.ResponseHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * This class is the promise wrapper which allows wait the moment when the promise will resolved or
 * rejected. This class might be used for example in tests, when we capture arguments of the error
 * handlers or response handlers.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <T> The resolving data type
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WaitingPromise<T> {
  private Promise<T> original;
  private CountDownLatch latch = new CountDownLatch(0);

  private WaitingPromise(Promise<T> original) {
    super();
    this.original = original;

  }

  private WaitingPromise(CountDownLatch latch, Promise<T> original) {
    super();
    this.latch = latch;
    this.original = original;
  }

  public static final <T> WaitingPromise<T> of(Promise<T> original) {
    return new WaitingPromise<>(original);
  }

  /**
   * Determine reaction on the promise resolving.
   *
   * @param responseHandler The respons handler
   * @return The derived promise instance
   */
  public WaitingPromise<T> then(ResponseHandler<T> responseHandler) {
    incrementLatch();
    this.original = this.original.then(createProxyResponseHandler(responseHandler));
    return this;
  }

  /**
   * Determine reaction on the promise rejection for a specified exception type and it's subtypes.
   *
   * @param <E>          The exception type name
   * @param errorType    The error type
   * @param errorHandler THe error handler
   * @return The derived promise instance
   */
  public <E extends Throwable> WaitingPromise<T> error(Class<E> errorType,
      ErrorHandler<E> errorHandler) {
    incrementLatch();
    this.original = this.original.error(errorType, createProxyErrorHandler(errorHandler));
    return this;
  }

  /**
   * Determine default reaction for errors, which isn't matched to the registered event types and
   * it's subtypes.
   *
   * @param errorHandler The error handler
   * @return The derived promise instance
   */
  public WaitingPromise<T> error(ErrorHandler<Throwable> errorHandler) {
    incrementLatch();
    this.original = this.original.error(createProxyErrorHandler(errorHandler));
    return this;
  }

  /**
   * Determine the reaction on promise state resolution. It happens after both, resolve and reject.
   *
   * @param finalizer The promise finalizer
   * @return The derived promise instance
   */
  public WaitingPromise<T> finalize(FinalizingHandler finalizer) {
    incrementLatch();
    this.original = this.original.finalize(createProxyFinalizer(finalizer));
    return this;
  }

  @SneakyThrows
  public Promise<T> await(@NonNull Long timeout) {
    this.latch.await(timeout, TimeUnit.SECONDS);
    return original;
  }

  public Promise<T> await() {
    return await(10L);
  }

  private ResponseHandler<T> createProxyResponseHandler(ResponseHandler<T> responseHandler) {
    return response -> {
      responseHandler.onResponse(response);
      countDown();
    };
  }

  private <
      E extends Throwable> ErrorHandler<E> createProxyErrorHandler(ErrorHandler<E> errorHandler) {
    return error -> {
      errorHandler.onError(error);
      countDown();
    };
  }

  private FinalizingHandler createProxyFinalizer(FinalizingHandler finalizer) {
    return () -> {
      finalizer.onComplete();
      countDown();
    };
  }

  private void incrementLatch() {
    this.latch = new CountDownLatch((int) latch.getCount() + 1);
  }

  private void countDown() {
    latch.countDown();
  }
}
