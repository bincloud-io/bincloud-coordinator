package io.bincloud.common;

public interface ValidationService {
	public <V> void validate(V validatable, Class<?>... groups);
}
