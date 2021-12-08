package io.bce.timer;

import lombok.NonNull;

/**
 * This exception notifies about exceptional case when the waiting time is over.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public final class TimeoutException extends RuntimeException {
  private static final long serialVersionUID = 1882272675937453878L;

  public TimeoutException(@NonNull Timeout timeout) {
    super(String.format("The actor response waiting time is over. Timeout is %s.", timeout));
  }
}
