package io.bce.domain.usecases;

public interface ReplyOnlyUseCase<S> {
    public S execute();

    public interface Decorator<S> {
        public S execute(ReplyOnlyUseCase<S> original);
    }

    public static <S> ReplyOnlyUseCase<S> decorate(ReplyOnlyUseCase<S> original, Decorator<S> decorator) {
        return () -> decorator.execute(original);
    }
}
