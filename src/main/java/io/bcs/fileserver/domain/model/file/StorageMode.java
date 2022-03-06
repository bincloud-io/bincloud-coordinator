package io.bcs.fileserver.domain.model.file;

/**
 * This class enumerates possible storage modes.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public enum StorageMode {
  /**
   * This mode marks that a file was created on the current distribution point.
   */
  ORIGINAL,
  /**
   * This mode marks that a file was created on another distribution point, but is ready to be
   * replicated.
   */
  MIRROR;
}
