package io.bincloud.storage.domain.model;

import java.util.Optional;

public interface ResourceRepository {
	public Optional<Resource> findById(Long id);
	public void save(Resource resource);
}
