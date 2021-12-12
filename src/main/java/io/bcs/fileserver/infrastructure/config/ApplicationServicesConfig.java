package io.bcs.fileserver.infrastructure.config;

import io.bce.validation.ValidationService;
import io.bcs.fileserver.domain.model.file.FileManagement;
import io.bcs.fileserver.domain.model.file.FileRepository;
import io.bcs.fileserver.domain.model.file.metadata.FileMetadataRepository;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.domain.services.ContentService;
import io.bcs.fileserver.domain.services.FileService;
import io.bcs.fileserver.infrastructure.file.content.FileMetadataProvider;
import io.bcs.fileserver.infrastructure.repositories.JpaFileMetadataRepository;
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

  /**
   * The file repository configuration.
   *
   * @return The file repository
   */
  @Produces
  public FileRepository fileRepository() {
    return new JpaFileRepository(entityManager, transactionManager);
  }

  /**
   * The file metadata repository configuration.
   *
   * @return The file metadata repository
   */
  @Produces
  public FileMetadataRepository fileMetadataRepository() {
    return new JpaFileMetadataRepository(entityManager);
  }

  /**
   * The file metadata provider configuration.
   *
   * @return The file metadata provider
   */
  @Produces
  public FileMetadataProvider fileMetadataProvider() {
    return contentLocator -> {
      return fileMetadataRepository().findById(contentLocator.getStorageFileName()).get();
    };
  }

  /**
   * The content service configuration.
   *
   * @return The content service
   */
  @Produces
  public ContentService contentService() {
    return new ContentService(fileRepository(), fileStorage);
  }

  /**
   * The file service configuration.
   *
   * @return The file service.
   */
  @Produces
  public FileManagement fileService() {
    return new FileService(validationService, fileRepository(), fileStorage);
  }
}
