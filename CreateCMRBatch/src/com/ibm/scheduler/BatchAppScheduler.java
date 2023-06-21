package com.ibm.scheduler;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.ibm.scheduler.creator.BatchTaskCreator;
import com.ibm.scheduler.creator.BatchTask;

public class BatchAppScheduler {

  public static void main(String[] args) {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(BatchTaskCreator.getTotalNumberOfBatches());
    scheduler.initialize();
    for (BatchTask cronTask : BatchTaskCreator.createTasks()) {
      if (cronTask.isCronEnable()) {
        scheduler.schedule(cronTask, cronTask.getCronTrigger());
      }
    }
  }
}
