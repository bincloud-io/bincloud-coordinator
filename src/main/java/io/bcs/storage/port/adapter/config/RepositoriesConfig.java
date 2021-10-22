package io.bcs.storage.port.adapter.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.TransactionManager;

import io.bcs.common.domain.model.generator.SequentialGenerator;
import io.bcs.storage.domain.model.FileRevisionRepository;
import io.bcs.storage.port.adapter.ServerContextProvider;
import io.bcs.storage.port.adapter.file.repository.InstanceBasedFileIdGenerator;
import io.bcs.storage.port.adapter.file.repository.JPAFileRepository;

@ApplicationScoped
public class RepositoriesConfig {
	@PersistenceContext(unitName = "central")
	private EntityManager centralContext;
	
	@Inject
	private TransactionManager transactionManager;
	
	@Inject
	private ServerContextProvider contextProvider;
	
	@Produces
	@FileIdGenerator
	public SequentialGenerator<String> fileIdGenerator() {
		return new InstanceBasedFileIdGenerator(contextProvider.getInstanceId());
	}
	
	@Produces
	public FileRevisionRepository fileRepository() {
		return new JPAFileRepository(centralContext, transactionManager);
	}
}
