package io.bce;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Locker {
	public static final Long DEFAULT_AWAITING_TIME_IN_SECONDS = 120L;
	
	private final Lock lock = new ReentrantLock();
	private final Long awaitTime;
	private final TimeUnit unit;
	
	public Locker() {
		this(DEFAULT_AWAITING_TIME_IN_SECONDS, TimeUnit.SECONDS);
	}

	public void executeCriticalSection(Runnable runnable) {
		executeCriticalSection(() -> {
			runnable.run();
			return null;
		});
	}
	
	public <D> D executeCriticalSection(Supplier<D> supplier) {
		acquireLock();
		try {
			return supplier.get();
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
	
	public static final class LockWaitingTimeoutException extends RuntimeException {
		private static final long serialVersionUID = 2811853824554414628L;

		private LockWaitingTimeoutException(Long awaitTime, TimeUnit timeUnit) {
			super(String.format("The lock timeout is over: Await time is [%s], time unit is [%s]", awaitTime, timeUnit));
		}		
	}
}
