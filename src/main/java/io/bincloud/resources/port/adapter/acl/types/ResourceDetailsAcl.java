package io.bincloud.resources.port.adapter.acl.types;

import java.util.Optional;

import io.bincloud.resources.domain.model.Resource.ResourceDetails;
import io.bincloud.resources.port.adapter.acl.FileName;
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
