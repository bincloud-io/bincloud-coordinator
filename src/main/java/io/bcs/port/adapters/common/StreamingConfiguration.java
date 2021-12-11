package io.bcs.port.adapters.common;

import io.bce.actor.ActorSystem;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.actor.ActorSystemStreamer;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class StreamingConfiguration {

  @Inject
  private ActorSystem actorSystem;

  @Produces
  @ApplicationScoped
  public Streamer streamer() {
    return new ActorSystemStreamer(actorSystem);
  }
}
