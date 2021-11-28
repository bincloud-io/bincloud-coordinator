package io.bce.domain.usecases;

import io.bce.promises.Promise;

public interface ReplyOnlyUseCase<S> {
    public Promise<S> execute();

    public interface Decorator<S> {
        public Promise<S> execute(ReplyOnlyUseCase<S> original);
    }

    public static <S> ReplyOnlyUseCase<S> decorate(ReplyOnlyUseCase<S> original, Decorator<S> decorator) {
        return () -> decorator.execute(original);
    }
}
