package io.bincloud.storage.domain.model.resource.file;

import javax.validation.constraints.NotNull;

import io.bincloud.common.domain.model.time.DateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class FileUploading {
	@NonNull
	@EqualsAndHashCode.Include
	private Long resourceId;
	
	@NonNull
	@EqualsAndHashCode.Include
	private String fileId;
	
	@NotNull
	private DateTime uploadingMoment;

	public FileUploading(@NonNull InitialState initialState) {
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
