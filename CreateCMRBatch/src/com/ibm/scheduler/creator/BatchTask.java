package com.ibm.scheduler.creator;

import org.springframework.scheduling.support.CronTrigger;

public abstract class BatchTask implements Runnable {
  public abstract String cronExpression();

  public abstract CronTrigger getCronTrigger();

  public abstract boolean isCronEnable();
}
