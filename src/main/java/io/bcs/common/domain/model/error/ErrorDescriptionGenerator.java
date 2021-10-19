package io.bcs.common.domain.model.error;

import java.util.Optional;

import io.bcs.common.domain.model.message.MessageProcessor;
import io.bcs.common.domain.model.message.MessageTemplate;
import io.bcs.common.domain.model.message.templates.ErrorDescriptorTemplate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorDescriptionGenerator {
	private final ApplicationException applicationError;
	private final MessageProcessor messageProcessor;
		
	public String generateDescription() {
		final MessageTemplate messageTemplate = new ErrorDescriptorTemplate(applicationError);
		return Optional.of(messageTemplate).map(template -> messageProcessor.interpolate(template))
				.filter(message -> !message.equals(messageTemplate.getText()))
				.orElse(applicationError.getMessage());
	}
	
	public static ErrorDescriptionGenerator of(MessageProcessor messageProcessor, ApplicationException applicationException) {
		return new ErrorDescriptionGenerator(applicationException, messageProcessor);
	}
}
