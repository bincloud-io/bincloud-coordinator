package io.bcs.fileserver.domain.services.listeners;

import io.bce.domain.EventListener;
import io.bcs.fileserver.domain.events.FileHasBeenCreated;
import io.bcs.fileserver.domain.model.file.File;
import io.bcs.fileserver.domain.model.file.FileRepository;
import java.util.Collection;
import lombok.RequiredArgsConstructor;

/**
 * This listener react on file creation and synchronize it with another distribution points.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class CreatedFileSynchronizationHandler implements EventListener<FileHasBeenCreated> {
  private final FileRepository fileRepository;
  private final DistribuionPointsProvider replicationPointsProvider;

  @Override
  public void onEvent(FileHasBeenCreated event) {
    replicationPointsProvider.findDistributionPoints().forEach(replicationPoint -> {
      fileRepository.save(new File(replicationPoint, event));
    });
  }

  /**
   * This interface describes the component, providing existing distribution points.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  public interface DistribuionPointsProvider {
    /**
     * Find distribution points.
     *
     * @return The collection of distribution points names
     */
    Collection<String> findDistributionPoints();
  }
}
