package io.bcs.fileserver.infrastructure.config;

import io.bce.domain.EventBus;
import io.bcs.fileserver.domain.Constants;
import io.bcs.fileserver.domain.events.FileDistributionHasBeenStarted;
import io.bcs.fileserver.domain.model.file.FileLocationRepository;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.domain.model.storage.descriptor.StorageDescriptorRepository;
import io.bcs.fileserver.domain.services.listeners.DistributingFileLocationHandler;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import io.bcs.fileserver.infrastructure.repositories.JpaFileLocationRepository;
import io.bcs.fileserver.infrastructure.repositories.JpaStorageDescriptorRepository;
import io.bcs.fileserver.infrastructure.storage.FilesystemPhysicalFile;
import io.bcs.fileserver.infrastructure.storage.FilesystemSpaceManager;
import io.bcs.fileserver.infrastructure.storage.JdbcFilesystemSpaceManager;
import io.bcs.fileserver.infrastructure.storage.LocalFileSystemStorage;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

/**
 * This class configures file storage.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class FileStorageConfiguration {
  @PersistenceContext(unitName = "central")
  private EntityManager entityManager;

  @Inject
  @SuppressWarnings("cdi-ambiguous-dependency")
  private TransactionManager transactionManager;

  @Resource(lookup = "java:/jdbc/BC_CENTRAL")
  private DataSource dataSource;

  @Inject
  private FileServerConfigurationProperties contentLoadingProperties;

  @Inject
  private EventBus eventBus;

  /**
   * The file location repository configuration.
   *
   * @return The file location repository
   */
  @Produces
  public FileLocationRepository fileLocationRepository() {
    return new JpaFileLocationRepository(entityManager, transactionManager);
  }

  /**
   * The storage descriptor repository configuration.
   *
   * @return The storage descriptor repository
   */
  @Produces
  public StorageDescriptorRepository localStorageDescriptorRepository() {
    return new JpaStorageDescriptorRepository(entityManager);
  }

  /**
   * The file system space manager configuration.
   *
   * @return The file system space manager
   */
  @Produces
  public FilesystemSpaceManager fileSpaceManager() {
    return new JdbcFilesystemSpaceManager(dataSource);
  }

  /**
   * The file storage configuration.
   *
   * @return The file storage
   */
  @Produces
  public FileStorage fileStorage() {
    return new LocalFileSystemStorage(fileSpaceManager(), localStorageDescriptorRepository(),
        FilesystemPhysicalFile.factory(), contentLoadingProperties.getBufferSize());
  }

  /**
   * Register domain events.
   *
   * @param init The CDI context event
   */
  public void configureEvents(@Observes @Initialized(ApplicationScoped.class) Object init) {
    eventBus.subscribeOn(Constants.CONTEXT, FileDistributionHasBeenStarted.EVENT_TYPE,
        new DistributingFileLocationHandler(fileLocationRepository()));
  }
}
