package io.bcs.fileserver.infrastructure.config;

import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.domain.model.storage.descriptor.LocalStorageDescriptorRepository;
import io.bcs.fileserver.infrastructure.FileServerConfigurationProperties;
import io.bcs.fileserver.infrastructure.storage.FilesystemPhysicalFile;
import io.bcs.fileserver.infrastructure.storage.FilesystemSpaceManager;
import io.bcs.fileserver.infrastructure.storage.JdbcFilesystemSpaceManager;
import io.bcs.fileserver.infrastructure.storage.LocalFileSystemStorage;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * This class configures file storage.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class FileStorageConfiguration {
  @Inject
  @SuppressWarnings("cdi-ambiguous-dependency")
  private DataSource dataSource;

  @Inject
  private FileServerConfigurationProperties contentLoadingProperties;

  @Inject
  @SuppressWarnings("cdi-ambiguous-dependency")
  private LocalStorageDescriptorRepository localStorageDescriptorRepository;

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
    return new LocalFileSystemStorage(fileSpaceManager(), localStorageDescriptorRepository,
        FilesystemPhysicalFile.factory(), contentLoadingProperties.getBufferSize());
  }
}
