package com.ibm.scheduler.tasks;

import java.io.IOException;

import org.springframework.scheduling.support.CronTrigger;

import com.ibm.scheduler.creator.BatchTask;

public class CMRRefreshWeekendTask extends BatchTask {
  String cronExpression = "* 0 4,8,12,16,20 * * 6,7";

  @Override
  public void run() {
    Process process;
    try {
      process = Runtime.getRuntime()
          .exec("/bin/sh -c /cmr/batch/run_cmr_refresh.ksh -hours 4 >> /cmr/batch/batch-run.log 2> /cmr/batch/batch-err.log");
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
