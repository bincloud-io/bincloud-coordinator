package io.bcs.fileserver.domain.services.listeners;

import io.bce.domain.EventListener;
import io.bcs.fileserver.domain.model.file.FileDistributionHasBeenStarted;
import io.bcs.fileserver.domain.model.file.FileLocation;
import io.bcs.fileserver.domain.model.file.FileLocationRepository;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;

/**
 * This event listener write file location to the distribution journal after file distribution
 * started.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class DistributingFileLocationHandler
    implements EventListener<FileDistributionHasBeenStarted> {
  private final FileLocationRepository fileLocationRepository;

  @Override
  public void onEvent(FileDistributionHasBeenStarted event) {
    FileLocation fileLocation = new FileLocation(event);
    fileLocationRepository.save(Arrays.asList(fileLocation));
  }
}
