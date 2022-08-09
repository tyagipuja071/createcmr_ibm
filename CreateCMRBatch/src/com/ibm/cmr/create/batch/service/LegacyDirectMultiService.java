/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;

/**
 * Class to process CMRs moved to the CMRDB2D schema multi-threaded.
 * 
 * @author clint
 */
public class LegacyDirectMultiService extends MultiThreadedBatchService<Long> {

  private LegacyDirectService service = new LegacyDirectService();

  public enum Mode {
    PendingLegacy, PendingRDC
  };

  private Mode mode = Mode.PendingLegacy;

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<Long> requests) throws Exception {
    this.service.initClient();

    ChangeLogListener.setUser(BATCH_USER_ID);
    switch (mode) {
    case PendingLegacy:
      this.service.processPendingLegacy(entityManager, requests);
      break;
    case PendingRDC:
      this.service.processPendingRDC(entityManager, requests);
      break;
    default:
      break;
    }
    keepAlive();
    return true;
  }

  @Override
  protected Queue<Long> getRequestsToProcess(EntityManager entityManager) {
    LandedCountryMap.init(entityManager);
    List<Long> records = null;
    switch (mode) {
    case PendingLegacy:
      records = this.service.gatherPendingRecords(entityManager);
      break;
    case PendingRDC:
      records = this.service.gatherPendingRecordsRDC(entityManager);
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

  public void setMultiMode(boolean multiMode) {
    this.service.setMultiMode(multiMode);
  }

  @Override
  protected String getThreadName() {
    return "LD-Multi";
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