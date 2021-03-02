package io.bincloud.storage.resource.accounting.domain.model;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
public class FileUploading {
	@NonNull
	@EqualsAndHashCode.Include
	private final Long resourceId;
	
	@NonNull
	@EqualsAndHashCode.Include
	private final UUID fileId;

	public FileUploading(@NonNull InitialState initialState) {
		super();
		this.resourceId = initialState.getResourceId();
		this.fileId = initialState.getFileId();
	}
	
	public interface InitialState {
		public Long getResourceId();
		public UUID getFileId();
	}
}
