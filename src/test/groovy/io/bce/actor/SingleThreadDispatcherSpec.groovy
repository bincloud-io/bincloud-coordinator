package io.bce.actor

import io.bce.actor.SingleThreadDispatcher
import io.bce.actor.EventLoop.Dispatcher
import spock.lang.Specification

class SingleThreadDispatcherSpec extends DispatcherSpecification {

  @Override
  protected Dispatcher getDispatcher() {
    return new SingleThreadDispatcher()
  }
}
