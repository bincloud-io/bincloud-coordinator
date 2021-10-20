package io.bcs.common.port.adapters.config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.bce.text.TextProcessor;
import io.bcs.common.domain.model.logging.Loggers;
import io.bcs.common.port.adapters.logging.JULLogger;

@ApplicationScoped
public class LoggersConfig {
	@Inject
	private TextProcessor messageProcessor;

	@PostConstruct
	public void configureLoggers(@Observes @Initialized(ApplicationScoped.class) Object pointless) {
		Loggers.registry().registerApplicationLogger(new JULLogger(messageProcessor));
	}
}
