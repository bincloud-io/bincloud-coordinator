package io.bincloud.storage.domain.model.file;

import java.time.Instant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
public class FileUpload {
	@EqualsAndHashCode.Include
	private Long resourceId;
	@EqualsAndHashCode.Include
	private String fileId;
	private Instant uploadMoment;

	public FileUpload(InitialState initialState) {
		super();
		this.fileId = initialState.getFileId();
		this.resourceId = initialState.getResourceId();
		this.uploadMoment = initialState.getUploadMoment();
	}

	public interface InitialState {
		public Long getResourceId();

		public String getFileId();

		public Instant getUploadMoment();
	}
}
