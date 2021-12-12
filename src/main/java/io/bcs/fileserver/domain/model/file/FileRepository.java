package io.bcs.fileserver.domain.model.file;

import java.util.Optional;

/**
 * This interface describes the {@link File} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileRepository {
  public Optional<File> findById(String storageFileName);

  public void save(File file);
}
