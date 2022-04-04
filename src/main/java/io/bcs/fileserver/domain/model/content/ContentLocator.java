package io.bcs.fileserver.domain.model.content;

/**
 * This interface describes the file location inside a physical storage.
 *
 * @author Dmitry Mikhayelnko
 *
 */
public interface ContentLocator {
  public String getStorageName();

  public String getStorageFileName();
}
