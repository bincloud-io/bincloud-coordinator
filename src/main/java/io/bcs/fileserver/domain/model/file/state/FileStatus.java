package io.bcs.fileserver.domain.model.file.state;

import io.bcs.fileserver.domain.model.file.state.FileState.FileEntityAccessor;

/**
 * This class enumerates the file statuses.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public enum FileStatus {
  DRAFT {
    @Override
    public FileState createState(FileEntityAccessor entityAccessor) {
      return new FileDraftState(entityAccessor);
    }
  },
  DISTRIBUTING {
    @Override
    public FileState createState(FileEntityAccessor entityAccessor) {
      return new FileDistributingState(entityAccessor);
    }
  },
  DISPOSED {
    @Override
    public FileState createState(FileEntityAccessor entityAccessor) {
      return new FileDisposedState(entityAccessor);
    }
  };

  public abstract FileState createState(FileEntityAccessor entityAccessor);
}