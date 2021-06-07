package io.bincloud.storage.port.adapter.file.config;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bincloud.storage.domain.model.file.FilesystemAccessor;
import io.bincloud.storage.port.adapter.ServerContextProvider;
import io.bincloud.storage.port.adapter.file.filesystem.BlockedFileSystemAccessor;

@ApplicationScoped
public class FilesystemConfig {
	@Inject
	private ServerContextProvider contextProvider;

	@Produces
	public FilesystemAccessor filesystemAccessor() {
		return new BlockedFileSystemAccessor(contextProvider.getRootFilesFolderPath(),
				contextProvider.getIOBufferSize());
	}
}
