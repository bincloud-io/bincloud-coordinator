package io.bce.streaming;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.Destination.SourceConnection;
import io.bce.interaction.streaming.Source;
import io.bce.interaction.streaming.Source.DestinationConnection;
import io.bce.interaction.streaming.Stream;
import io.bce.interaction.streaming.Streamer;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import java.util.ArrayList;
import java.util.Collection;
import lombok.RequiredArgsConstructor;

/**
 * This class is simple streamer implementation, which is working in single thread synchronously.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public class DirectStreamer implements Streamer {
  @Override
  public <T> Stream<T> createStream(Source<T> source, Destination<T> destination) {
    return new DirectStream<>(source, destination);
  }

  @RequiredArgsConstructor
  private static class DirectStream<T>
      implements Stream<T>, SourceConnection, DestinationConnection<T> {
    private final Source<T> source;
    private final Destination<T> destination;
    private final Collection<StatusObserver> observers = new ArrayList<>();
    private Long transferredContent = 0L;

    @Override
    public Stream<T> observeStatus(StatusObserver statusObserver) {
      this.observers.add(statusObserver);
      return this;
    }

    @Override
    public Promise<Stat> start() {
      return Promises.of(deferred -> {
        receive();
        deferred.resolve(createStatistic());
      });
    }

    @Override
    public void submit(T data, Integer size) {
      this.transferredContent += size;
      notifyObservers();
      destination.write(this, data, size);
    }

    @Override
    public void receive() {
      source.read(this);
    }

    @Override
    public void complete() {
      source.release();
      destination.release();
    }

    private void notifyObservers() {
      observers.forEach(observer -> observer.onStatusChange(createStatistic()));
    }

    private Stat createStatistic() {
      return new Stat() {
        @Override
        public Long getSize() {
          return transferredContent;
        }
      };
    }
  }
}
