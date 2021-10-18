package io.bincloud.resources.port.adapter.endpoint.resource;

import java.util.Optional;

import io.bincloud.resources.domain.model.resource.CreateResource;
import io.bincloud.resources.port.adapter.acl.FileName;
import io.bincloud.storage.port.adapter.resource.endpoint.management.CreateNewResourceRqType;

public class WSCreateNewResourceCommand implements CreateResource {
	@FileName
	private final String fileName;

	public WSCreateNewResourceCommand(CreateNewResourceRqType request) {
		super();
		this.fileName = request.getFileName();
	}
	
	@Override
	public Optional<String> getFileName() {
		return Optional.ofNullable(fileName).map(String::trim);
	}	
}
