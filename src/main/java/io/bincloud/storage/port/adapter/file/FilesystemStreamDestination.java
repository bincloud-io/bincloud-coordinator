package io.bincloud.storage.port.adapter.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.bincloud.common.port.adapters.io.transfer.destinations.StreamDestination;

public class FilesystemStreamDestination extends StreamDestination {
	public FilesystemStreamDestination(File file) throws IOException {
		super(new FileOutputStream(file));
	}	
}
