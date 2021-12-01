package io.bcs.port.adapters.file;

import java.util.Collection;
import java.util.Optional;

import io.bcs.application.DownloadCommand;
import io.bcs.domain.model.file.Range;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class HttpDownloadCommand implements DownloadCommand {
    private final Optional<String> storageFileName;
    private final Collection<Range> ranges;

    public HttpDownloadCommand(@NonNull Optional<String> storageFileNameParam, @NonNull HttpRanges httpRanges) {
        super();
        this.storageFileName = storageFileNameParam;
        this.ranges = httpRanges.getRanges();
    }
}
