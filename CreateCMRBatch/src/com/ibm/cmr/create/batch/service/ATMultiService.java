package com.ibm.cmr.create.batch.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;

/*
 * Multi-threaded service for {@link ATService}
 */
public class ATMultiService extends MultiThreadedBatchService<Admin> {

  private ATService service = new ATService();

  public enum Mode {
    Aborted, Normal, Mass
  };

  Mode mode = Mode.Normal;

  @Override
  public boolean isTransactional() {
    return true;
  }

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<Admin> requests) throws Exception {
    this.service.initClient();
    ChangeLogListener.setUser(BATCH_USER_ID);

    switch (mode) {
    case Aborted:
      this.service.monitorAbortedRecords(entityManager);
      break;
    case Normal:
      this.service.monitorCreqcmr(entityManager);
      break;
    case Mass:
      this.service.monitorCreqcmrMassUpd(entityManager);
      break;

    default:
      break;
    }
    keepAlive();
    return true;

  }

  @Override
  protected Queue<Admin> getRequestsToProcess(EntityManager entityManager) {
    List<Admin> records = null;
    switch (mode) {

    case Normal:
      records = this.service.getPendingRecords(entityManager);
      break;
    case Mass:
      records = this.service.getPendingRecordsMassUpd(entityManager);
      break;

    default:
      break;
    }

    Queue<Admin> queue = new LinkedList<>();
    if (records != null && !records.isEmpty()) {
      queue.addAll(records);
    }
    return queue;
  }

  @Override
  protected String getThreadName() {
    return "AT-Multi";
  }

}