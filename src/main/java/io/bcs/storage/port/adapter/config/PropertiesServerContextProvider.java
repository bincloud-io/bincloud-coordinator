package io.bcs.storage.port.adapter.config;

import java.util.Properties;

import io.bcs.storage.port.adapter.ServerContextProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PropertiesServerContextProvider implements ServerContextProvider {
	private static final String DEFAULT_INSTANCE_ID = "I00000000";
	private static final String DEFAULT_ROOT_FOLDER_PATH = "~/bincloud";
	private static final String DEFAULT_IO_BUFFER_SIZE = "1024";
	private static final String IO_BUFFER_SIZE_PROPERTY_NAME = "context.io.buffer.size";
	private static final String ROOT_FILES_FOLDER_PATH_PROPERTY_NAME = "context.root.folder.path";
	private static final String INSTANCE_ID_PATH_PROPERTY_NAME = "context.instance.id";
	
	private final Properties settings;

	@Override
	public int getIOBufferSize() {
		return Integer.valueOf(settings.getProperty(IO_BUFFER_SIZE_PROPERTY_NAME, DEFAULT_IO_BUFFER_SIZE));
	}

	@Override
	public String getRootFilesFolderPath() {
		return settings.getProperty(ROOT_FILES_FOLDER_PATH_PROPERTY_NAME, DEFAULT_ROOT_FOLDER_PATH);
	}

	@Override
	public String getInstanceId() {
		return settings.getProperty(INSTANCE_ID_PATH_PROPERTY_NAME, DEFAULT_INSTANCE_ID);
	}
}
