package io.bincloud.resources.port.adapter.endpoint.files.download;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

public class ResourceSpecifiedRevisionPointer extends ResourceRevisionPointer {
	public ResourceSpecifiedRevisionPointer(HttpServletRequest request) {
		super(request);
	}

	@Override
	public Optional<String> getFileId() {
		return Optional.ofNullable(getRequest().getParameter("fileId"));
	}
}
