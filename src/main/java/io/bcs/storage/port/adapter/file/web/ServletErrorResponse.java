package io.bcs.storage.port.adapter.file.web;

import java.util.Optional;
import java.util.Properties;

import io.bcs.common.domain.model.error.ApplicationException;
import io.bcs.common.domain.model.error.ErrorDescriptor;
import io.bcs.common.domain.model.message.MessageProcessor;
import io.bcs.common.domain.model.message.MessageTemplate;
import io.bcs.common.domain.model.message.templates.ErrorDescriptorTemplate;

public class ServletErrorResponse extends Properties {
	private static final long serialVersionUID = 8988275144185066868L;
	
	private static final String ERROR_CONTEXT_PROPERTY = "error.context";
	private static final String ERROR_CODE_PROPERTY = "error.code";
	private static final String ERROR_SEVERITY_PROPERTY = "error.severity";
	private static final String ERROR_DESCRIPTION_PROPERTY = "error.description";

	public ServletErrorResponse(ApplicationException error, MessageProcessor messageProcessor) {
		super();
		put(ERROR_CONTEXT_PROPERTY, error.getContext());
		put(ERROR_CODE_PROPERTY, error.getErrorCode().toString());
		put(ERROR_SEVERITY_PROPERTY, error.getSeverity().name());
		put(ERROR_DESCRIPTION_PROPERTY, getErrorDescription(error, messageProcessor).orElse(error.getMessage()));
	}

	private Optional<String> getErrorDescription(ErrorDescriptor errorDescriptor, MessageProcessor messageProcessor) {
		final MessageTemplate errorMessageTemplate = new ErrorDescriptorTemplate(errorDescriptor);
		return Optional.of(errorMessageTemplate).map(template -> messageProcessor.interpolate(template))
				.filter(interpolated -> !errorMessageTemplate.getText().equals(interpolated));
	}
}
