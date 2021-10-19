package io.bcs.common.domain.model.message.templates;

import java.util.Optional;

import io.bcs.common.domain.model.message.MessageTemplate;
import lombok.EqualsAndHashCode;

public class BundleResolvingTemplate extends RecursiveMessageProcessingTemplate implements MessageTemplate {
	@EqualsAndHashCode.Exclude
	private final BundleResolver bundleResolver;
	
	public BundleResolvingTemplate(MessageTemplate messageTemplate, BundleResolver bundleResolver) {
		super(messageTemplate);
		this.bundleResolver = bundleResolver;
	}
	
	@Override
	public String getText() {
		return bundleResolver.resolveBundle(getMessageTemplate().getText()).orElse(getMessageTemplate().getText());
	}
	
	@Override
	protected Object processMessageTemplateParameter(MessageTemplate messageTemplateParameter) {
		return new BundleResolvingTemplate(messageTemplateParameter, bundleResolver);
	}
	
	public interface BundleResolver {
		/**
		 * Resolve template by template id
		 * 
		 * @param templateId The template id
		 * @return The resolved template optional value
		 */
		public Optional<String> resolveBundle(String templateId);
	}
}
