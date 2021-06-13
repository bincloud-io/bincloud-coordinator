package io.bincloud.files.port.adapters.repository

import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.TransactionManager

import io.bincloud.files.domain.model.FileRepository
import io.bincloud.files.port.adapter.file.repository.JPAFileRepository

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