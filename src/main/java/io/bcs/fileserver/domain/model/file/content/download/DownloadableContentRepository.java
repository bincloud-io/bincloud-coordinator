package io.bcs.fileserver.domain.model.file.content.download;

import java.util.Optional;

/**
 * This interface describes the {@link DownloadableContent} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface DownloadableContentRepository {
  /**
   * Find downloadable content by storage file name.
   *
   * @param storageFileName The storage file name
   * @return The downloadable content
   */
  Optional<DownloadableContent> findBy(String storageFileName);
}
