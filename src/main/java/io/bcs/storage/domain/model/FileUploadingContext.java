package io.bcs.storage.domain.model;


import io.bcs.common.domain.model.io.transfer.CompletionCallback;
import io.bcs.common.domain.model.io.transfer.SourcePoint;
import io.bcs.common.domain.model.io.transfer.TransferingScheduler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FileUploadingContext {
	private final Long contentSize;
	private final SourcePoint source;
	private final TransferingScheduler scheduler;
	private final FilesystemAccessor fileSystemAccessor;
	private final CompletionCallback completionCallback;
}
