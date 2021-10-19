package io.bcs.files.port.adapter.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.TransactionManager;

import io.bcs.common.domain.model.generator.SequentialGenerator;
import io.bcs.files.domain.model.FileRepository;
import io.bcs.files.port.adapter.ServerContextProvider;
import io.bcs.files.port.adapter.file.repository.InstanceBasedFileIdGenerator;
import io.bcs.files.port.adapter.file.repository.JPAFileRepository;

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
	public FileRepository fileRepository() {
		return new JPAFileRepository(centralContext, transactionManager);
	}
}
