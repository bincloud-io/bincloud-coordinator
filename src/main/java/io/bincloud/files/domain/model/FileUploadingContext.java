package io.bincloud.files.domain.model;


import io.bincloud.common.domain.model.io.transfer.CompletionCallback;
import io.bincloud.common.domain.model.io.transfer.DestinationPoint;
import io.bincloud.common.domain.model.io.transfer.SourcePoint;
import io.bincloud.common.domain.model.io.transfer.TransferingScheduler;
import io.bincloud.common.domain.model.io.transfer.Transmitter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FileUploadingContext {
	private final SourcePoint source;
	private final TransferingScheduler scheduler;
	private final FilesystemAccessor fileSystemAccessor;
	private final CompletionCallback completionCallback;
	
	public void upload(String fileName) {
		DestinationPoint destination = fileSystemAccessor.getAccessOnWrite(fileName);
		Transmitter transmitter = scheduler.schedule(source, destination, completionCallback);
		transmitter.start();
	}
}
