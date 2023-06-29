package com.ibm.scheduler;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.ibm.scheduler.creator.BatchTask;
import com.ibm.scheduler.creator.BatchTaskCreator;

public class BatchAppScheduler {

  public static void main(String[] args) {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(2);
    scheduler.initialize();
    for (BatchTask cronTask : BatchTaskCreator.createTasks()) {
      if (cronTask.isCronEnable()) {
        scheduler.schedule(cronTask, cronTask.getCronTrigger());
      }
    }
  }
}
