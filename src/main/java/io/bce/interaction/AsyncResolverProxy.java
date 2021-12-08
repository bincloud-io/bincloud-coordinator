package io.bce.interaction;

import io.bce.promises.Deferred;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.NonNull;

/**
 * This class is the deferred object wrapper, which execute promise resolving and rejection inside
 * separated thread.
 *
 * @author Dmitry Mikhaylenko
 *
 * @param <T> The resolution data type name
 */
public class AsyncResolverProxy<T> implements Deferred<T> {
  private final Deferred<T> original;
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  public AsyncResolverProxy(@NonNull Deferred<T> original) {
    super();
    this.original = original;
  }

  @Override
  public void resolve(T response) {
    executeAsync(() -> original.resolve(response));
  }

  @Override
  public void reject(Throwable error) {
    executeAsync(() -> original.reject(error));
  }

  private void executeAsync(Runnable task) {
    executorService.execute(() -> {
      task.run();
      executorService.shutdownNow();
    });
  }

}
