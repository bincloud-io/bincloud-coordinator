package io.bcs.fileserver.domain.services.listeners;

import io.bce.domain.EventListener;
import io.bcs.fileserver.domain.events.FileDistributionHasBeenStarted;
import io.bcs.fileserver.domain.model.file.FileRepository;
import lombok.RequiredArgsConstructor;

/**
 * This event listener reacts on distribution starting event and activates all replicas.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class FileDistributionActivationHandler
    implements EventListener<FileDistributionHasBeenStarted> {
  private final FileRepository fileRepository;

  @Override
  public void onEvent(FileDistributionHasBeenStarted event) {
    fileRepository.findAllReplicatedFiles(event.getLocator().getStorageFileName()).forEach(file -> {
      file.startFileDistribution(event.getLocator(), event.getTotalLength());
      fileRepository.save(file);
    });
  }
}
