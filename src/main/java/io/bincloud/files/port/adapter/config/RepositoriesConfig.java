package io.bincloud.files.port.adapter.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.TransactionManager;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.files.domain.model.FileRepository;
import io.bincloud.files.port.adapter.file.repository.InstanceBasedFileIdGenerator;
import io.bincloud.files.port.adapter.file.repository.JPAFileRepository;
import io.bincloud.resources.port.adapter.ServerContextProvider;

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
