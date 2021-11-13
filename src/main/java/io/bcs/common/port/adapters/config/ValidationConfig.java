package io.bcs.common.port.adapters.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.validation.Validator;

import io.bce.validation.JSRBeanValidationService;
import io.bce.validation.ValidationService;

@ApplicationScoped
public class ValidationConfig {
	@Inject
	private Validator validator;
//	@Inject
//	private TextProcessor textProcessor;
	
	@Produces
	public ValidationService validationService() {
		return new JSRBeanValidationService(validator);
	}
}
