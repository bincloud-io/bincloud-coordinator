package io.bincloud.storage.port.adapter.file.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.TransactionManager;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.storage.domain.model.file.FileRepository;
import io.bincloud.storage.port.adapter.ServerContextProvider;
import io.bincloud.storage.port.adapter.file.repository.InstanceBasedFileIdGenerator;
import io.bincloud.storage.port.adapter.file.repository.JPAFileRepository;

@ApplicationScoped
public class RepositoriesConfig {
	@PersistenceContext(unitName = "central")
	private EntityManager centralContext;
	
	@Inject
	@SuppressWarnings("cdi-ambiguous-dependency")
	private TransactionManager transactionManager;
	
	@Inject
	private ServerContextProvider contextProvider;
	
	@Produces
	@FileIdGenerator
	public SequentialGenerator<String> fileIdGenerator() {
		return new InstanceBasedFileIdGenerator(contextProvider.getInstanceId());
	}
	
	@Produces
	public FileRepository fileRepository() {
		return new JPAFileRepository(centralContext, transactionManager);
	}
}
