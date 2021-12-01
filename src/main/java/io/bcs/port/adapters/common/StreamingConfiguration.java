package io.bcs.port.adapters.common;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bce.actor.ActorSystem;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.actor.ActorSystemStreamer;

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
