/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;

/**
 * Thread that halts the execution of the JVM after a period of time
 * 
 * @author JeffZAMORA
 * 
 */
public class TerminatorThread extends Thread {

  private static final Logger LOG = Logger.getLogger(TerminatorThread.class);
  private long duration;
  private long startTime;
  private EntityManager entityManager;

  public TerminatorThread(long duration, EntityManager entityManager) {
    this.duration = duration;
    this.startTime = new Date().getTime();
    this.entityManager = entityManager;
    setName("Terminator");
    setDaemon(false);
  }

  @Override
  public void run() {
    while (true) {
      // keep running
      LOG.debug("Checking runtime.. (" + this.startTime + ")");
      long curr = new Date().getTime();
      if (curr - this.startTime > this.duration) {
        LOG.warn("Batch has been runnning for " + (this.duration / 1000) + " secs. Terminating.");
        try {
          if (this.entityManager != null && this.entityManager.isOpen()) {
            EntityTransaction transaction = this.entityManager.getTransaction();
            if (transaction != null && transaction.isActive()) {
              LOG.debug("Rolling back current transaction..");
              transaction.rollback();
            }
            LOG.debug("Closing entity manager..");
            this.entityManager.clear();
            this.entityManager.close();
          }

        } catch (Exception e) {
          // noop
        }
        System.exit(1);
      }

      try {
        Thread.sleep(1000 * 60 * 1);
      } catch (InterruptedException e) {
        // noop
      }
    }
  }

  public void keepAlive() {
    LOG.debug("Keeping terminator thread alive..");
    this.startTime = new Date().getTime();
  }

}
