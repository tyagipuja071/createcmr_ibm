package com.ibm.scheduler.tasks;

import java.io.IOException;

import org.springframework.scheduling.support.CronTrigger;

import com.ibm.scheduler.creator.BatchTask;

public class AutomationEngineTask extends BatchTask {
  String cronExpression = "* */2 * * * *";

  @Override
  public void run() {
    System.out.println("Starting " + this.getClass().getCanonicalName() + " ...");
    Process process;
    try {
      process = Runtime.getRuntime().exec("/bin/sh -c /cmr/batch/run_auto_engine.ksh >> /cmr/batch/batch-run.log 2>/cmr/batch/batch-err.log");
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
