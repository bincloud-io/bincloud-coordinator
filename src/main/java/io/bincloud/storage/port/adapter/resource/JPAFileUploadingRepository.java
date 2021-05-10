package io.bincloud.storage.port.adapter.resource;

import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.TransactionManager;

import io.bincloud.storage.domain.model.resource.file.FileUploading;
import io.bincloud.storage.domain.model.resource.file.FileUploadingId;
import io.bincloud.storage.domain.model.resource.file.FileUploadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class JPAFileUploadingRepository implements FileUploadingRepository {
	private final EntityManager entityManager;
	private final TransactionManager transactionManager;

	@Override
	public Optional<FileUploading> findById(Long resourceId, UUID fileId) {
		FileUploadingId fileUploadingId = new FileUploadingId(resourceId, fileId.toString());
		return Optional.of(entityManager.getReference(FileUploading.class, fileUploadingId));
	}

	@Override
	public Optional<FileUploading> findLatestResourceUploading(Long resourceId) {
		TypedQuery<FileUploading> findLatestResourceUploadingQuery = entityManager
				.createNamedQuery("FileUploadingRepository.findLatestResourceUploading", FileUploading.class);
		findLatestResourceUploadingQuery.setParameter("resourceId", resourceId);
		return Optional.ofNullable(findLatestResourceUploadingQuery.getSingleResult());
	}

	@Override
	@SneakyThrows
	public void save(FileUploading fileUploading) {
		transactionManager.begin();
		entityManager.merge(fileUploading);
		transactionManager.commit();
	}

}
