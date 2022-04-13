package io.bcs.fileserver.domain.model.file.content.download;

import java.util.Optional;

/**
 * This interface describes the {@link DownloadableContent} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface DownloadableContentRepository {
  Optional<DownloadableContent> findBy(String storageFileName);
}
