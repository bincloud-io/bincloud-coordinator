package io.bcs.port.adapters.common;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.bce.actor.ActorSystem;
import io.bce.actor.Actors;
import io.bce.actor.CorrelationKeyGenerator;
import io.bce.actor.EventLoop.Dispatcher;
import io.bcs.port.adapters.ActorSystemProperties;
import io.bce.actor.ExecutorServiceDispatcher;

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
        return Actors.create(configurer -> {
            return configurer.withCorrelationKeyGenerator(correlationKeyGenerator()).withDispatcher(dispatcher())
                    .configure();
        });
    }

    @PostConstruct
    public void initializeActorSystem() {
        ActorSystem actorSystem = actorSystem();
        actorSystem.start();
    }

    @PreDestroy
    public void shutdownActorSystem() {
        ActorSystem actorSystem = actorSystem();
        actorSystem.shutdown();
    }

    private int processorsCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    private int maxThreadCount() {
        return THREADS_PER_CORE * processorsCount();
    }
}
