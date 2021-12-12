package io.bcs.common.generators;

import io.bce.Generator;
import io.bce.MustNeverBeHappenedError;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * This class implements the sequential generator, simulates the real sequences. It may be used for
 * example on Mysql database.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@RequiredArgsConstructor
public class JdbcSequenceGenerator implements Generator<Long> {
  private static final String KEY_GENERATION_QUERY = "SELECT SEQ_NEXT(?) FROM DUAL";

  private final DataSource dataSource;
  private final String sequenceName;

  @Override
  @SneakyThrows
  public Long generateNext() {
    try (Connection connection = dataSource.getConnection()) {
      PreparedStatement statement =
          connection.prepareStatement(String.format(KEY_GENERATION_QUERY, sequenceName));
      statement.setString(1, sequenceName);
      ResultSet resultSet = statement.executeQuery();
      checkResultExistence(resultSet);
      return resultSet.getLong(1);
    }
  }

  private void checkResultExistence(ResultSet resultSet)
      throws SQLException, MustNeverBeHappenedError {
    if (!resultSet.next()) {
      throw new MustNeverBeHappenedError(
          "This query call sequence generator and it must always return value");
    }
  }
}
