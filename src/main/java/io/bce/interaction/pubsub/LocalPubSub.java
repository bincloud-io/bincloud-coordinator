package io.bce.interaction.pubsub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.bce.Locker;
import lombok.RequiredArgsConstructor;

public class LocalPubSub<T> implements PubSub<T> {
	private final LocalChannel channel = new LocalChannel();

	@Override
	public Publisher<T> getPublisher(Topic topic) {
		return new Publisher<T>() {
			@Override
			public void publish(T message) {
				channel.putMessage(topic, message);
			}
		};
	}

	@Override
	public <M extends T> Subscribtion subscribeOn(Topic topic, Class<M> messageType, Subscriber<M> subscriber) {
		return channel.subscribe(topic, messageType, subscriber);
	}

	@Override
	public void close() {
		channel.unsubscribeAll();
	}

	private class LocalChannel {
		private final Locker locker = new Locker();
		private final Map<Topic, List<LocalSubscribtion>> subscribtions = new HashMap<>();

		public void putMessage(Topic topic, T message) {
			locker.executeCriticalSection(() -> {
				getSubscribtionsFor(topic).forEach(subscribtion -> subscribtion.notifySubscriber(message));
			});
		}

		@SuppressWarnings("unchecked")
		public <M extends T> Subscribtion subscribe(Topic topic, Class<M> messageType, Subscriber<M> subscriber) {
			LocalSubscribtion subscribtion = new LocalSubscribtion(topic, messageType, (Subscriber<T>) subscriber);
			locker.executeCriticalSection(() -> {
				registerTopicIfUnregistered(topic);
				registerSubscribtion(topic, subscribtion);
			});
			return subscribtion;
		}
		
		public void unsubscribeAll() {
			locker.executeCriticalSection(() -> {
				subscribtions.values().stream().flatMap(List::stream)
						.forEach(subscribtion -> subscribtion.unsubscribe());
				subscribtions.clear();
			});
		}


		private List<LocalSubscribtion> getSubscribtionsFor(Topic topic) {
			return subscribtions.getOrDefault(topic, new ArrayList<>());
		}
		
		private void registerTopicIfUnregistered(Topic topic) {
			if (!subscribtions.containsKey(topic)) {
				subscribtions.put(topic, new ArrayList<>());
			}
		}
		
		private void registerSubscribtion(Topic topic, LocalSubscribtion subscribtion) {
			List<LocalSubscribtion> subscribtionsList = subscribtions.get(topic);
			subscribtionsList.add(subscribtion);
		}

		@RequiredArgsConstructor
		private class LocalSubscribtion implements Subscribtion {
			private final Topic topic;
			private final Class<? extends T> messageType;
			private final Subscriber<T> subscriber;

			@Override
			public void unsubscribe() {
				getSubscribtionsFor(topic).remove(this);
			}

			public void notifySubscriber(T message) {
				if (messageType.isInstance(message)) {
					subscriber.onMessage(message);
				}
			}
		}
	}
}
