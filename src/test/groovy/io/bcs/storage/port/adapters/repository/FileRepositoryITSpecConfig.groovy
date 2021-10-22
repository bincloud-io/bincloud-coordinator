package io.bcs.storage.port.adapters.repository

import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.TransactionManager

import io.bcs.storage.domain.model.FileRevisionRepository
import io.bcs.storage.port.adapter.file.repository.JPAFileRepository

@ApplicationScoped
class FileRepositoryITSpecConfig {
	@PersistenceContext(name = "central")
	private EntityManager entityManager;
	
	@Inject
	private TransactionManager transactionManager;
	
	@Produces
	public FileRevisionRepository fileRepository() {
		return new JPAFileRepository(entityManager, transactionManager)
	}

}