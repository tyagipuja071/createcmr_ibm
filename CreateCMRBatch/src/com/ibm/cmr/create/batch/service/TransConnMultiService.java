/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;

/**
 * Multi-threaded service for {@link TransConnService}
 * 
 * @author 136786PH1
 *
 */
public class TransConnMultiService extends MultiThreadedBatchService<Long> {

  private TransConnService service = new TransConnService();

  public enum Mode {
    Aborted, Pending, MQ, Manual, Pool, LAReprocess
  };

  private Mode mode = Mode.Pending;
  private boolean poolMode;

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<Long> requests) throws Exception {
    this.service.initClient();

    ChangeLogListener.setUser(BATCH_USER_ID);
    switch (mode) {
    case Aborted:
      this.service.monitorAbortedRecords(entityManager, requests);
      break;
    case MQ:
      this.service.monitorMQInterfaceRequests(entityManager, requests);
      break;
    case Manual:
      this.service.monitorDisAutoProcRec(entityManager, requests);
      break;
    case Pending:
      this.service.monitorTransconn(entityManager, requests);
      break;
    case Pool:
      this.service.monitorLegacyPending(entityManager, requests);
      break;
    case LAReprocess:
      this.service.monitorLAReprocessRdcRecords(entityManager, requests);
      break;
    default:
      break;
    }
    keepAlive();
    return true;
  }

  @Override
  protected Queue<Long> getRequestsToProcess(EntityManager entityManager) {
    List<Long> records = null;
    switch (mode) {
    case Aborted:
      records = this.service.gatherAbortedRecords(entityManager);
      break;
    case MQ:
      records = this.service.gatherMQInterfaceRequests(entityManager);
      break;
    case Manual:
      records = this.service.gatherDisAutoProcRecords(entityManager);
      break;
    case Pending:
      records = this.service.gatherTransConnRecords(entityManager);
      break;
    case Pool:
      records = this.service.gatherLegacyPending(entityManager);
      break;
    case LAReprocess:
      records = this.service.gatherLAReprocessRdcRecords(entityManager);
      break;
    default:
      break;
    }
    Queue<Long> queue = new LinkedList<>();
    if (records != null && !records.isEmpty()) {
      queue.addAll(records);
    }
    return queue;
  }

  public void setDeleteMode(boolean delete) {
    this.service.setDeleteRDcTargets(true);
  }

  public void setMultiMode(boolean multiMode) {
    this.service.setMultiMode(multiMode);
  }

  @Override
  protected String getThreadName() {
    return "TC-Multi";
  }

  @Override
  public boolean isTransactional() {
    return true;
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

  public boolean isPoolMode() {
    return poolMode;
  }

  public void setPoolMode(boolean poolMode) {
    this.poolMode = poolMode;
  }

}
