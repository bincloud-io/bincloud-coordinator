package io.bincloud.common.domain.model.message.templates;

import java.util.Map;

import io.bincloud.common.domain.model.error.ErrorDescriptor;
import io.bincloud.common.domain.model.message.MessageTemplate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorDescriptorTemplate implements MessageTemplate {
	private static final String ERROR_TEMPLATE_ID = "ERROR.%s.%s";
	
	private final ErrorDescriptor errorDescriptor;

	@Override
	public String getText() {
		return String.format(ERROR_TEMPLATE_ID, errorDescriptor.getContext(), errorDescriptor.getErrorCode());
	}

	@Override
	public Map<String, Object> getParameters() {
		return errorDescriptor.getDetails();
	}
}
