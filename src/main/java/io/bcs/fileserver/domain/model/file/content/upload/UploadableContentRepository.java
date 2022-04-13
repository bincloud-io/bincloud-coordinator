package io.bcs.fileserver.domain.model.file.content.upload;

import java.util.Optional;

/**
 * This interface describes the {@link UploadableContent} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface UploadableContentRepository {
  Optional<UploadableContent> findBy(String storageFileName);
}
