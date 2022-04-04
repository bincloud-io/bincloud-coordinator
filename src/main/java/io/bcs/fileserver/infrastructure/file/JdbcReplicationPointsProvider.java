package io.bcs.fileserver.infrastructure.file;

import io.bcs.fileserver.domain.model.DistributionPointNameProvider;
import io.bcs.fileserver.domain.services.listeners.CreatedFileSynchronizationHandler.ReplicationPointsProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashSet;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * This class provides the replication points name.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class JdbcReplicationPointsProvider implements ReplicationPointsProvider {
  private static final String FIND_REPLICATION_POINTS_QUERY = "SELECT DISTRIBUTION_POINT_NAME "
      + "FROM REF_DISTRIBUTION_POINTS WHERE DISTRIBUTION_POINT_NAME <> ?";

  private final DataSource dataSource;
  private final DistributionPointNameProvider distributionPointNameProvider;

  @Override
  @SneakyThrows
  public Collection<String> findReplicationPoints() {
    try (Connection connection = dataSource.getConnection()) {
      PreparedStatement statement = connection.prepareStatement(FIND_REPLICATION_POINTS_QUERY);
      statement.setString(1, distributionPointNameProvider.getDistributionPointName());
      return extractReplicationPointsNames(statement.executeQuery());
    }
  }

  @SneakyThrows
  private Collection<String> extractReplicationPointsNames(ResultSet resultSet) {
    Collection<String> result = new HashSet<String>();
    while (resultSet.next()) {
      result.add(resultSet.getString(1));
    }
    return result;
  }
}
