package io.bcs.fileserver.infrastructure.jobs;

import io.bce.domain.errors.UnexpectedErrorException;
import io.bcs.fileserver.domain.services.ContentService;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;

/**
 * This class implements daemon job performs filesystem cleaning.
 *
 * @author Dmitry Mikhaylenko
 *
 */
@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class FilesCleanJob {
  private static final String JOB_NAME = "FILES_CLEAN_JOB";
  @Resource
  private TimerService timerService;

  @Inject
  @FilesCleanTimer
  private ScheduleExpression schedulerExpression;

  @Inject
  private LockProvider jobLockProvider;

  @Inject
  @SuppressWarnings("cdi-ambiguous-dependency")
  private TransactionManager transactionManager;
  
  @Inject
  private ContentService contentService;

  @PostConstruct
  public void initTimer() {
    timerService.createCalendarTimer(schedulerExpression, new TimerConfig(JOB_NAME, false));
  }

  /**
   * React on timer event.
   *
   * @param timer The created timer
   * @throws SystemException If transaction manager throws
   */
  @Timeout
  public void onTimeout(Timer timer) {
    if (timer.getInfo() == JOB_NAME) {
      executeJob();
    }
  }

  private void executeJob() {
    Transaction transaction = suspendTransaction();
    getLock().ifPresent(lock -> {
      try {
        resumeTransaction(transaction);
        contentService.clearDisposedFiles();
      } finally {
        unlock(lock);
      }
    });
  }

  private void unlock(SimpleLock lock) {
    Transaction transaction = suspendTransaction();
    lock.unlock();
    resumeTransaction(transaction);
  }

  private Transaction suspendTransaction() {
    try {
      return transactionManager.suspend();
    } catch (SystemException e) {
      throw new UnexpectedErrorException(e);
    }
  }

  private void resumeTransaction(Transaction transaction) {
    try {
      transactionManager.resume(transaction);
    } catch (Exception e) {
      throw new UnexpectedErrorException(e);
    }
    ;
  }

  private Optional<SimpleLock> getLock() {
    LockConfiguration lockConfiguration =
        new LockConfiguration(Instant.now(), JOB_NAME, Duration.ofHours(1), Duration.ofSeconds(0));
    return jobLockProvider.lock(lockConfiguration);
  }

  /**
   * This annotation qualifies cheduler expression bean.
   *
   * @author Dmitry Mikhaylenko
   *
   */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.FIELD, ElementType.METHOD })
  public @interface FilesCleanTimer {
  }
}
