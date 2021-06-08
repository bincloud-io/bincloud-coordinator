package io.bincloud.storage.port.adapter.resource.acl.types;

import java.util.Optional;

import io.bincloud.storage.domain.model.resource.Resource.ResourceDetails;
import io.bincloud.storage.port.adapter.resource.acl.FileName;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResourceDetailsAcl implements ResourceDetails {
	@FileName
	private final String fileName;

	@Override
	public Optional<String> getFileName() {
		return Optional.ofNullable(fileName).map(String::trim);
	}
}
