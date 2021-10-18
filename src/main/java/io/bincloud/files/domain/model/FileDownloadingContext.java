package io.bincloud.files.domain.model;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.common.domain.model.io.transfer.TransferingScheduler;
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