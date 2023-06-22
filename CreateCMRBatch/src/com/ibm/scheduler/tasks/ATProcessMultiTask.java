package com.ibm.scheduler.tasks;

import java.io.IOException;

import org.springframework.scheduling.support.CronTrigger;

import com.ibm.scheduler.creator.BatchTask;

public class ATProcessMultiTask extends BatchTask {
  String cronExpression = "* 0,4,8,12,16,20,24,28,32,36,40,44,48,52,56 * * * *";

  @Override
  public void run() {
    System.out.println("Starting " + this.getClass().getSimpleName() + "...");
    Process process;
    try {
      process = Runtime.getRuntime().exec("/bin/sh -c /cmr/batch/run_at_multi_batch.ksh >> /cmr/batch/batch-run.log 2> /cmr/batch/batch-err.log");
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
