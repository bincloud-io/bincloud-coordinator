package io.bincloud.files.domain.model;

import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.common.domain.model.io.transfer.TransferingScheduler;
import io.bincloud.common.domain.model.io.transfer.Transmitter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileDownloadingContext {
	private final DestinationPoint destination;
	private final TransferingScheduler scheduler;
	private final FilesystemAccessor fileSystemAccessor;
	private final CompletionCallback completionCallback;
	
	public void download(String fileName, Long offset, Long size) {
		SourcePoint source = fileSystemAccessor.getAccessOnRead(fileName, offset, size);
		Transmitter transmitter = scheduler.schedule(source, destination, completionCallback);
		transmitter.start();
	}
}