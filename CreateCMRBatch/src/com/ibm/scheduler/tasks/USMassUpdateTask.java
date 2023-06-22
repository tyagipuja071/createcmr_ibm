package com.ibm.scheduler.tasks;

import java.io.IOException;

import org.springframework.scheduling.support.CronTrigger;

import com.ibm.scheduler.creator.BatchTask;

public class USMassUpdateTask extends BatchTask {
  String cronExpression = "* 0,6,12,18,24,30,36,42,48,54 * * * *";

  @Override
  public void run() {
    System.out.println("Starting " + this.getClass().getSimpleName() + "...");
    Process process;
    try {
      process = Runtime.getRuntime().exec("/bin/sh -c /cmr/batch/run_us_massUpd_batch.ksh >> /cmr/batch/batch-run.log 2> /cmr/batch/batch-err.log");
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
