package com.ibm.cmr.create.batch.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;

/*
 * Multi-threaded service for {@link ATService}
 */
public class ATMultiService extends MultiThreadedBatchService<Long> {

  private ATService service = new ATService();
  private boolean countryMapInit = false;

  public enum Mode {
    Aborted, Normal, Mass
  };

  Mode mode = Mode.Normal;

  @Override
  public boolean isTransactional() {
    return true;
  }

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<Long> requests) throws Exception {
    this.service.initClientAT();
    ChangeLogListener.setUser(BATCH_USER_ID);
    synchronized (this) {
      if (!this.countryMapInit) {
        LandedCountryMap.init(entityManager);
        this.countryMapInit = true;
      }
    }
    switch (mode) {
    case Aborted:
      this.service.monitorAbortedRecords(entityManager, requests);
      break;
    case Normal:
      this.service.monitorCreqcmr(entityManager, requests);
      break;
    case Mass:
      this.service.monitorCreqcmrMassUpd(entityManager, requests);
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

    case Normal:
      records = this.service.getPendingRecords(entityManager);
      break;
    case Aborted:
      records = this.service.getPendingRecordsAborted(entityManager);
      break;
    case Mass:
      records = this.service.getPendingRecordsMassUpd(entityManager);
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
  protected String getThreadName() {
    return "AT-Multi";
  }

  public Mode getMode() {
    return mode;
  }

  public void setMode(Mode mode) {
    this.mode = mode;
  }

}