package io.bincloud.common.port.adapters.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bincloud.common.domain.model.message.MessageInterpolator;
import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.domain.model.message.MessageTransformer;
import io.bincloud.common.domain.model.message.templates.BundleResolvingTemplate;
import io.bincloud.common.domain.model.message.templates.BundleResolvingTemplate.BundleResolver;
import io.bincloud.common.domain.model.message.templates.MessageTextResolvingTemplate;
import io.bincloud.common.port.adapters.messages.LocaleProvider;
import io.bincloud.common.port.adapters.messages.MustacheInterpolator;
import io.bincloud.common.port.adapters.messages.ResourceBundleResolver;

@ApplicationScoped
public class MessagesConfig {
	@Inject
	private LocaleProvider localeProvider;
	
	@Produces
	public MessageProcessor messageProcessor() {
		return new MessageProcessor().configure()
				.withTransformation(bundleResolverTransformer())
				.withTransformation(messageInterpolatorTransformer())
				.apply();
	}
	
	private MessageTransformer bundleResolverTransformer() {
		final BundleResolver bundleResolver = bundleResolver();
		return messageTemplate -> new BundleResolvingTemplate(messageTemplate, bundleResolver);
	}
	
	private MessageTransformer messageInterpolatorTransformer() {
		final MessageInterpolator messageInterpolator = messageInterpolator();
		return messageTemplate -> new MessageTextResolvingTemplate(messageTemplate, messageInterpolator);
	}
	
	private MessageInterpolator messageInterpolator() {
		return new MustacheInterpolator();
	}
	
	private BundleResolver bundleResolver() {
		return new ResourceBundleResolver(localeProvider)
			.withResourceBundle("i18n/messages");
	}
}
