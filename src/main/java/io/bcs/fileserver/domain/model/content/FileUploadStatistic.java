package io.bcs.fileserver.domain.model.content;

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