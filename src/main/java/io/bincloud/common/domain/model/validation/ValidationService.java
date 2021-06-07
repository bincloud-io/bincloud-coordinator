package io.bincloud.common.domain.model.validation;

public interface ValidationService {
	public <V> ValidationState validate(V validatable, Class<?>... groups);
}
