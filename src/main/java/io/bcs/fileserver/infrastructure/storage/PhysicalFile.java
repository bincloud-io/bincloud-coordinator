package io.bcs.fileserver.infrastructure.storage;

import io.bce.interaction.streaming.binary.BinaryDestination;
import io.bce.interaction.streaming.binary.BinarySource;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import java.io.IOException;

/**
 * This interface represents a physical file on a filesystem.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface PhysicalFile {
  /**
   * Create new physical file.
   *
   * @throws IOException Throws if something went wrong during creation
   */
  void create() throws IOException;

  /**
   * Open physical file for read.
   *
   * @param offset     The read offset
   * @param limit      The read limit
   * @param bufferSize The read buffer size
   * @return The file read input stream
   * @throws IOException Throws if something went wrong during read process
   */
  BinarySource openForRead(Long offset, Long limit, Integer bufferSize) throws IOException;

  /**
   * Open physical file for write.
   *
   * @return The file read input stream
   * @throws IOException Throws if something went wrong during read process
   */
  BinaryDestination openForWrite() throws IOException;

  /**
   * Delete file from file system.
   */
  void delete();

  /**
   * This interface declares the contract for physical file creation.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  interface Factory {
    /**
     * Create new physical file.
     *
     * @param baseDirectory  The storage base directory
     * @param contentLocator The content locator
     * @return The created physical file
     */
    PhysicalFile create(String baseDirectory, ContentLocator contentLocator);
  }
}
