package com.ibm.scheduler.tasks;

import java.io.IOException;

import org.springframework.scheduling.support.CronTrigger;

import com.ibm.scheduler.creator.BatchTask;

public class LogsCleanUpTask extends BatchTask {
  String cronExpression = "* 0 0 * * 1,2,3,4,5,6,7";

  @Override
  public void run() {
    System.out.println("Starting " + this.getClass().getSimpleName() + "...");
    Process process;
    try {
      process = Runtime.getRuntime().exec("/bin/sh -c /cmr/batch/logscleanup.sh &");
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
