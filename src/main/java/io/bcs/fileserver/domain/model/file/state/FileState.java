package io.bcs.fileserver.domain.model.file.state;

import io.bce.promises.Promise;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.Range;
import io.bcs.fileserver.domain.model.file.content.ContentDownloader;
import io.bcs.fileserver.domain.model.file.content.ContentUploader;
import io.bcs.fileserver.domain.model.file.content.FileUploadStatistic;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import java.util.Collection;

/**
 * This abstract class describes a file state, which affects to the life-cycle coordination.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public abstract class FileState {
  private final FileEntityAccessor fileEntityAccessor;

  protected FileState(FileEntityAccessor fileEntityAccessor) {
    super();
    this.fileEntityAccessor = fileEntityAccessor;
  }
  
  /**
   * Upload file content.
   *
   * @param fileStorage     The file storage
   * @param contentUploader The content uploader
   * @return The upload process completion promise
   */
  public abstract Promise<FileUploadStatistic> uploadContent(FileStorage fileStorage,
      ContentUploader contentUploader);

  /**
   * Download file content.
   *
   * @param fileStorage       The file storage
   * @param contentDownloader The content downloader
   * @param ranges            The file ranges
   * @return The downloading process completion promise
   */
  public abstract Promise<Void> downloadContent(FileStorage fileStorage,
      ContentDownloader contentDownloader, Collection<Range> ranges);
  
  protected final ContentLocator getContentLocator() {
    return fileEntityAccessor.getLocator();
  }
  
  protected final Long getTotalLength() {
    return fileEntityAccessor.getTotalLength();
  }
  
  protected final void startFileDistribution(Long contentLength) {
    fileEntityAccessor.startFileDistribution(contentLength);
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
    
    public void startFileDistribution(Long contentLength);
  }
}