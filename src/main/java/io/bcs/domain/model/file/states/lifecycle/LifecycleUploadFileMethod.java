package io.bcs.domain.model.file.states.lifecycle;

import io.bce.interaction.streaming.Destination;
import io.bce.interaction.streaming.binary.BinaryChunk;
import io.bce.logging.ApplicationLogger;
import io.bce.logging.Loggers;
import io.bce.promises.Promise;
import io.bce.promises.Promises;
import io.bce.text.TextTemplates;
import io.bcs.domain.model.file.ContentLocator;
import io.bcs.domain.model.file.ContentUploader;
import io.bcs.domain.model.file.FileStorage;
import io.bcs.domain.model.file.FileState.FileEntityAccessor;
import io.bcs.domain.model.file.Lifecycle.FileUploadStatistic;
import io.bcs.domain.model.file.Lifecycle.LifecycleMethod;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LifecycleUploadFileMethod implements LifecycleMethod<FileUploadStatistic> {
  private static final ApplicationLogger log =
      Loggers.applicationLogger(LifecycleUploadFileMethod.class);
  private final FileEntityAccessor entityAccessor;
  private final FileStorage storage;
  private final ContentUploader uploader;

  @Override
  public Promise<FileUploadStatistic> execute() {
    return Promises.of(deferred -> {
      ContentLocator locator = entityAccessor.getLocator();
      log.info(TextTemplates.createBy("Upload file content to {{locator}}").withParameter("locator",
          locator));
      Destination<BinaryChunk> destination = storage.getAccessOnWrite(locator);
      uploader.upload(entityAccessor.getLocator(), destination).then(statistic -> {
        log.debug(
            TextTemplates.createBy("File content has been successfully written to {{locator}}")
                .withParameter("locator", locator));
        entityAccessor.startFileDistribution(statistic.getTotalLength());
      }).delegate(deferred);
    });
  }
}
