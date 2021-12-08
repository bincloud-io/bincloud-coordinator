package io.bce.interaction.pubsub.actor;

import io.bce.actor.ActorAddress;
import io.bce.interaction.pubsub.Topic;
import io.bce.interaction.pubsub.actor.MessagingCoordinatorActor.Subscribtions;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This class describes the "unsubscribe" command.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public final class Unsubscribe {
  private final Topic topic;
  private final ActorAddress actorAddress;

  void unsubscribe(Subscribtions subscribtions) {
    subscribtions.unsubscribe(topic, actorAddress);
  }
}