package io.bcs.fileserver.infrastructure.config;

import io.bcs.fileserver.infrastructure.jobs.FilesCleanJob.FilesCleanTimer;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;

/**
 * The schedulers config.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@ApplicationScoped
public class SchedulersConfig {
  private static final String SCHEDULERS_LOCK_TABLE = "SCHEDLOCK";

  @Resource(lookup = "java:/jdbc/BC_CENTRAL")
  private DataSource dataSource;

  /**
   * Scheduler lock provider configuration.
   *
   * @return Lock provider
   */
  @Produces
  public LockProvider lockProvider() {
    return new JdbcLockProvider(dataSource, SCHEDULERS_LOCK_TABLE);
  }

  /**
   * Files clean job expression config.
   *
   * @return Schedule expression
   */
  @Produces
  @FilesCleanTimer
  public ScheduleExpression filesCleanJobExpression() {
    return new ScheduleExpression().minute("*/1").hour("*");
  }
}
