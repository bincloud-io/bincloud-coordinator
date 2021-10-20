package io.bce.domain.errors;

import java.util.Optional;

import io.bce.domain.ErrorDescriptorTemplate;
import io.bce.text.TextProcessor;
import io.bce.text.TextTemplate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorDescriptionGenerator {
	private final ErrorDescriptor applicationError;
	private final TextProcessor textProcessor;
		
	public String generateDescription() {
		final TextTemplate textTemplate = ErrorDescriptorTemplate.createFor(applicationError);
		return Optional.of(textTemplate).map(template -> textProcessor.interpolate(template))
				.filter(message -> !message.equals(textTemplate.getTemplateText()))
				.orElse(applicationError.toString());
	}
	
	public static ErrorDescriptionGenerator of(TextProcessor messageProcessor, ErrorDescriptor errorDescriptor) {
		return new ErrorDescriptionGenerator(errorDescriptor, messageProcessor);
	}
}
