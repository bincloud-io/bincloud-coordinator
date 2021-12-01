package io.bce.domain.usecases;

public interface RequestOnlyUseCase<Q> {
    public void execute(Q request);

    public interface Decorator<Q> {
        public void execute(Q request, RequestOnlyUseCase<Q> original);
    }

    public static <Q> RequestOnlyUseCase<Q> decorate(RequestOnlyUseCase<Q> original, Decorator<Q> decorator) {
        return request -> decorator.execute(request, original);
    }
}
