package io.bce.actor

import io.bce.actor.EventLoop.Dispatcher
import io.bce.actor.EventLoop.Worker

import java.util.concurrent.CountDownLatch

import io.bce.actor.FixedMessagesWaitingDispatcher
import io.bce.actor.WaitableWorker
import io.bce.actor.WorkersWaiter
import spock.lang.Specification

abstract class DispatcherSpecification extends Specification {
  private Worker firstWorker
  private Worker secondWorker
  private Worker thirdWorker
  private Worker forthWorker

  def setup() {
    this.firstWorker = Mock(Worker)
    this.secondWorker = Mock(Worker)
    this.thirdWorker = Mock(Worker)
    this.forthWorker = Mock(Worker)
  }

  def "Scenario: dispatch requests to worker"() {
    CountDownLatch dispatchersAwaitLatch = new CountDownLatch(4)
    WorkersWaiter waiter = new WorkersWaiter(dispatchersAwaitLatch)

    given: "The dispatcher"
    Dispatcher dispatcher = getDispatcher()

    and: "The workers"
    Collection<Worker> workers = [
      firstWorker,
      secondWorker,
      thirdWorker,
      forthWorker
    ]

    when: "The workers is passed to the dispatcher"
    dispatchAllWorkers(dispatchersAwaitLatch, dispatcher, workers)
    waiter.await()

    then: "Each worker should be executed"
    1 * firstWorker.execute()
    1 * secondWorker.execute()
    1 * thirdWorker.execute()
    1 * forthWorker.execute()
  }

  private void dispatchAllWorkers(CountDownLatch dispatcherAwaitLatch, Dispatcher dispatcher, Collection<Worker> workers) {
    workers.stream().forEach({Worker worker -> dispatcher.dispatch(new WaitableWorker(worker, dispatcherAwaitLatch))})
  }

  protected abstract Dispatcher getDispatcher();
}
