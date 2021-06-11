package io.bincloud.storage.domain.model.resource.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceHasBeenRemoved {
	private Long resourceId;
}
