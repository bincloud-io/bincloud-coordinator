package io.bcs.files.port.adapter.file.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.bcs.common.port.adapters.io.transfer.destinations.CloseOnDisposeStreamDestination;

public class FilesystemStreamDestination extends CloseOnDisposeStreamDestination {
	public FilesystemStreamDestination(File file) throws IOException {
		super(new FileOutputStream(file));
	}	
}
