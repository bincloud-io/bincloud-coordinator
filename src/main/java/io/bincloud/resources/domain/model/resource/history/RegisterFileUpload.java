package io.bincloud.resources.domain.model.resource.history;

public interface RegisterFileUpload {
	public Long getResourceId();

	public String getFileName();

	public String getMediaType();

	public String getContentDisposition();
}
