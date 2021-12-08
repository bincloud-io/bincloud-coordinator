package io.bce.promises;

import io.bce.promises.Deferred.DeferredFunction;
import io.bce.promises.Promises.DeferredFunctionRunner;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * This class is the deferred function runner, based on the executor service
 * {@link ExecutorService}.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class ExecutorServiceBasedDeferredFunctionRunner implements DeferredFunctionRunner {
  private final Supplier<ExecutorService> executorServiceCreator;

  @Override
  public <T> void executeDeferredOperation(DeferredFunction<T> deferredExecutor,
      Deferred<T> deferred) {
    ExecutorService singleThreadPool = executorServiceCreator.get();
    singleThreadPool.execute(() -> {
      createSafeDeferredFunction(deferredExecutor).execute(deferred);
      singleThreadPool.shutdown();
    });
  }

  private <T> DeferredFunction<T> createSafeDeferredFunction(
      DeferredFunction<T> deferredOperationExecutor) {
    return deferred -> {
      try {
        deferredOperationExecutor.execute(deferred);
      } catch (Throwable error) {
        deferred.reject(error);
      }
    };
  }
}