/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cmr.create.batch.service.MultiThreadedBatchService;

/**
 * Thread worker that takes up a process job. A process job typically should
 * process only one request
 * 
 * @author JeffZAMORA
 * 
 */
public class BatchThreadWorker implements Runnable {

  private static final Logger LOG = Logger.getLogger(BatchThreadWorker.class);

  private MultiThreadedBatchService batchService;
  private List<Long> requestIds;

  private boolean error;
  private String errorMessage;

  public BatchThreadWorker(MultiThreadedBatchService batchService, List<Long> requestIds) {
    this.batchService = batchService;
    this.requestIds = requestIds;
  }

  @Override
  public void run() {
    try {
      if (this.requestIds == null || this.requestIds.isEmpty()) {
        LOG.info("No request to process at this time.");
        return;
      }
      LOG.info("Starting processing of " + this.requestIds.size() + " requests");

      EntityManagerFactory emf = Persistence.createEntityManagerFactory("RDC");
      try {
        EntityManager entityManager = emf.createEntityManager();
        EntityTransaction transaction = null;
        try {

          LOG.info("Batch User ID: " + this.batchService.getUserId());
          if (this.batchService.isTransactional()) {
            ChangeLogListener.setManager(entityManager);
            transaction = entityManager.getTransaction();
            transaction.begin();
          }

          this.batchService.executeBatchForRequests(entityManager, this.requestIds);

          if (this.batchService.isTransactional() && transaction != null && transaction.isActive() && !transaction.getRollbackOnly()) {
            transaction.commit();
          }

        } catch (Throwable e) {
          LOG.error("An error was encountered during processing. Transaction will be rolled back.", e);
          if (this.batchService.isTransactional() && transaction != null && transaction.isActive()) {
            transaction.rollback();
          }
          throw e;
        } finally {
          if (this.batchService.isTransactional()) {
            ChangeLogListener.clearManager();
          }
          if (this.batchService.isTransactional() && transaction != null && transaction.isActive()) {
            transaction.rollback();
          }
          // empty the manager
          entityManager.clear();
          entityManager.close();
        }
      } finally {
        emf.close();
      }

    } catch (Exception e) {
      this.error = true;
      this.errorMessage = "Unexpected error when processing record. " + e.getMessage();
      LOG.error("Unexpected error when processing record. ", e);
    }
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
