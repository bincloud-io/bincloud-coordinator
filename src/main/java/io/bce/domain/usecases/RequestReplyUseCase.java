package io.bce.domain.usecases;

import io.bce.promises.Promise;

public interface RequestReplyUseCase<Q, S> {
    public Promise<S> execute(Q request);
    
    public interface Decorator<Q, S> {
        public Promise<S> execute(Q request, RequestReplyUseCase<Q, S> original);
    }
    
    public static <Q, S> RequestReplyUseCase<Q, S> decorate(RequestReplyUseCase<Q, S> original, Decorator<Q, S> decorator) {
        return request -> {
            return decorator.execute(request, original);
        };
    }
}
