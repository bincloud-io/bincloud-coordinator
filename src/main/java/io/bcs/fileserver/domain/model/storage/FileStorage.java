package io.bcs.fileserver.domain.model.storage;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bcs.fileserver.domain.errors.FileStorageException;
import java.util.Optional;

/**
 * This interface describes the file storage.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileStorage {
  /**
   * Create file with specified length.
   *
   * @param file          The file descriptor
   * @param contentLength The requested content length
   * @return The content locator
   * @throws FileStorageException Throws if something went wrong
   */
  ContentLocator create(FileDescriptor file, Long contentLength) throws FileStorageException;

  Destination<BinaryChunk> getAccessOnWrite(ContentLocator contentLocator)
      throws FileStorageException;

  Source<BinaryChunk> getAccessOnRead(ContentLocator contentLocator, ContentFragment fragment)
      throws FileStorageException;

  void delete(FileDescriptor file) throws FileStorageException;

  /**
   * This interface describes an abstraction describing file.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  interface FileDescriptor {
    /**
     * Get storage file name.
     *
     * @return The storage file name
     */
    String getStorageFileName();

    /**
     * Get storage name.
     *
     * @return The storage name
     */
    Optional<String> getStorageName();

    /**
     * Get media type.
     *
     * @return The media type
     */
    String getMediaType();
  }
}
