package io.bincloud.resources.domain.model.resource.history;

import io.bincloud.resources.domain.model.FileReference;

public interface FileStorage {
	public FileReference createFile(CreateFileInStorage command);
	
	public interface CreateFileInStorage {
		public Long getResourceId();

		public String getFileName();

		public String getMediaType();

		public String getContentDisposition();
	}
}
