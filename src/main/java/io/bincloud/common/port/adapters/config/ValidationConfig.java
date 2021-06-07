package io.bincloud.common.port.adapters.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.validation.Validator;

import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.domain.model.validation.ValidationService;
import io.bincloud.common.port.adapters.validation.JSRBeanValidationService;

@ApplicationScoped
public class ValidationConfig {
	@Inject
	private Validator validator;
	@Inject
	private MessageProcessor messageProcessor;
	
	@Produces
	public ValidationService validationService() {
		return new JSRBeanValidationService(validator, messageProcessor);
	}
}
