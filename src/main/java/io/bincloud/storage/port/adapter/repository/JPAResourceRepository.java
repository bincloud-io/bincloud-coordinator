package io.bincloud.storage.port.adapter.repository;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;

import io.bincloud.storage.domain.model.Resource;
import io.bincloud.storage.domain.model.ResourceRepository;
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
	@SneakyThrows
	public void remove(Long id) {
		transactionManager.begin();
		Optional.ofNullable(entityManager.getReference(Resource.class, id)).ifPresent(entityManager::remove);
		transactionManager.commit();
	}
}
