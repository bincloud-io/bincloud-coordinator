package io.bce.interaction.pubsub;

/**
 * This interface declares the contract of the pub-sub pattern implementation.
 * This pattern works by the fire and forget communication mechanism. This
 * consists of three primary abstractions - publisher, subscriber and topic.
 * Publisher is the component which puts message to the topic asynchronously and
 * forget about that. In other words publisher doesn't wait response from any
 * component. Subscriber is the component which is subscribed on the topic and
 * receives the messages, published to the last one. The topic is the
 * abstraction which aggregates publishers and subscribers - publishers produce
 * messages to topics which is received by subscribers, subscribed to them
 * 
 * @author Dmitry Mikhaylenko
 *
 * @param <T> The base pub-sub type name
 */
public interface PubSub<T> {
	/**
	 * Get the publisher component
	 * 
	 * @param topic The topic to publishing
	 * @return The publisher object
	 */
	public Publisher<T> getPublisher(Topic topic);

	/**
	 * Subscribe on the topic
	 * 
	 * @param <M>         The message type
	 * @param topic       The topic to publishing
	 * @param messageType The message type
	 * @param subscriber  The subscriber component
	 * @return The subscribtion
	 */
	public <M extends T> Subscribtion subscribeOn(Topic topic, Class<M> messageType, Subscriber<M> subscriber);

	/**
	 * Close the pub-sub channel
	 */
	public void close();

	/**
	 * This interface describes the contract for event publishing
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <M> The message type name
	 */
	public interface Publisher<M> {
		/**
		 * Publish the message to the topic
		 * 
		 * @param message The message object
		 */
		public void publish(M message);
	}

	/**
	 * This interface describes the contract for event listening
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 * @param <M> The message type name
	 */
	public interface Subscriber<M> {
		/**
		 * React on the message
		 * 
		 * @param message The message object
		 */
		public void onMessage(M message);
	}

	/**
	 * This interface describes the contract of subscribtion handling
	 * 
	 * @author Dmitry Mikhaylenko
	 *
	 */
	public interface Subscribtion {
		/**
		 * Unsubscribe from the subscription
		 */
		public void unsubscribe();
	}
}
