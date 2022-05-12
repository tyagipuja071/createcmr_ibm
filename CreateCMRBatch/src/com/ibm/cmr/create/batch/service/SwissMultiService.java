/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.persistence.EntityManager;

/**
 * @author 136786PH1
 *
 */
public class SwissMultiService extends MultiThreadedBatchService<Long> {

  private SWISSService service = new SWISSService();

  public enum Mode {
    Single, MassUpdt
  };

  private Mode mode = Mode.Single;

  @Override
  protected Queue<Long> getRequestsToProcess(EntityManager entityManager) {
    List<Long> records = null;
    switch (this.mode) {
    case MassUpdt:
      records = this.service.gatherMassUpdateRequests(entityManager);
      break;
    case Single:
      records = this.service.gatherSingleRequests(entityManager);
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

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<Long> requests) throws Exception {
    this.service.initClientSwiss();
    switch (this.mode) {
    case MassUpdt:
      this.service.monitorCreqcmrMassUpd(entityManager, requests);
      break;
    case Single:
      this.service.monitorCreqcmrMassUpd(entityManager, requests);
      break;
    default:
      break;
    }
    keepAlive();
    return true;
  }

  @Override
  protected String getThreadName() {
    return "SwissMulti";
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

}
