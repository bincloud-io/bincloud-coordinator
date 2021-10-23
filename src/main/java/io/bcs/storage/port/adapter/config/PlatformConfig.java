package io.bcs.storage.port.adapter.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bce.actor.ActorSystem;
import io.bce.actor.Actors;
import io.bce.actor.CorrelationKeyGenerator;
import io.bce.interaction.streaming.Streamer;
import io.bce.interaction.streaming.actor.ActorSystemStreamer;
import io.bcs.storage.port.adapter.ServerContextProvider;

@ApplicationScoped
public class PlatformConfig {
	@Inject
	private ServerContextProvider serverContextProvider;
	
	@Produces
	public ActorSystem actorSystem() {
		ActorSystem actorSystem = Actors.create(configuration -> {
			return configuration
				.withCorrelationKeyGenerator(new CorrelationKeyGenerator(serverContextProvider.getInstanceId()))
				.configure();
		});
		actorSystem.start();
		return actorSystem;
	}
	
	@Produces
	public Streamer dataStreamer(ActorSystem actorSystem) {
		return new ActorSystemStreamer(actorSystem);
	}
}
