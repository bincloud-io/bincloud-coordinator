package io.bincloud.storage.domain.model.resource.file;

import io.bincloud.common.domain.model.time.DateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
public class FileUploading {
	@EqualsAndHashCode.Include
	private Long resourceId;
	@EqualsAndHashCode.Include
	private String fileId;
	private DateTime uploadingMoment;

	public FileUploading(InitialState initialState) {
		super();
		this.resourceId = initialState.getResourceId();
		this.fileId = initialState.getFileId();
		this.uploadingMoment = initialState.getUploadingMoment();
	}
	
	public interface InitialState {
		public Long getResourceId();
		public String getFileId();
		public DateTime getUploadingMoment();
	}
}
