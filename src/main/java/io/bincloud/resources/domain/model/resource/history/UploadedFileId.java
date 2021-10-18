package io.bincloud.resources.domain.model.resource.history;

import java.io.Serializable;

import io.bincloud.resources.domain.model.FileReference;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UploadedFileId implements FileReference, Serializable {
	private static final long serialVersionUID = 3413238431627529956L;
	private Long resourceId;
	private String filesystemName;
}
