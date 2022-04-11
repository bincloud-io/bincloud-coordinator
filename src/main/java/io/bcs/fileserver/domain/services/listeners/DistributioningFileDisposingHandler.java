package io.bcs.fileserver.domain.services.listeners;

import io.bce.domain.EventListener;
import io.bcs.fileserver.domain.events.FileHasBeenDisposed;
import io.bcs.fileserver.domain.model.file.FileRepository;
import lombok.RequiredArgsConstructor;

/**
 * This event listener reacts on file disposing event and deactivates all replicas.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class DistributioningFileDisposingHandler implements EventListener<FileHasBeenDisposed> {
  private final FileRepository fileRepository;

  @Override
  public void onEvent(FileHasBeenDisposed event) {
    fileRepository.findAllReplicatedFiles(event.getStorageFileName()).forEach(file -> {
      file.dispose();
      fileRepository.save(file);
    });
  }
}
