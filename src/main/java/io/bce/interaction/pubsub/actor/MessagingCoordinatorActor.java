package io.bce.interaction.pubsub.actor;

import io.bce.actor.Actor;
import io.bce.actor.ActorAddress;
import io.bce.actor.Message;
import io.bce.interaction.pubsub.Topic;
import io.bce.interaction.pubsub.actor.Publish.PublishCommand;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;

class MessagingCoordinatorActor extends Actor<Object> {
  private final Subscribtions subscribtions = new Subscribtions();

  private MessagingCoordinatorActor(@NonNull Context context) {
    super(context);
  }

  public static final Factory<Object> factory() {
    return MessagingCoordinatorActor::new;
  }

  @Override
  protected void receive(Message<Object> message) throws Throwable {
    message.whenIsMatchedTo(Subscribe.class, command -> {
      command.subscribe(subscribtions);
    });

    message.whenIsMatchedTo(Unsubscribe.class, command -> {
      command.unsubscribe(subscribtions);
    });

    message.whenIsMatchedTo(Publish.class, command -> {
      command.publish(message, publishHandler());
    });

    message.whenIsMatchedTo(Shutdown.class, command -> {
      stop(self());
    });
  }

  @Override
  protected void afterStop() {
    subscribtions.unsubscribeAll();
  }

  private PublishCommand publishHandler() {
    return (topic, message) -> {
      subscribtions.getSubscribers(topic).forEach(actorAddress -> {
        tell(message.withDestination(actorAddress));
      });
    };
  }

  class Subscribtions {
    private final Map<Topic, Set<ActorAddress>> subscribtions = new HashMap<>();

    public void subscribe(Topic eventType, ActorAddress actorAddress) {
      getSubscribers(eventType).add(actorAddress);
    }

    public void unsubscribe(Topic eventType, ActorAddress actorAddress) {
      getSubscribers(eventType).remove(actorAddress);
      stop(actorAddress);
    }

    public void unsubscribeAll() {
      subscribtions.entrySet().forEach(entry -> {
        entry.getValue().forEach(value -> unsubscribe(entry.getKey(), value));
      });
    }

    public Set<ActorAddress> getSubscribers(Topic topic) {
      Set<ActorAddress> subscribers = subscribtions.getOrDefault(topic, new HashSet<>());
      subscribtions.put(topic, subscribers);
      return subscribers;
    }
  }
}
