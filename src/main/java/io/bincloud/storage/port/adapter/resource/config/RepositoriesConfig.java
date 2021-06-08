package io.bincloud.storage.port.adapter.resource.config;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import io.bincloud.common.domain.model.generator.SequentialGenerator;
import io.bincloud.common.port.adapters.generators.JDBCSequenceGenerator;
import io.bincloud.storage.domain.model.resource.ResourceRepository;
import io.bincloud.storage.domain.model.resource.file.FileUploadingRepository;
import io.bincloud.storage.port.adapter.resource.repository.JPAFileUploadingRepository;
import io.bincloud.storage.port.adapter.resource.repository.JPAResourceRepository;

@ApplicationScoped
public class RepositoriesConfig {
	@PersistenceContext(unitName = "central")
	private EntityManager centralContext;
	
	@Resource(lookup = "java:/jdbc/BC_CENTRAL")
	private DataSource dataSource;
	
	@Inject
	@SuppressWarnings("cdi-ambiguous-dependency")
	private TransactionManager transactionManager;
	
	@Produces
	public ResourceRepository resourceRepository() {
		return new JPAResourceRepository(centralContext, transactionManager);
	}
	
	@Produces
	public FileUploadingRepository fileUploadingRepository() {
		return new JPAFileUploadingRepository(centralContext, transactionManager);
	}
	
	@Produces
	@Named("resourceIdGenerator")
	public SequentialGenerator<Long> resourceIdGenerator() {
		return new JDBCSequenceGenerator(dataSource, "RESOURCE_SEQUENCE");
	}
}
