package io.bce.interaction.pubsub.actor;

import java.util.concurrent.atomic.AtomicLong;

import io.bce.actor.Actor;
import io.bce.actor.ActorAddress;
import io.bce.actor.ActorName;
import io.bce.actor.ActorSystem;
import io.bce.actor.Message;
import io.bce.interaction.pubsub.PubSub;
import io.bce.interaction.pubsub.Topic;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class ActorSystemPubSub<T> implements PubSub<T> {
	private final AtomicLong sequence = new AtomicLong(0L);

	private final ActorSystem actorSystem;
	private final ActorAddress coordinatorAddress;

	private ActorSystemPubSub(@NonNull ActorSystem actorSystem, @NonNull ActorAddress coordinatorAddress) {
		super();
		this.actorSystem = actorSystem;
		this.coordinatorAddress = coordinatorAddress;
	}

	/**
	 * The actor system factory
	 * 
	 * @param actorSystem The actor system
	 * @return The pub-sub system factory
	 */
	public static final Factory factory(@NonNull ActorSystem actorSystem) {
		return new Factory() {
			@Override
			public <T> PubSub<T> createPubSub(@NonNull ActorName coordinatorName) {
				ActorAddress coordinatorAddress = actorSystem.actorOf(coordinatorName,
						MessagingCoordinatorActor.factory());
				return new ActorSystemPubSub<>(actorSystem, coordinatorAddress);
			}
		};
	}

	@Override
	public Publisher<T> getPublisher(@NonNull Topic topic) {
		return new ActorSystemPublisher(topic);
	}

	@Override
	public <M extends T> Subscribtion subscribeOn(@NonNull Topic topic, @NonNull Class<M> messageType,
			@NonNull Subscriber<M> subscriber) {
		ActorAddress subscriberAddress = actorSystem.actorOf(generateSubscriberName(topic),
				context -> new SubscriberActor<M>(context, messageType, topic, subscriber));
		return new ActorSystemSubscribtion(subscriberAddress, topic);
	}

	public void close() {
		actorSystem.tell(Message.createFor(coordinatorAddress, new Shutdown()));
	}

	private ActorName generateSubscriberName(Topic topic) {
		return ActorName.wrap(String.format("SUBSCRIBER--%s--%s", topic, sequence.incrementAndGet()));
	}

	@RequiredArgsConstructor
	private class ActorSystemPublisher implements Publisher<T> {
		@NonNull
		private final Topic topic;

		@Override
		public void publish(T message) {
			actorSystem.tell(Message.createFor(coordinatorAddress, new Publish(topic, message)));
		}
	}

	@RequiredArgsConstructor
	private class ActorSystemSubscribtion implements Subscribtion {
		@NonNull
		private final ActorAddress subscriberAddress;
		@NonNull
		private final Topic topic;

		@Override
		public void unsubscribe() {
			actorSystem.tell(Message.createFor(coordinatorAddress, new Unsubscribe(topic, subscriberAddress)));
		}
	}

	private class SubscriberActor<M> extends Actor<M> {
		private final Topic topic;
		private final Class<M> messageType;
		private final Subscriber<M> subscriber;

		public SubscriberActor(@NonNull Context context, @NonNull Class<M> messageType, @NonNull Topic topic,
				@NonNull Subscriber<M> subscriber) {
			super(context);
			this.messageType = messageType;
			this.subscriber = subscriber;
			this.topic = topic;
		}

		@Override
		protected void receive(@NonNull Message<M> message) throws Throwable {
			message.whenIsMatchedTo(messageType, body -> {
				subscriber.onMessage(body);
			});
		}

		@Override
		protected void beforeStart() {
			tell(Message.createFor(coordinatorAddress, new Subscribe(topic, self())));
		}
	}
	
	/**
	 * This interface declares the contract of creating pub-sub channel, bound to
	 * the concrete topic with specified message type
	 * 
	 * @author Dmitry Mikaylenko
	 *
	 */
	public interface Factory {
		/**
		 * Create the pub-sub channel
		 * 
		 * @param <T>             The message type name
		 * @param coordinatorName The coordinator actor name
		 * @return The pub-sub channel
		 */
		public <T> PubSub<T> createPubSub(ActorName coordinatorName);
	}
}
