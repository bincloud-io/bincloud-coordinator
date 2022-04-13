package io.bcs.fileserver.domain.model.file.content.upload;

import java.util.Optional;

/**
 * This interface describes the {@link ContentUploadSpace} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface ContentUploadSpaceRepository {
  Optional<ContentUploadSpace> findBy(String storageFileName);
}
