package io.bcs.application;

import java.util.Collection;

import io.bcs.domain.model.file.Range;

public interface DownloadCommand {
    public String getStorageFileName();

    public Collection<Range> getRanges();
}