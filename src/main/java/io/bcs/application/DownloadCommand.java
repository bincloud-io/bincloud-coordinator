package io.bcs.application;

import java.util.Collection;
import java.util.Optional;

import io.bcs.domain.model.file.Range;

public interface DownloadCommand {
  public Optional<String> getStorageFileName();

  public Collection<Range> getRanges();
}