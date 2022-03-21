package io.bcs.fileserver.domain.model.file;

import java.util.Collection;

/**
 * This interface describes the {@link FileLocation} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileLocationRepository {
  void save(Collection<FileLocation> fileLocations);
}
