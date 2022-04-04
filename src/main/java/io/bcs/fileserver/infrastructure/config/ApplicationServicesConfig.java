package io.bcs.fileserver.infrastructure.config;

import io.bce.Generator;
import io.bce.domain.EventBus;
import io.bce.validation.ValidationService;
import io.bcs.fileserver.domain.model.content.FileStorage;
import io.bcs.fileserver.domain.model.file.FileRepository;
import io.bcs.fileserver.domain.services.ContentCleanService;
import io.bcs.fileserver.domain.services.FileService;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import io.bcs.fileserver.infrastructure.file.StorageFileNameGenerator;
import io.bcs.fileserver.infrastructure.repositories.JpaFileRepository;
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
  private FileStorage fileStorage;

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
   * The content service configuration.
   *
   * @return The content service
   */
  @Produces
  @SuppressWarnings("cdi-ambiguous-dependency")
  public ContentCleanService contentService() {
    return new ContentCleanService(fileRepository(), fileStorage);
  }

  /**
   * The file service configuration.
   *
   * @return The file service.
   */
  @Produces
  @SuppressWarnings("cdi-ambiguous-dependency")
  public FileService fileService() {
    return new FileService(validationService, fileRepository(), fileNameGenerator(),
        fileServerConfigurationProperties, eventBus);
  }
}
