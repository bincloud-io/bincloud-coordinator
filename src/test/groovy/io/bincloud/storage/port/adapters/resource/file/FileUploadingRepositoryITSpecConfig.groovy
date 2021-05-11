package io.bincloud.storage.port.adapters.resource.file

import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Produces
import javax.inject.Inject
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import javax.transaction.TransactionManager

import io.bincloud.storage.domain.model.resource.file.FileUploadingRepository
import io.bincloud.storage.port.adapter.resource.JPAFileUploadingRepository

@ApplicationScoped
class FileUploadingRepositoryITSpecConfig {
	@PersistenceContext(name = "central")
	private EntityManager entityManager;
	
	@Inject
	private TransactionManager transactionManager;
	
	@Produces
	public FileUploadingRepository fileUploadingRepository() {
		return new JPAFileUploadingRepository(entityManager, transactionManager)
	}
}
