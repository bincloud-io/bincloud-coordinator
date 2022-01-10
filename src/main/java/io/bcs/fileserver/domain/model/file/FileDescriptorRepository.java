package io.bcs.fileserver.domain.model.file;

import java.util.Optional;

/**
 * This interface describess the {@link FileDescriptor} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileDescriptorRepository {
  public Optional<FileDescriptor> findById(String storageFileName);

  public void save(FileDescriptor fileDescriptor);
}
