package io.bcs.storage.domain.model.states;

import io.bcs.storage.domain.model.FileState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FileStatus {
	DRAFT(new DraftState()), CREATED(new CreatedState()), DISPOSED(new DisposedState()),
	DISTRIBUTION(new DistributionState());
	
	@Getter
	private final FileState fileState;
}
