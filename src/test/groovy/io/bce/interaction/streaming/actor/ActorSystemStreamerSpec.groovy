package io.bce.interaction.streaming.actor

import java.util.concurrent.CountDownLatch

import io.bce.actor.ActorSystem
import io.bce.actor.Actors
import io.bce.actor.CorrelationKey
import io.bce.actor.FixedMessagesWaitingDispatcher
import io.bce.actor.Actors.SystemConfigurer
import io.bce.actor.EventLoop.Dispatcher
import io.bce.interaction.streaming.Destination
import io.bce.interaction.streaming.Source
import io.bce.interaction.streaming.Stream
import io.bce.interaction.streaming.Streamer
import io.bce.interaction.streaming.Source.DestinationConnection
import io.bce.interaction.streaming.Stream.Stat
import io.bce.promises.Promise
import io.bce.promises.Promises
import spock.lang.Specification

class ActorSystemStreamerSpec extends Specification {
	private static final CorrelationKey GENERATED_KEY = CorrelationKey.wrap("CORRELATED_KEY")
	
	def "Scenario: transfer data from source to destination"() {
		Stat resultStatistic
		CountDownLatch latch = new CountDownLatch(1)
		given: "The actor system"
		FixedMessagesWaitingDispatcher dispatcher = FixedMessagesWaitingDispatcher.singleThreadDispatcher(13)
		ActorSystem actorSystem = createActorSystem(dispatcher)
		actorSystem.start()
		
		and: "The source which sends the sequence of integer: 1->2->3->4->5"
		Source<Long> source = Mock(Source) {
			read(_) >> { DestinationConnection<Long> connection ->
				connection.submit(1L, 1)
			} >> { DestinationConnection<Long> connection ->
				connection.submit(2L, 1)
			} >> { DestinationConnection<Long> connection ->
				connection.submit(3L, 1)
			} >> { DestinationConnection<Long> connection ->
				connection.submit(4L, 1)
			} >> { DestinationConnection<Long> connection ->
				connection.submit(5L, 1)
			} >> { DestinationConnection<Long> connection ->
				connection.complete()
			}
		}
		
		and: "The destination receives data"
		Destination<Stat> destination = Mock(Destination)
		
		and: "The actor system streamer"
		Streamer streamer = new ActorSystemStreamer(actorSystem)
		
		when: "The stream is created"
		Stream<Integer> stream = streamer.createStream(source, new Destination() {
			@Override
			public void write(SourceConnection connection, Object data, Integer size) {
				destination.write(connection, data, size)
				connection.receive()
			}

			@Override
			public void release() {
				destination.release()
			}
		})
		
		and: "The streaming is started"
		Promise<Long> promise = stream.start();
		
		and: "The response handler subscribed to the promise"
		promise.then({value -> 
			resultStatistic = value
			latch.countDown()
		})
		dispatcher.getWaiter().await()
		latch.await()
		
		then: "The sequence should be transferred in 1->2->3->4->5 order"
		1 * destination.write(_, 1L, 1)
		1 * destination.write(_, 2L, 1)
		1 * destination.write(_, 3L, 1)
		1 * destination.write(_, 4L, 1)
		1 * destination.write(_, 5L, 1)
//		
		and: "The source and destination should be released"
		1 * source.release()
		1 * destination.release()
		
		and: "The response handler should be resolved with size = 7"
		resultStatistic.getSize() == 5L
		
		cleanup:
		actorSystem.shutdown()
	}
	
	def "Scenario: error on read"() {
		Throwable resultError
		UnsupportedOperationException thrownError = new UnsupportedOperationException()
		CountDownLatch latch = new CountDownLatch(1)
		given: "The actor system"
		FixedMessagesWaitingDispatcher dispatcher = FixedMessagesWaitingDispatcher.singleThreadDispatcher(3)
		ActorSystem actorSystem = createActorSystem(dispatcher)
		actorSystem.start()
		
		and: "The source which sends the sequence of integer: 1->2->3->4->5"
		Source<Long> source = Mock(Source) {
			read(_) >> { DestinationConnection<Long> connection ->
				throw thrownError
			}
		}
		
		and: "The destination receives data"
		Destination<Long> destination = Mock(Destination)
		
		and: "The actor system streamer"
		Streamer streamer = new ActorSystemStreamer(actorSystem)
		
		when: "The stream is created"
		Stream<Integer> stream = streamer.createStream(source, destination)
		
		and: "The streaming is started"
		Promise<Long> promise = stream.start();
		
		and: "The error handler subscribed to the promise"
		promise.error({error -> 
			resultError = error
			latch.countDown()
		})
		dispatcher.getWaiter().await()
		latch.await()
		
		then: "The promise is completed with error, thrown by source"
		resultError.is(thrownError) == true
		
		and: "The source and destinatio should be released"
		1 * source.release()
		1 * destination.release()
		
		cleanup:
		actorSystem.shutdown()
	}
	
	def "Scenario: error on write"() {
		Throwable resultError
		UnsupportedOperationException thrownError = new UnsupportedOperationException()
		CountDownLatch latch = new CountDownLatch(1)
		given: "The actor system"
		FixedMessagesWaitingDispatcher dispatcher = FixedMessagesWaitingDispatcher.singleThreadDispatcher(4)
		ActorSystem actorSystem = createActorSystem(dispatcher)
		actorSystem.start()
		
		and: "The source which sends the sequence of integer: 1->2->3->4->5"
		Source<Long> source = Mock(Source) {
			read(_) >> { DestinationConnection<Long> connection ->
				connection.submit(1L, 1)
			}
		}
		
		and: "The destination receives data"
		Destination<Long> destination = Mock(Destination)
		
		and: "The actor system streamer"
		Streamer streamer = new ActorSystemStreamer(actorSystem)
		
		when: "The stream is created"
		Stream<Integer> stream = streamer.createStream(source, new Destination() {
			@Override
			public void write(SourceConnection connection, Object data, Integer size) {
				throw thrownError	
			}

			@Override
			public void release() {
				destination.release()	
			}
		})
		
		and: "The streaming is started"
		Promise<Long> promise = stream.start();
		
		and: "The error handler subscribed to the promise"
		promise.error({error ->
			resultError = error
			latch.countDown()
		})
		dispatcher.getWaiter().await()
		latch.await()
		
		then: "The promise is completed with error, thrown by source"
		resultError.is(thrownError) == true
		
		and: "The source and destinatio should be released"
		1 * source.release()
		1 * destination.release()
		
		cleanup:
		actorSystem.shutdown()
	}
	
	private ActorSystem createActorSystem(Dispatcher dispatcher) {
		return Actors.create({SystemConfigurer configurer ->
			return configurer
					.withDispatcher(dispatcher)
					.withCorrelationKeyGenerator({GENERATED_KEY})
					.configure()
		})
	}
	
}
