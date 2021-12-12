package io.bcs.fileserver.domain.model.file.lifecycle;

import io.bce.promises.Promise;
import io.bcs.fileserver.domain.model.file.content.ContentUploader;
import io.bcs.fileserver.domain.model.storage.ContentLocator;

/**
 * This interface provides operations, affecting to the file life-cycle.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface Lifecycle {
  LifecycleMethod<FileUploadStatistic> upload(ContentUploader uploader);

  LifecycleMethod<Void> dispose();

  /**
   * This interface describes the interface of life-cycle affecting operation execution.
   *
   * @author Dmitry Mikhaylenko
   *
   * @param <R> The lifecycle method response type
   */
  interface LifecycleMethod<R> {
    Promise<R> execute();
  }

  /**
   * This interface describes the file upload statistic.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  interface FileUploadStatistic {
    ContentLocator getLocator();

    Long getTotalLength();
  }
}