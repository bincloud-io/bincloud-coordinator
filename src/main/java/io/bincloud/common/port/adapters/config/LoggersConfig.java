package io.bincloud.common.port.adapters.config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.bincloud.common.domain.model.logging.Loggers;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.port.adapters.logging.JULLogger;

@ApplicationScoped
public class LoggersConfig {
	@Inject
	private MessageProcessor messageProcessor;

	@PostConstruct
	public void configureLoggers(@Observes @Initialized(ApplicationScoped.class) Object pointless) {
		Loggers.registry().registerApplicationLogger(new JULLogger(messageProcessor));
	}
}
