package io.bce.timer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class supervise that timeout has not been exceeded and if it is happened the
 * {@link TimeoutCallback#onTimeout()} will be called.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public final class TimeoutSupervisor {
  private final ScheduledExecutorService executorService;
  private final TimeoutCallback callback;
  private final Timeout timeout;

  /**
   * Create the timeout supervisor.
   *
   * @param timeout  The timeout value
   * @param callback The timeout handling callback.
   */
  public TimeoutSupervisor(Timeout timeout, TimeoutCallback callback) {
    super();
    this.executorService = Executors.newSingleThreadScheduledExecutor();
    this.callback = callback;
    this.timeout = timeout;
  }

  /**
   * Start the timeout supervision.
   */
  public void startSupervision() {
    executorService.schedule(() -> callback.onTimeout(), timeout.getMilliseconds(),
        TimeUnit.MILLISECONDS);
  }

  /**
   * Stop the timeout supervision.
   */
  public void stopSupervision() {
    executorService.shutdownNow();
  }

  /**
   * This interface describes the contract for timeout reaction mechanism. When timeout is happened,
   * the {@link TimeoutSupervisor} will call {@link TimeoutCallback#onTimeout()} method, in which
   * you have to put your logic for timeout processing.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface TimeoutCallback {
    /**
     * Handle timeout.
     */
    public void onTimeout();
  }
}
