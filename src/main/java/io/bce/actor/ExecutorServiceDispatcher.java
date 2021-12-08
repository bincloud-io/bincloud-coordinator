package io.bce.actor;

import io.bce.actor.EventLoop.Dispatcher;
import io.bce.actor.EventLoop.Worker;
import java.util.concurrent.ExecutorService;
import lombok.RequiredArgsConstructor;

/**
 * This class is the dispatcher, based on the executor service.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor(staticName = "createFor")
public class ExecutorServiceDispatcher implements Dispatcher {
  private final ExecutorService executorService;

  @Override
  public final void dispatch(Worker worker) {
    executorService.execute(worker::execute);
  }
}
