package io.bincloud.storage.port.adapters.file.repository

import javax.annotation.Resource
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.TransactionManager

import io.bincloud.storage.domain.model.file.FileRepository
import io.bincloud.storage.port.adapter.file.repository.JPAFileRepository

@ApplicationScoped
class FileRepositoryITSpecConfig {
	@PersistenceContext(name = "central")
	private EntityManager entityManager;
	
	@Inject
	private TransactionManager transactionManager;
	
	@Produces
	public FileRepository fileRepository() {
		return new JPAFileRepository(entityManager, transactionManager)
	}

}