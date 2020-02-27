/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate;

import java.util.concurrent.ThreadFactory;

/**
 * Thread factory for mass create validations
 * 
 * @author Jeffrey Zamora
 * 
 */
public class WorkerThreadFactory implements ThreadFactory {

  private String threadName;

  public WorkerThreadFactory(String threadName) {
    this.threadName = threadName;
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread t = new Thread(r);
    t.setName(this.threadName + "-" + t.getId());
    return t;
  }

}
