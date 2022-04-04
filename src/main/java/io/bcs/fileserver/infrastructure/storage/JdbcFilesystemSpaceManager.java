package io.bcs.fileserver.infrastructure.storage;

import io.bcs.fileserver.domain.errors.FileStorageException;
import io.bcs.fileserver.domain.model.DistributionPointNameProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * This class manages a filesystem space. It performs operations such as filesystem space allocation
 * and release.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class JdbcFilesystemSpaceManager implements FilesystemSpaceManager {
  private static final String ALLOCATE_SPACE_QUERY =
      "SELECT LS_ALLOC_SPACE(?, ?, ?, ?) AS ALLOCATED_STORAGE FROM DUAL";
  private static final String RELEASE_SPACE_QUERY =
      "SELECT LS_RELEASE_SPACE(?, ?, ?) AS IS_RELEASED FROM DUAL";

  private final DataSource dataSource;
  private final DistributionPointNameProvider distributionPointNameProvider;

  @Override
  @SneakyThrows
  public String allocateSpace(String mediaType, String storageFileName, Long contentLength) {
    try (Connection connection = dataSource.getConnection()) {
      PreparedStatement statement = connection.prepareStatement(ALLOCATE_SPACE_QUERY);
      statement.setString(1, distributionPointNameProvider.getDistributionPointName());
      statement.setString(2, mediaType);
      statement.setString(3, storageFileName);
      statement.setLong(4, contentLength);
      return Optional.of(statement.executeQuery()).filter(this::isDataReceived)
          .map(this::extractAllocatedStorage).orElseThrow(() -> {
            return new FileStorageException("Requested file space couldn't be allocated because "
                + "there isn't enough space on local storages.");
          });
    }
  }

  @Override
  @SneakyThrows
  public void releaseSpace(String storageName, String storageFileName) {
    try (Connection connection = dataSource.getConnection()) {
      PreparedStatement statement = connection.prepareStatement(RELEASE_SPACE_QUERY);
      statement.setString(1, distributionPointNameProvider.getDistributionPointName());
      statement.setString(2, storageName);
      statement.setString(3, storageFileName);
      boolean isReleased = Optional.of(statement.executeQuery()).filter(this::isDataReceived)
          .map(this::extractReleaseStatus).orElse(false);
      if (!isReleased) {
        throw new FileStorageException("Space for specified file couldn't be released.");
      }
    }
  }

  @SneakyThrows
  private String extractAllocatedStorage(ResultSet resultSet) {
    return resultSet.getString("ALLOCATED_STORAGE");
  }

  @SneakyThrows
  private boolean extractReleaseStatus(ResultSet resultSet) {
    return resultSet.getBoolean("IS_RELEASED");
  }

  @SneakyThrows
  private boolean isDataReceived(ResultSet resultSet) {
    return resultSet.next();
  }
}
