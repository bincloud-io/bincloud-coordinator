package io.bcs.fileserver.domain.model.file.lifecycle;

import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.text.TextTemplates;
import io.bcs.fileserver.domain.model.file.lifecycle.Lifecycle.LifecycleMethod;
import io.bcs.fileserver.domain.model.file.state.FileStatus.FileEntityAccessor;
import io.bcs.fileserver.domain.model.storage.ContentLocator;
import io.bcs.fileserver.domain.model.storage.FileStorage;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the file disposing life-cycle method. This method removes an entity
 * associated physical file and make the entity unusable.
 *
 * @author Dmitry Mikhaylenko
 *
 */
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