package io.bincloud.storage.port.adapter.file;

import java.util.UUID;

import io.bincloud.storage.domain.model.file.File.IdGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileIdGenerator implements IdGenerator {
	private final String instanceId;
	
	@Override
	public String generateId() {
		return String.format("%s-%s--%s", instanceId, Thread.currentThread().getId(), UUID.randomUUID());
	}
}
