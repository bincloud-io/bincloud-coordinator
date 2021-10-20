package io.bcs.common.port.adapters.generators;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import io.bce.MustNeverBeHappenedError;
import io.bcs.common.domain.model.generator.SequentialGenerator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class JDBCSequenceGenerator implements SequentialGenerator<Long> {
	private static final String KEY_GENERATION_QUERY = "SELECT SEQ_NEXT(?) FROM DUAL";

	private final DataSource dataSource;
	private final String sequenceName;

	@Override
	@SneakyThrows
	public Long nextValue() {
		try (Connection connection = dataSource.getConnection()) {
			PreparedStatement statement = connection
					.prepareStatement(String.format(KEY_GENERATION_QUERY, sequenceName));
			statement.setString(1, sequenceName);
			ResultSet resultSet = statement.executeQuery();
			checkResultExistence(resultSet);
			return resultSet.getLong(1);
		}
	}

	private void checkResultExistence(ResultSet resultSet) throws SQLException, MustNeverBeHappenedError {
		if (!resultSet.next()) {
			throw new MustNeverBeHappenedError("This query call sequence generator and it must always return value");
		}
	}
}
