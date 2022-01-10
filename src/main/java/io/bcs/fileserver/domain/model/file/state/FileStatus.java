package io.bcs.fileserver.domain.model.file.state;

import io.bce.promises.Promise;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.content.FileContent;
import io.bcs.fileserver.domain.model.file.lifecycle.Lifecycle;
import io.bcs.fileserver.domain.model.storage.ContentFragment;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.Getter;

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

  /**
   * This abstract class describes a file state, which affects to the life-cycle coordination.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public abstract static class FileState {
    @Getter(value = AccessLevel.PROTECTED)
    private final FileEntityAccessor fileEntityAccessor;

    protected FileState(FileEntityAccessor fileEntityAccessor) {
      super();
      this.fileEntityAccessor = fileEntityAccessor;
    }    
    
    public abstract Promise<FileContent> getContentAccess(FileStorage fileStorage,
        Collection<ContentFragment> contentFragments);

    public abstract Lifecycle getLifecycle(FileStorage storage);

    
  }
  
  /**
   * This interface describes an operations over the {@link File} entity inside a state
   * implementations.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface FileEntityAccessor {
    ContentLocator getLocator();

    Long getTotalLength();

    void startFileDistribution(Long contentLength);
  }
}