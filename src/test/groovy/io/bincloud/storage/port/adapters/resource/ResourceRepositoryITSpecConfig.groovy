package io.bincloud.storage.port.adapters.resource

import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.TransactionManager

import io.bincloud.storage.domain.model.resource.ResourceRepository
import io.bincloud.storage.port.adapter.resource.repository.JPAResourceRepository

@ApplicationScoped
class ResourceRepositoryITSpecConfig {
	@PersistenceContext(name = "central")
	private EntityManager entityManager;
	
	@Inject
	private TransactionManager transactionManager;
	
	@Produces
	public ResourceRepository fileRepository() {
		return new JPAResourceRepository(entityManager, transactionManager)
	}
}
