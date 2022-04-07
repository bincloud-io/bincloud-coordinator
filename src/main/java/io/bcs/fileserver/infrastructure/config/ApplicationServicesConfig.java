package io.bcs.fileserver.infrastructure.config;

import io.bce.Generator;
import io.bce.domain.EventBus;
import io.bce.validation.ValidationService;
import io.bcs.fileserver.domain.model.content.ContentRepository;
import io.bcs.fileserver.domain.model.file.FileRepository;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.domain.model.storage.StorageDescriptorRepository;
import io.bcs.fileserver.domain.services.ContentAccessService;
import io.bcs.fileserver.domain.services.ContentCleanService;
import io.bcs.fileserver.domain.services.ContentService;
import io.bcs.fileserver.domain.services.FileService;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import io.bcs.fileserver.infrastructure.file.StorageFileNameGenerator;
import io.bcs.fileserver.infrastructure.repositories.JpaContentRepository;
import io.bcs.fileserver.infrastructure.repositories.JpaFileRepository;
import io.bcs.fileserver.infrastructure.repositories.JpaStorageDescriptorRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.TransactionManager;

/**
 * This class configures fileserver application services.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class ApplicationServicesConfig {
  @PersistenceContext(unitName = "central")
  private EntityManager entityManager;

  @Inject
  @SuppressWarnings("cdi-ambiguous-dependency")
  private TransactionManager transactionManager;

  @Inject
  private ValidationService validationService;

  @Inject
  private EventBus eventBus;

  @Inject
  private FileServerConfigurationProperties fileServerConfigurationProperties;

  /**
   * File name generator configuration.
   *
   * @return The file name generator
   */
  @Produces
  public Generator<String> fileNameGenerator() {
    return new StorageFileNameGenerator(fileServerConfigurationProperties);
  }

  /**
   * The file repository configuration.
   *
   * @return The file repository
   */
  @Produces
  public FileRepository fileRepository() {
    return new JpaFileRepository(entityManager, transactionManager,
        fileServerConfigurationProperties);
  }

  /**
   * The storage descriptor repository configuration.
   *
   * @return The file repository
   */
  @Produces
  public StorageDescriptorRepository storageDescriptorRepository() {
    return new JpaStorageDescriptorRepository(entityManager, fileServerConfigurationProperties);
  }

  /**
   * The content repository configuration.
   *
   * @return The storage repository
   */
  @Produces
  public ContentRepository contentRepository() {
    return new JpaContentRepository(entityManager, fileServerConfigurationProperties);
  }

  /**
   * The content access service configuration.
   *
   * @return The content access service
   */
  @Produces
  public ContentAccessService contentAccessService() {
    return new ContentAccessService(storageDescriptorRepository());
  }

  /**
   * The content service configuration.
   *
   * @return The content clean service
   */
  @Produces
  @SuppressWarnings("cdi-ambiguous-dependency")
  public ContentCleanService contentCleanService(FileStorage fileStorage) {
    return new ContentCleanService(fileRepository(), fileStorage);
  }

  /**
   * The content clean service configuration.
   *
   * @return The content service
   */
  @Produces
  public ContentService contentService() {
    return new ContentService(contentRepository(), eventBus);
  }

  /**
   * The file service configuration.
   *
   * @return The file service.
   */
  @Produces
  public FileService fileService() {
    return new FileService(validationService, fileRepository(), fileNameGenerator(),
        fileServerConfigurationProperties, eventBus);
  }
}
