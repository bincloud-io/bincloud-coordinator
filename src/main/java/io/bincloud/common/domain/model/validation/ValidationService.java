package io.bincloud.common.domain.model.validation;

public interface ValidationService {
	public <V> void validate(V validatable, Class<?>... groups);
}
