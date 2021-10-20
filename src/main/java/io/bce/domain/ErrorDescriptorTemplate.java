package io.bce.domain;

import java.util.Map;

import io.bce.domain.errors.ErrorDescriptor;
import io.bce.text.TextTemplate;
import io.bce.text.TextTemplates;
import lombok.NonNull;

public class ErrorDescriptorTemplate implements TextTemplate {
	private static final String ERROR_TEMPLATE_ID = "ERROR.%s.%s";
	
	private final TextTemplate errorTemplate;

	private ErrorDescriptorTemplate(ErrorDescriptor errorDescriptor) {
		super();
		this.errorTemplate = TextTemplates.createBy(getTextTemplateIdentifier(errorDescriptor), errorDescriptor.getErrorDetails());
	}
	
	@Override
	public String getTemplateText() {
		return errorTemplate.getTemplateText();
	}
	
	@Override
	public Map<String, Object> getParameters() {
		return errorTemplate.getParameters();
	}
	
	@Override
	public TextTemplate transformBy(Transformer transformer) {
		return errorTemplate.transformBy(transformer);
	}

	private String getTextTemplateIdentifier(ErrorDescriptor errorDescriptor) {
		return String.format(ERROR_TEMPLATE_ID, errorDescriptor.getContextId(), errorDescriptor.getErrorCode());
	}
	
	public static final TextTemplate createFor(@NonNull ErrorDescriptor errorDescriptor) {
		return new ErrorDescriptorTemplate(errorDescriptor);
	}
}
