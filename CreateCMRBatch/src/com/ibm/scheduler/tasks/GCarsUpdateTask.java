package com.ibm.scheduler.tasks;

import java.io.IOException;

import org.springframework.scheduling.support.CronTrigger;

import com.ibm.scheduler.creator.BatchTask;

public class GCarsUpdateTask extends BatchTask {
  String cronExpression = "* 20,50 * * * *";

  @Override
  public void run() {
    Process process;
    try {
      process = Runtime.getRuntime().exec("/bin/sh -c /cmr/batch/run_gcars_upd_batch.ksh >> /cmr/batch/batch-run.log 2> /cmr/batch/batch-err.log");
      logProcessOutputToConsole(process, this.getClass().getSimpleName());
      process.waitFor();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }

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
