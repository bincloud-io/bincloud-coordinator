package io.bincloud.resources.port.adapters.repository

import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.TransactionManager

import io.bincloud.resources.domain.model.file.FileUploadsRepository
import io.bincloud.resources.port.adapter.repository.JPAFileUploadsRepository

@ApplicationScoped
class FileUploadsRepositoryITSpecConfig {
	@PersistenceContext(name = "central")
	private EntityManager entityManager;
	
	@Inject
	private TransactionManager transactionManager;
	
	@Produces
	public FileUploadsRepository fileUploadingRepository() {
		return new JPAFileUploadsRepository(entityManager, transactionManager)
	}
}
