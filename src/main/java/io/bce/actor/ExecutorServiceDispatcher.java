package io.bce.actor;

import java.util.concurrent.ExecutorService;

import io.bce.actor.EventLoop.Dispatcher;
import io.bce.actor.EventLoop.Worker;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "createFor")
public class ExecutorServiceDispatcher implements Dispatcher {
	private final ExecutorService executorService;

	@Override
	public void dispatch(Worker worker) {
		executorService.execute(worker::execute);
	}
}
