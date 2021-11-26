package io.bce.actor;

import java.util.concurrent.CountDownLatch;

import io.bce.MustNeverBeHappenedError;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WorkersWaiter {
    private final CountDownLatch workersExecutionWaiterLatch;

    public void await() {
        try {
            workersExecutionWaiterLatch.await();
        } catch (InterruptedException errors) {
            throw new MustNeverBeHappenedError(errors);
        }
    }
}
