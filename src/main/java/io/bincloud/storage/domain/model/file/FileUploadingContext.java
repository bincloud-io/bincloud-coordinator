package io.bincloud.storage.domain.model.file;


import io.bincloud.common.io.transfer.CompletionCallback;
import io.bincloud.common.io.transfer.DestinationPoint;
import io.bincloud.common.io.transfer.SourcePoint;
import io.bincloud.common.io.transfer.TransferingScheduler;
import io.bincloud.common.io.transfer.Transmitter;
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
