package io.bcs.files.port.adapters.repository

import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.TransactionManager

import io.bcs.files.domain.model.FileRepository
import io.bcs.files.port.adapter.file.repository.JPAFileRepository

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