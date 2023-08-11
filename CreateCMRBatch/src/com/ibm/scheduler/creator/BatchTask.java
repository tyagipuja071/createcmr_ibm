package com.ibm.scheduler.creator;

import org.springframework.scheduling.support.CronTrigger;

import com.ibm.scheduler.interfaces.ProcessLogger;
import com.ibm.scheduler.interfaces.ProcessStarter;

public abstract class BatchTask implements Runnable, ProcessLogger, ProcessStarter {
  public abstract String cronExpression();

  public abstract CronTrigger getCronTrigger();

  public abstract boolean isCronEnable();

}
