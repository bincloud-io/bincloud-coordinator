package io.bcs.fileserver.infrastructure.storage;

/**
 * This interface describes the component which performs the free space management operations over
 * registered local storages.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FilesystemSpaceManager {
  /**
   * Allocate space on the storage for requested length of file.
   *
   * @param mediaType       The file media type
   * @param storageFileName The storage file name
   * @param contentLength   The requested content length
   * @return The storage name on which the space will be allocated for requested file
   */
  String allocateSpace(String mediaType, String storageFileName, Long contentLength);

  /**
   * Release space on the storage for requested file.
   *
   * @param storageName     The file storage name
   * @param storageFileName The file name
   */
  void releaseSpace(String storageName, String storageFileName);

}