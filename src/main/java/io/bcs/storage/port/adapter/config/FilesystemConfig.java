package io.bcs.storage.port.adapter.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bcs.common.domain.model.io.transfer.TransferingScheduler;
import io.bcs.common.port.adapters.io.transfer.transmitter.DirectTransferingScheduler;
import io.bcs.storage.domain.model.FilesystemAccessor;
import io.bcs.storage.port.adapter.ServerContextProvider;
import io.bcs.storage.port.adapter.file.filesystem.BlockedFileSystemAccessor;

@ApplicationScoped
public class FilesystemConfig {
	@Inject
	private ServerContextProvider contextProvider;

	@Produces
	public FilesystemAccessor filesystemAccessor() {
		return new BlockedFileSystemAccessor(contextProvider.getRootFilesFolderPath(),
				contextProvider.getIOBufferSize());
	}
	
	@Produces
	public TransferingScheduler transferingScheduler() {
		return new DirectTransferingScheduler();
	}
}
