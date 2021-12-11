package io.bcs.domain.model.file.states.lifecycle;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.text.TextTemplates;
import io.bcs.domain.model.file.ContentLocator;
import io.bcs.domain.model.file.FileStorage;
import io.bcs.domain.model.file.FileState.FileEntityAccessor;
import io.bcs.domain.model.file.Lifecycle.LifecycleMethod;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LifecycleDisposeFileMethod implements LifecycleMethod<Void> {
  private static final ApplicationLogger log =
      Loggers.applicationLogger(LifecycleDisposeFileMethod.class);

  private final FileEntityAccessor entityAccessor;
  private final FileStorage storage;

  @Override
  public Promise<Void> execute() {
    return Promises.of(deferred -> {
      ContentLocator locator = entityAccessor.getLocator();
      log.info(
          TextTemplates.createBy("Dispose file {{locator}}").withParameter("locator", locator));
      storage.delete(locator);
      entityAccessor.dispose();
      deferred.resolve(null);
    });
  }
}