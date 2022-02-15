package io.bcs.common.config;

import io.bce.actor.ActorName;
import io.bce.actor.ActorSystem;
import io.bce.domain.EventBus;
import io.bce.domain.buses.PubSubEventBus;
import io.bce.interaction.pubsub.PubSub;
import io.bce.interaction.pubsub.actor.ActorSystemPubSub;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * This class configures local event bus.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class EventBusConfiguration {
  private static final ActorName PUB_SUB_ACTOR_NAME = ActorName.wrap("LOCAL_EVENT_TRANSPORT");

  @Inject
  private ActorSystem actorSystem;

  @Produces
  @ApplicationScoped
  public PubSub<Object> eventBusPubSub() {
    return ActorSystemPubSub.factory(actorSystem).createPubSub(PUB_SUB_ACTOR_NAME);
  }

  @Produces
  @ApplicationScoped
  public EventBus eventBus() {
    return PubSubEventBus.factory(eventBusPubSub()).createEventBus();
  }
}
