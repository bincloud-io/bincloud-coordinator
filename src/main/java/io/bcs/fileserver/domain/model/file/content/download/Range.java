package io.bcs.fileserver.domain.model.file.content.download;

import java.util.Optional;

/**
 * This interface describes a requested content range.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface Range {
  public Optional<Long> getStart();

  public Optional<Long> getEnd();
}