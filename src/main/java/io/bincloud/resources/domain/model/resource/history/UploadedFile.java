package io.bincloud.resources.domain.model.resource.history;

import java.time.Instant;

import io.bincloud.resources.domain.model.FileReference;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder
@NoArgsConstructor
public class UploadedFile {
	@EqualsAndHashCode.Include
	private Long resourceId;
	@EqualsAndHashCode.Include
	private String filesystemName;
	private AvailabilityStatus availability;
	private Instant distributionStartingDate;
	private Long fileSize;
	

	public UploadedFile(FileReference command) {
		super();
		this.filesystemName = command.getFilesystemName();
		this.resourceId = command.getResourceId();
		this.availability = AvailabilityStatus.NOT_AVAILABLE;
	}

	public void makeAvailable(MakeUploadedFileAvailable command) {
		this.availability = AvailabilityStatus.AVAILABLE;
		this.distributionStartingDate = command.getDistributionStartingDate();
		this.fileSize = command.getFileSize();
	}
	
	public enum AvailabilityStatus {
		AVAILABLE,
		NOT_AVAILABLE;
	}
}
