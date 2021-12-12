package io.bcs.common.config;

import io.bce.actor.ActorSystem;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.actor.ActorSystemStreamer;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * This class configures the streaming.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class StreamingConfiguration {

  @Inject
  private ActorSystem actorSystem;

  /**
   * The streamer configuration.
   *
   * @return The streamer configuration
   */
  @Produces
  @ApplicationScoped
  public Streamer streamer() {
    return new ActorSystemStreamer(actorSystem);
  }
}
