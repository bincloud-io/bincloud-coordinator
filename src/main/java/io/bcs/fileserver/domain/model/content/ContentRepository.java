package io.bcs.fileserver.domain.model.content;

import java.util.Optional;

/**
 * This interface describes the {@link Content} entity repository.
 *
 * @author Dmitry Mikhaylenko
 *
 */
public interface ContentRepository {
  Optional<Content> findBy(String storageFileName);
}
