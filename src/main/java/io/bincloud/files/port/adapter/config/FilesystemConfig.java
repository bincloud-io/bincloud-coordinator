package io.bincloud.files.port.adapter.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bincloud.common.domain.model.io.transfer.TransferingScheduler;
import io.bincloud.common.port.adapters.io.transfer.transmitter.DirectTransferingScheduler;
import io.bincloud.files.domain.model.FilesystemAccessor;
import io.bincloud.files.port.adapter.file.filesystem.BlockedFileSystemAccessor;
import io.bincloud.resources.port.adapter.ServerContextProvider;

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
