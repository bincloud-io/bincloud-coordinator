package io.bcs.fileserver.domain.services.listeners;

import io.bce.domain.EventBus;
import io.bce.domain.EventListener;
import io.bce.domain.EventPublisher;
import io.bcs.fileserver.domain.Constants;
import io.bcs.fileserver.domain.events.FileContentHasBeenUploaded;
import io.bcs.fileserver.domain.events.FileDistributionHasBeenStarted;
import io.bcs.fileserver.domain.model.file.FileRepository;
import lombok.RequiredArgsConstructor;

/**
 * This event listener reacts on uploaded content event and starts the file distribution if it is
 * happened.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class UploadedFileDistributingStartHandler
    implements EventListener<FileContentHasBeenUploaded> {
  private final FileRepository fileRepository;
  private final EventBus eventBus;

  @Override
  public void onEvent(FileContentHasBeenUploaded event) {
    fileRepository.findLocatedOnCurrentPoint(event.getLocator().getStorageFileName())
        .ifPresent(file -> {
          EventPublisher<FileDistributionHasBeenStarted> eventPublisher = getPublisher();
          file.startFileDistribution(event.getLocator(), event.getTotalLength());
          fileRepository.save(file);
          eventPublisher.publish(new FileDistributionHasBeenStarted(file));
        });
  }

  private EventPublisher<FileDistributionHasBeenStarted> getPublisher() {
    return eventBus.getPublisher(Constants.CONTEXT, FileDistributionHasBeenStarted.EVENT_TYPE);
  }
}
