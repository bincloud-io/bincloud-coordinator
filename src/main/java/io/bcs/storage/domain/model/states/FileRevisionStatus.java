package io.bcs.storage.domain.model.states;

import io.bcs.storage.domain.model.FileRevision.FileRevisionState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FileRevisionStatus {
	DRAFT(new DraftFileRevisionState()), CREATED(new CreatedFileRevisionState()), DISPOSED(new DisposedFileRevisionState()),
	DISTRIBUTION(new DistributionFileRevisionState());
	
	@Getter
	private final FileRevisionState fileState;
}
