package io.bcs.storage.domain.model;

import java.io.Serializable;

import io.bce.validation.DefaultValidationContext;
import io.bce.validation.ValidationContext;
import io.bce.validation.ValidationContext.Validatable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class FileId implements Validatable, Serializable {
	private static final long serialVersionUID = -649053032229878964L;
	private String filesystemName;
	
	@Override
	public DefaultValidationContext validate(ValidationContext context) {
		return null;
	}
	
	
}
