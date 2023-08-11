package com.ibm.scheduler.tasks;

import org.springframework.scheduling.support.CronTrigger;

import com.ibm.scheduler.creator.BatchTask;

public class AutomationEngineTask extends BatchTask {
  String cronExpression = "* */2 * * * *";
  String command = "/bin/sh -c /cmr/batch/run_auto_engine.ksh >> /cmr/batch/batch-run.log 2> /cmr/batch/batch-err.log";

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
