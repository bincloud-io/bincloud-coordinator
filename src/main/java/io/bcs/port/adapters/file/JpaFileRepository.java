package io.bcs.port.adapters.file;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import io.bcs.domain.model.file.File;
import io.bcs.domain.model.file.FileRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JpaFileRepository implements FileRepository {
    private final EntityManager entityManager;

    @Override
    public Optional<File> findById(String storageFileName) {
        return Optional.ofNullable(entityManager.find(File.class, storageFileName));
    }

    @Override
    public void save(File file) {
        EntityTransaction transaction = startTransaction();
        entityManager.merge(file);
        transaction.commit();
    }
    
    private EntityTransaction startTransaction() {
        EntityTransaction transaction = entityManager.getTransaction();
        if (!transaction.isActive()) {
            transaction.begin();
        }
        return transaction;
    }
}
