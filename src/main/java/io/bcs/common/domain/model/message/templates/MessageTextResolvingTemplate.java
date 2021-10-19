package io.bcs.common.domain.model.message.templates;

import io.bcs.common.domain.model.message.MessageInterpolator;
import io.bcs.common.domain.model.message.MessageTemplate;

public class MessageTextResolvingTemplate extends RecursiveMessageProcessingTemplate implements MessageTemplate {
	private final MessageInterpolator interpolator;
	
	public MessageTextResolvingTemplate(MessageTemplate messageTemplate, MessageInterpolator interpolator) {
		super(messageTemplate);
		this.interpolator = interpolator;
	}

	@Override
	public String getText() {
		return interpolator.interpolate(new TextMessageTemplate(getMessageTemplate().getText(), getParameters()));
	}

	@Override
	protected Object processMessageTemplateParameter(MessageTemplate messageTemplateParameter) {
		return new MessageTextResolvingTemplate(messageTemplateParameter, interpolator).getText();
	}
}
