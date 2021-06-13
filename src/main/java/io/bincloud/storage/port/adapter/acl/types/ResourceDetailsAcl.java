package io.bincloud.storage.port.adapter.acl.types;

import java.util.Optional;

import io.bincloud.storage.domain.model.Resource.ResourceDetails;
import io.bincloud.storage.port.adapter.acl.FileName;
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
