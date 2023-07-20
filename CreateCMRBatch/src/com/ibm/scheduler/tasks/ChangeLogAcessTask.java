package com.ibm.scheduler.tasks;

import org.springframework.scheduling.support.CronTrigger;

import com.ibm.scheduler.creator.BatchTask;

public class ChangeLogAcessTask extends BatchTask {
  String cronExpression = "* 0 1,3,5,7,9 * * *";
  String command = "/bin/sh -c chmod -R 777 /ci/shared/data/applogs && chmod -R 777 /cmr/batch/logs";

  @Override
  public void run() {
    runProcess(command, this.getClass().getSimpleName());
  }

  @Override
  public String cronExpression() {
    return cronExpression;
  }

  @Override
  public CronTrigger getCronTrigger() {
    return new CronTrigger(cronExpression);
  }

  @Override
  public boolean isCronEnable() {
    // TODO fetch this from somewhere
    return true;
  }
}
