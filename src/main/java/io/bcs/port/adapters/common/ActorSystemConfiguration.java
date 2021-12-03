package io.bcs.port.adapters.common;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bce.actor.ActorSystem;
import io.bce.actor.Actors;
import io.bce.actor.CorrelationKeyGenerator;
import io.bce.actor.EventLoop.Dispatcher;
import io.bce.actor.ExecutorServiceDispatcher;
import io.bcs.port.adapters.ActorSystemProperties;

@ApplicationScoped
public class ActorSystemConfiguration {
    public static final int THREADS_PER_CORE = 10;

    @Inject
    private ActorSystemProperties actorSystemProperties;

    @Produces
    public Dispatcher dispatcher() {
        ThreadPoolExecutor executorService = new ThreadPoolExecutor(processorsCount(), maxThreadCount(), 10,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        return ExecutorServiceDispatcher.createFor(executorService);
    }

    @Produces
    public CorrelationKeyGenerator correlationKeyGenerator() {
        return new CorrelationKeyGenerator(actorSystemProperties.getInstanceId());
    }

    @Produces
    @ApplicationScoped
    public ActorSystem actorSystem() {
        ActorSystem actorSystem = Actors.create(configurer -> {
            return configurer.withCorrelationKeyGenerator(correlationKeyGenerator()).withDispatcher(dispatcher())
                    .configure();
        });
        actorSystem.start();
        return actorSystem;
    }

    private int processorsCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    private int maxThreadCount() {
        return THREADS_PER_CORE * processorsCount();
    }
}
