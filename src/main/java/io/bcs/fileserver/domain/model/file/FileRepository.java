package io.bcs.fileserver.domain.model.file;

import java.util.Collection;
import java.util.Optional;

/**
 * This interface describes the {@link File} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileRepository {
  /**
   * Find all files, loca on the current file name.
   *
   * @param storageFileName The storage file name
   * @return The file on the current distribution point
   */
  Optional<File> findLocatedOnCurrentPoint(String storageFileName);

  /**
   * Find all replicated files on all distribution points except current.
   *
   * @param storageFileName The storage file name
   * @return Found replicated files
   */
  Collection<File> findAllReplicatedFiles(String storageFileName);

  /**
   * Find all not removed disposed files.
   *
   * @return The not removed disposed files
   */
  Collection<File> findNotRemovedDisposedFiles();

  /**
   * Save file entity.
   *
   * @param file The file
   */
  void save(File file);
}
