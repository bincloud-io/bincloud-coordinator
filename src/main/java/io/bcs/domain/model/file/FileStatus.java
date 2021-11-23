package io.bcs.domain.model.file;

import io.bcs.domain.model.file.FileState.FileEntityAccessor;
import io.bcs.domain.model.file.FileState.FileStateFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FileStatus {
    DRAFT(FileDraftState::new), DISTRIBUTING(FileDistributingState::new), DISPOSED(FileDisposedState::new);

    private final FileStateFactory stateFactory;

    public FileState createState(FileEntityAccessor entityAccessor) {
        return stateFactory.create(entityAccessor);
    }
}