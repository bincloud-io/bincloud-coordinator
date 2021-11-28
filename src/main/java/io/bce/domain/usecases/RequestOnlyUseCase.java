package io.bce.domain.usecases;

import io.bce.promises.Promise;

public interface RequestOnlyUseCase<Q> {
    public Promise<Void> execute(Q request);

    public interface Decorator<Q> {
        public Promise<Void> execute(Q request, RequestOnlyUseCase<Q> original);
    }

    public static <Q> RequestOnlyUseCase<Q> decorate(RequestOnlyUseCase<Q> original, Decorator<Q> decorator) {
        return request -> decorator.execute(request, original);
    }
}
