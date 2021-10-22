package io.bce.interaction;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.bce.promises.Deferred;
import lombok.NonNull;

public class AsyncResolverProxy<T> implements Deferred<T> {
	private final Deferred<T> original;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	
	public AsyncResolverProxy(@NonNull Deferred<T> original) {
		super();
		this.original = original;
	}

	@Override
	public void resolve(T response) {
		executeAsync(() -> original.resolve(response));	
	}

	@Override
	public void reject(Throwable error) {
		executeAsync(() -> original.reject(error));
	}
	
	private void executeAsync(Runnable task) {
		executorService.execute(() -> {
			task.run();
			executorService.shutdownNow();
		});
	}

}
