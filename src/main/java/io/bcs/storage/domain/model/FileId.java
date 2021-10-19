package io.bcs.storage.domain.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class FileId implements Serializable {
	private static final long serialVersionUID = -649053032229878964L;
	private String filesystemName;
}
