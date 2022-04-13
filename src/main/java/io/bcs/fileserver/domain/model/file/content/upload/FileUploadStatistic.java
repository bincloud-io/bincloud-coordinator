package io.bcs.fileserver.domain.model.file.content.upload;

import io.bcs.fileserver.domain.model.storage.ContentLocator;

/**
 * This interface describes the file upload statistic.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface FileUploadStatistic {
  ContentLocator getLocator();

  Long getTotalLength();
}