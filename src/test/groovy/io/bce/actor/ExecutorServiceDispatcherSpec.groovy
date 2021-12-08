package io.bce.actor

import java.util.concurrent.Executors

import io.bce.actor.ExecutorServiceDispatcher
import io.bce.actor.EventLoop.Dispatcher
import io.bce.actor.EventLoop.Worker
import spock.lang.Specification

class ExecutorServiceDispatcherSpec extends DispatcherSpecification {
  @Override
  protected Dispatcher getDispatcher() {
    return ExecutorServiceDispatcher.createFor(Executors.newFixedThreadPool(4))
  }
}
