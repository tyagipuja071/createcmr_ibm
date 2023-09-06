package com.ibm.scheduler.creator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The main class which handles creating tasks for the batch application.
 *
 */
public class BatchTaskCreator {

  public static List<Class<?>> classes = BatchClassFileLoader.getClasses("com.ibm.scheduler.tasks");

  public static int getTotalNumberOfBatches() {
    return classes.size();
  }

  public static List<BatchTask> createTasks() {
    List<BatchTask> batchTasks = new ArrayList<BatchTask>();
    for (Class<?> clazz : classes) {
      createInstances(clazz).ifPresent(batchTasks::add);
    }
    return batchTasks;
  }

  private static Optional<BatchTask> createInstances(Class<?> clazz) {
    try {
      return Optional.of((BatchTask) clazz.newInstance());
    } catch (IllegalAccessException | InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return Optional.empty();
    }
  }

}
