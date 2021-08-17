/**
 * 
 */
package com.ibm.cmr.create.batch.util;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cmr.create.batch.service.MultiThreadedBatchService;

/**
 * Class to handle second-level multi-threading<br>
 * T denotes the entity class to process in multi threaded mode
 * 
 * 
 * @author 136786PH1
 *
 */
public abstract class MultiThreadedWorker<T> implements Runnable {

  private static final Logger LOG = Logger.getLogger(MultiThreadedWorker.class);
  protected static final String BATCH_SERVICES_URL = SystemConfiguration.getValue("BATCH_SERVICES_URL");
  protected static final String BATCH_USER_ID = SystemConfiguration.getValue("BATCH_USERID");

  private List<String> statusCodes = new ArrayList<String>();
  private List<String> rdcProcessStatusMsgs = new ArrayList<String>();
  private StringBuilder comment = new StringBuilder();
  private boolean error;
  private Throwable errorMsg;

  protected Admin parentAdmin;
  protected T parentRow;

  protected MultiThreadedBatchService<?> parentService;

  public MultiThreadedWorker(MultiThreadedBatchService<?> parentService, Admin parentAdmin, T parentEntity) {
    this.parentService = parentService;
    this.parentAdmin = parentAdmin;
    this.parentRow = parentEntity;
  }

  @Override
  public void run() {

    EntityManager entityManager = JpaManager.getEntityManager();
    if (flushOnCommitOnly()) {
      LOG.debug("Setting flush mode to COMMIT..");
      entityManager.setFlushMode(FlushModeType.COMMIT);
    }
    EntityTransaction transaction = null;
    try {

      ChangeLogListener.setManager(entityManager);
      ChangeLogListener.setUser(BATCH_USER_ID);
      transaction = entityManager.getTransaction();
      transaction.begin();

      // execute
      executeProcess(entityManager);

      entityManager.flush();

      if (transaction != null && transaction.isActive() && !transaction.getRollbackOnly()) {
        LOG.debug("Committing working transaction...");
        transaction.commit();
        this.parentService.keepAlive();
      }

    } catch (Throwable e) {
      this.error = true;
      this.errorMsg = e;
      LOG.error("An error was encountered during processing. Transaction will be rolled back.", e);
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
    } finally {
      ChangeLogListener.clearManager();
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }

  }

  /**
   * Main execute process
   * 
   * @param entityManager
   * @throws Exception
   */
  public abstract void executeProcess(EntityManager entityManager) throws Exception;

  /**
   * Override and set to true if flushes will only be done on explicit calls and
   * commits
   * 
   * @return
   */
  public boolean flushOnCommitOnly() {
    return false;
  }

  /**
   * Adds an rdc status code or message to the list
   * 
   * @param rdcStatus
   */
  public void addRdcStatus(String rdcStatus) {
    this.rdcProcessStatusMsgs.add(rdcStatus);
  }

  /**
   * Adds a status code to the list
   * 
   * @param statusCode
   */
  public void addStatusCode(String statusCode) {
    this.statusCodes.add(statusCode);
  }

  /**
   * Adds a comment to the current comment string
   * 
   * @param comment
   */
  public void addComment(String comment) {
    this.comment.append(comment);
  }

  public String getComments() {
    return this.comment.toString();
  }

  public boolean isError() {
    return error;
  }

  public void setError(boolean error) {
    this.error = error;
  }

  public Throwable getErrorMsg() {
    return errorMsg;
  }

  public void setErrorMsg(Throwable errorMsg) {
    this.errorMsg = errorMsg;
  }

  public List<String> getStatusCodes() {
    return statusCodes;
  }

  public List<String> getRdcProcessStatusMsgs() {
    return rdcProcessStatusMsgs;
  }

}
