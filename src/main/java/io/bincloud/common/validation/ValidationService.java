package io.bincloud.common.validation;

public interface ValidationService {
	public <V> void validate(V validatable, Class<?>... groups);
}
