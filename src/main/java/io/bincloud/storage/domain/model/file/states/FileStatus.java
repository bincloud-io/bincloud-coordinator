package io.bincloud.storage.domain.model.file.states;

import io.bincloud.storage.domain.model.file.FileState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FileStatus {
	DRAFT(new DraftState()), CREATED(new CreatedState()), DISPOSED(new DisposedState()),
	DISTRIBUTION(new DistributionState());
	
	@Getter
	private final FileState fileState;
}
