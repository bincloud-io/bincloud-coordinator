package io.bce.validation;

public interface ValidationService {
	public <V> ValidationState validate(V validatable);
}
