package io.bcs.fileserver.domain.model.file.state;

import io.bcs.fileserver.domain.model.file.File;

/**
 * This class enumerates the file statuses.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public enum FileStatus {
  DRAFT {
    @Override
    public FileState createState(File file) {
      return new FileDraftState(file);
    }
  },
  DISTRIBUTING {
    @Override
    public FileState createState(File entityAccessor) {
      return new FileDistributingState(entityAccessor);
    }
  },
  DISPOSED {
    @Override
    public FileState createState(File entityAccessor) {
      return new FileDisposedState(entityAccessor);
    }
  };

  public abstract FileState createState(File entityAccessor);
}