package io.bce;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

/**
 * This class is responsible for working with critical sections.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class CriticalSection {
  public static final Long DEFAULT_AWAITING_TIME_IN_SECONDS = 120L;

  private final Lock lock = new ReentrantLock();
  private final Long awaitTime;
  private final TimeUnit unit;

  public CriticalSection() {
    this(DEFAULT_AWAITING_TIME_IN_SECONDS, TimeUnit.SECONDS);
  }

  /**
   * Execute a function which isn't return any response inside the critical section.
   *
   * @param function The function, running inside critical section
   */
  public void executeCriticalSection(Runnable function) {
    executeCriticalSection(() -> {
      function.run();
      return null;
    });
  }

  /**
   * Execute a function which provides a response inside the critical section.
   *
   * @param <D>      The response type, provided by the function
   * @param function The function, running inside critical section
   * @return The result, returned by function
   */
  public <D> D executeCriticalSection(Supplier<D> function) {
    acquireLock();
    try {
      return function.get();
    } finally {
      releaseLock();
    }
  }

  private void acquireLock() {
    if (!tryAcquireLock()) {
      throw new LockWaitingTimeoutException(awaitTime, unit);
    }
  }

  private void releaseLock() {
    lock.unlock();
  }

  private boolean tryAcquireLock() {
    try {
      return lock.tryLock(awaitTime, unit);
    } catch (InterruptedException e) {
      throw new MustNeverBeHappenedError(e);
    }
  }

  /**
   * This class notifies about the exceptional case when the lock release waiting time is over.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public static final class LockWaitingTimeoutException extends RuntimeException {
    private static final long serialVersionUID = 2811853824554414628L;

    private LockWaitingTimeoutException(Long awaitTime, TimeUnit timeUnit) {
      super(String.format("The lock timeout is over: Await time is [%s], time unit is [%s]",
          awaitTime, timeUnit));
    }
  }
}
