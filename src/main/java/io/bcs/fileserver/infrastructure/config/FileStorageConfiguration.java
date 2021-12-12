package io.bcs.fileserver.infrastructure.config;

import io.bce.Generator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import io.bcs.fileserver.infrastructure.ContentLoadingProperties;
import io.bcs.fileserver.infrastructure.storage.LocalFileSystemStorage;
import java.util.UUID;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * This class configures file storage.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class FileStorageConfiguration {
  @Inject
  private ContentLoadingProperties contentLoadingProperties;

  /**
   * File name generator configuration.
   *
   * @return The file name generator
   */
  @Produces
  public Generator<String> fileNameGenerator() {
    return () -> UUID.randomUUID().toString();
  }

  /**
   * The file storage configuration.
   *
   * @return The file storage
   */
  @Produces
  public FileStorage fileStorage() {
    return new LocalFileSystemStorage(fileNameGenerator(),
        contentLoadingProperties.getStorageName(), contentLoadingProperties.getBaseDirectory(),
        contentLoadingProperties.getBufferSize());
  }
}
