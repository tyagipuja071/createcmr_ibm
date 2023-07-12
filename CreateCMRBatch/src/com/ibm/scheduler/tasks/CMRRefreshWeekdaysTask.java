package com.ibm.scheduler.tasks;

import java.io.IOException;

import org.springframework.scheduling.support.CronTrigger;

import com.ibm.scheduler.creator.BatchTask;

public class CMRRefreshWeekdaysTask extends BatchTask {
  String cronExpression = "* 0 * * * 1,2,3,4,5";

  @Override
  public void run() {
    Process process;
    try {
      process = Runtime.getRuntime().exec("/bin/sh -c /cmr/batch/run_cmr_refresh.ksh >> /cmr/batch/batch-run.log 2> /cmr/batch/batch-err.log");
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
