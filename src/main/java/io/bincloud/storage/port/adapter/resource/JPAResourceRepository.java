package io.bincloud.storage.port.adapter.resource;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

import io.bincloud.storage.domain.model.resource.Resource;
import io.bincloud.storage.domain.model.resource.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class JPAResourceRepository implements ResourceRepository {
	private final EntityManager entityManager;
	private final TransactionManager transactionManager;

	@Override
	public boolean isExists(Long id) {
		return findById(id).isPresent();
	}

	@Override
	public Optional<Resource> findById(Long id) {
		return Optional.ofNullable(entityManager.find(Resource.class, id));
	}

	@Override
	@SneakyThrows
	public void save(Resource resource) {
		transactionManager.begin();
		entityManager.merge(resource);
		transactionManager.commit();
	}

	@Override
	public void remove(Long id) {
		Optional.ofNullable(entityManager.getReference(Resource.class, id)).ifPresent(this::removeResource);
	}

	@SneakyThrows
	private void removeResource(final Resource resource) {
		transactionManager.begin();
		entityManager.remove(resource);
		transactionManager.commit();
	}
}
