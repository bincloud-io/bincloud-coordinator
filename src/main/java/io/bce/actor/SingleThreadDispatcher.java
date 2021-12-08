package io.bce.actor;

import io.bce.actor.EventLoop.Dispatcher;
import io.bce.actor.EventLoop.Worker;

/**
 * This class is the dispatcher implementation, working in the single thread.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class SingleThreadDispatcher implements Dispatcher {
  @Override
  public final void dispatch(Worker worker) {
    worker.execute();
  }
}
