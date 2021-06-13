package io.bincloud.storage.domain.model.file;

import java.io.Serializable;

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
public class FileUploadId implements Serializable {
	private static final long serialVersionUID = 3413238431627529956L;
	private Long resourceId;
	private String fileId;
}
