package com.ibm.scheduler.tasks;

import java.io.IOException;

import org.springframework.scheduling.support.CronTrigger;

import com.ibm.scheduler.creator.BatchTask;

public class MassCreateValidatorTask extends BatchTask {
  String cronExpression = "* 00,05,10,15,20,25,30,35,40,45,50,55 * * * *";

  @Override
  public void run() {
    System.out.println("Starting " + this.getClass().getSimpleName() + "...");
    Process process;
    try {
      process = Runtime.getRuntime()
          .exec("/bin/sh -c /cmr/batch/run_masscreate_validator.ksh >> /cmr/batch/batch-run.log 2> /cmr/batch/batch-err.log");
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
