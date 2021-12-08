package io.bce.actor;

import io.bce.actor.EventLoop.Dispatcher;
import io.bce.actor.EventLoop.Worker;

public class SingleThreadDispatcher implements Dispatcher {
  @Override
  public void dispatch(Worker worker) {
    worker.execute();
  }
}
