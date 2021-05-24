package io.bincloud.common.port.adapters.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.bincloud.common.domain.model.message.MessageInterpolator;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.domain.model.message.templates.MessageTextResolvingTemplate;
import io.bincloud.common.port.adapters.messages.MustacheInterpolator;

@ApplicationScoped
public class MessagesConfig {
	@Produces
	public MessageProcessor messageProcessor() {
		return new MessageProcessor().configure()
				.withTransformation(
						messageTemplate -> new MessageTextResolvingTemplate(messageTemplate, messageInterpolator()))
				.apply();
	}

	public MessageInterpolator messageInterpolator() {
		return new MustacheInterpolator();
	}
}
