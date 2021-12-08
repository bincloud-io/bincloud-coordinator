package io.bce.actor;

import java.util.concurrent.CountDownLatch;

import io.bce.actor.EventLoop.Worker;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WaitableWorker implements Worker {
    private final Worker originalWorker;
    private final CountDownLatch workersExecutionWaiterLatch;

    @Override
    public void execute() {
        originalWorker.execute();
        workersExecutionWaiterLatch.countDown();
    }
}
