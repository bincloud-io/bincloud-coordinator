package io.bcs.fileserver.infrastructure.config;

import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.domain.model.storage.descriptor.StorageDescriptorRepository;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import io.bcs.fileserver.infrastructure.repositories.JpaStorageDescriptorRepository;
import io.bcs.fileserver.infrastructure.storage.FilesystemPhysicalFile;
import io.bcs.fileserver.infrastructure.storage.FilesystemSpaceManager;
import io.bcs.fileserver.infrastructure.storage.JdbcFilesystemSpaceManager;
import io.bcs.fileserver.infrastructure.storage.LocalFileSystemStorage;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

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

  @Resource(lookup = "java:/jdbc/BC_CENTRAL")
  private DataSource dataSource;

  @Inject
  private FileServerConfigurationProperties contentLoadingProperties;

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
}
