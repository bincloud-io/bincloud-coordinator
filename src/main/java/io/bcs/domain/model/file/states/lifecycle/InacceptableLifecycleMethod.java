package io.bcs.domain.model.file.states.lifecycle;

import io.bce.domain.errors.ApplicationException;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bcs.domain.model.file.Lifecycle.LifecycleMethod;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InacceptableLifecycleMethod<R> implements LifecycleMethod<R> {
    private final ExceptionProvider exceptionProvider; 
    
    @Override
    public Promise<R> execute() {
        return Promises.rejectedBy(exceptionProvider.createException());
    }

    public interface ExceptionProvider {
        public ApplicationException createException();
    }
}
