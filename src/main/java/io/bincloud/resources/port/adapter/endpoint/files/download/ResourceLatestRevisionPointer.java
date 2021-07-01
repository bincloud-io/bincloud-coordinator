package io.bincloud.resources.port.adapter.endpoint.files.download;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

public class ResourceLatestRevisionPointer extends ResourceRevisionPointer {
	public ResourceLatestRevisionPointer(HttpServletRequest request) {
		super(request);
	}

	@Override
	public Optional<String> getFileId() {
		return Optional.empty();
	}
}
