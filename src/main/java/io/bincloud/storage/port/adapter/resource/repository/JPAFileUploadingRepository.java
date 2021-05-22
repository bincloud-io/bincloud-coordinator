package io.bincloud.storage.port.adapter.resource.repository;

import java.util.Optional;

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
	public Optional<FileUploading> findById(Long resourceId, String fileId) {
		FileUploadingId fileUploadingId = new FileUploadingId(resourceId, fileId);
		return Optional.ofNullable(entityManager.find(FileUploading.class, fileUploadingId));
	}

	@Override
	public Optional<FileUploading> findLatestResourceUploading(Long resourceId) {
		TypedQuery<FileUploading> findLatestResourceUploadingQuery = entityManager
				.createNamedQuery("FileUploadingRepository.findLatestResourceUploading", FileUploading.class);
		findLatestResourceUploadingQuery.setParameter("resourceId", resourceId);
		return findLatestResourceUploadingQuery.getResultList().stream().findFirst();
	}

	@Override
	@SneakyThrows
	public void save(FileUploading fileUploading) {
		transactionManager.begin();
		entityManager.merge(fileUploading);
		transactionManager.commit();
	}

}
