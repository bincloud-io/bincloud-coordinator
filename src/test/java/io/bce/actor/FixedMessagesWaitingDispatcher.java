package io.bce.actor;

import java.util.concurrent.CountDownLatch;

import io.bce.actor.EventLoop.Dispatcher;
import io.bce.actor.EventLoop.Worker;

public class FixedMessagesWaitingDispatcher implements Dispatcher {
	private final CountDownLatch latch;
	private final Dispatcher wrappedDispatcher;

	public FixedMessagesWaitingDispatcher(int messagesCount, Dispatcher wrappedDispatcher) {
		super();
		this.latch = new CountDownLatch(messagesCount);
		this.wrappedDispatcher = wrappedDispatcher;
	}

	@Override
	public void dispatch(Worker worker) {		
		wrappedDispatcher.dispatch(new WaitableWorker(worker, latch));
	}

	public WorkersWaiter getWaiter() {
		return new WorkersWaiter(latch);
	}
	
	public static final FixedMessagesWaitingDispatcher singleThreadDispatcher(int messagesCount) {
		return new FixedMessagesWaitingDispatcher(messagesCount, worker -> worker.execute());
	}
}
