package io.bcs.fileserver.infrastructure.repositories;

import io.bcs.fileserver.domain.model.storage.descriptor.LocalStorageDescriptor;
import io.bcs.fileserver.domain.model.storage.descriptor.LocalStorageDescriptorRepository;
import java.util.Collection;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

/**
 * This class implements the local storage descriptors repository, using JPA framework.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class JpaLocalStorageDescriptorRepository implements LocalStorageDescriptorRepository {
  private final EntityManager entityManager;

  @Override
  public Collection<LocalStorageDescriptor> findByMediaType(String mediaType) {
    TypedQuery<LocalStorageDescriptor> query = entityManager
        .createNamedQuery("LocalStorageDescriptor.findByMediaType", LocalStorageDescriptor.class);
    query.setParameter("mediaType", mediaType);
    return query.getResultList();
  }

  @Override
  public Optional<LocalStorageDescriptor> findByName(String storageName) {
    return Optional.ofNullable(entityManager.find(LocalStorageDescriptor.class, storageName));
  }
}
