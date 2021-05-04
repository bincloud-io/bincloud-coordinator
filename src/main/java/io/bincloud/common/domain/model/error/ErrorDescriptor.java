package io.bincloud.common.domain.model.error;

import java.util.Map;

public interface ErrorDescriptor {
	public String getContext();
	public Long getErrorCode();
	public Map<String, Object> getDetails();
}
