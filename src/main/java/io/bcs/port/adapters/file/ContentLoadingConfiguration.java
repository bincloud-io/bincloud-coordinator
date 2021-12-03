package io.bcs.port.adapters.file;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.TransactionManager;

import io.bce.Generator;
import io.bce.validation.DefaultValidationContext;
import io.bce.validation.ValidationService;
import io.bcs.application.ContentService;
import io.bcs.application.FileService;
import io.bcs.domain.model.file.FileRepository;
import io.bcs.domain.model.file.FileStorage;
import io.bcs.port.adapters.ContentLoadingProperties;

@ApplicationScoped
public class ContentLoadingConfiguration {
    @PersistenceContext(unitName = "central")
    private EntityManager entityManager;

    @Inject
    @SuppressWarnings("cdi-ambiguous-dependency")
    private TransactionManager transactionManager;

    @Inject
    private ContentLoadingProperties contentLoadingProperties;

    @Produces
    public ValidationService validationService() {
        return DefaultValidationContext.createValidationService();
    }

    @Produces
    public FileRepository fileRepository() {
        return new JpaFileRepository(entityManager, transactionManager);
    }

    @Produces
    public Generator<String> fileNameGenerator() {
        return () -> UUID.randomUUID().toString();
    }

    @Produces
    public FileStorage fileStorage() {
        return new LocalFileSystemStorage(fileNameGenerator(), contentLoadingProperties.getStorageName(),
                contentLoadingProperties.getBaseDirectory(), contentLoadingProperties.getBufferSize());
    }
    
    @Produces
    public ContentService contentService() {
        return new ContentService(fileRepository(), fileStorage());
    }

    @Produces
    public FileService fileStorageService() {
        return new FileService(validationService(), fileRepository(), fileStorage());
    }
}
