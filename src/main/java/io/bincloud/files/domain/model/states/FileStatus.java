package io.bincloud.files.domain.model.states;

import io.bincloud.files.domain.model.FileState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FileStatus {
	DRAFT(new DraftState()), CREATED(new CreatedState()), DISPOSED(new DisposedState()),
	DISTRIBUTION(new DistributionState());
	
	@Getter
	private final FileState fileState;
}
