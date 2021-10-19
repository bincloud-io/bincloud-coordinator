package io.bcs.files.domain.model;

import io.bcs.common.domain.model.io.transfer.CompletionCallback;
import io.bcs.common.domain.model.io.transfer.DestinationPoint;
import io.bcs.common.domain.model.io.transfer.TransferingScheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FileDownloadingContext {
	private final DestinationPoint destination;
	private final TransferingScheduler scheduler;
	private final FilesystemAccessor fileSystemAccessor;
	private final CompletionCallback completionCallback;
}