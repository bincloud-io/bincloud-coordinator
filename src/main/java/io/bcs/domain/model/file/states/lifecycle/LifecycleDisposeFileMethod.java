package io.bcs.domain.model.file.states.lifecycle;

import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.domain.model.FileStorage;
import io.bcs.domain.model.file.FileState.FileEntityAccessor;
import io.bcs.domain.model.file.Lifecycle.LifecycleMethod;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LifecycleDisposeFileMethod implements LifecycleMethod<Void> {
    private final FileEntityAccessor entityAccessor;
    private final FileStorage storage;
    
    @Override
    public Promise<Void> execute() {
        return Promises.of(deferred -> {
            storage.delete(entityAccessor.getLocator());
            entityAccessor.dispose();
            deferred.resolve(null);
        });
    }
}