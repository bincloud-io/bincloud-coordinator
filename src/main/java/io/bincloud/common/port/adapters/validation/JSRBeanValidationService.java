package io.bincloud.common.port.adapters.validation;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.ConstraintViolation;
import javax.validation.Path.Node;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;

import io.bincloud.common.domain.model.message.MessageProcessor;
import io.bincloud.common.domain.model.message.MessageTemplate;
import io.bincloud.common.domain.model.message.templates.TextMessageTemplate;
import io.bincloud.common.domain.model.validation.ValidationService;
import io.bincloud.common.domain.model.validation.ValidationState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JSRBeanValidationService implements ValidationService {
	private static final String NULL_STRING_JOINT_VALUE = "null";
	private final Validator beanValidator;
	private final MessageProcessor messageProcessor;

	@Override
	public <V> ValidationState validate(V validatable, Class<?>... groups) {
		ValidationState validationResult = new ValidationState();
		for (ConstraintViolation<V> violation : beanValidator.validate(validatable, groups)) {
			validationResult = appendError(validationResult, violation);
		}
		return validationResult;
	}

	private <V> ValidationState appendError(ValidationState currentState, ConstraintViolation<V> violation) {
		Optional<String> optionalConstraintGroup = getConstraintViolationGroup(violation);
		if (optionalConstraintGroup.isPresent()) {
			return appendGroupedError(currentState, optionalConstraintGroup.get(), violation);
		}
		return appendUngroupedError(currentState, violation);
	}

	private <V> ValidationState appendUngroupedError(ValidationState currentState, ConstraintViolation<V> violation) {
		return currentState.withUngrouped(createConstraintDescription(violation));
	}

	private <V> ValidationState appendGroupedError(ValidationState currentState, String group,
			ConstraintViolation<V> violation) {
		return currentState.withGrouped(group, createConstraintDescription(violation));
	}

	private <V> String createConstraintDescription(ConstraintViolation<V> violation) {
		ConstraintDescriptor<?> descriptor = violation.getConstraintDescriptor();
		MessageTemplate descriptionTemplate = new TextMessageTemplate(violation.getMessageTemplate(),
				descriptor.getAttributes());
		return messageProcessor.interpolate(descriptionTemplate);
	}

	private <V> Optional<String> getConstraintViolationGroup(ConstraintViolation<V> violation) {
		String groupValue = StreamSupport.stream(violation.getPropertyPath().spliterator(), false).map(Node::getName)
				.collect(Collectors.joining());
		return Optional.ofNullable(groupValue).filter(group -> !NULL_STRING_JOINT_VALUE.equals(group));
	}
}
