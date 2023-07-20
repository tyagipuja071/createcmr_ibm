package com.ibm.scheduler.tasks;

import org.springframework.scheduling.support.CronTrigger;

import com.ibm.scheduler.creator.BatchTask;

public class RdcIERPMassUpdateTask extends BatchTask {
  String cronExpression = "* 2,5,8,11,14,17,20,23,26,29,32,35,36,38,41,44,47,50,53,56 * * * *";
  String command = "/bin/sh -c /cmr/batch/run_ierp_massUpd_batch.ksh >> /cmr/batch/batch-run.log 2> /cmr/batch/batch-err.log";

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
