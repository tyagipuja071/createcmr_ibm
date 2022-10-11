package com.ibm.cmr.create.batch.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.persistence.EntityManager;

import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;

/**
 * 
 * @author Joseph Ramos
 *
 */

public class IERPProcessMultiService extends MultiThreadedBatchService<Long> {

  private static final String COMMENT_LOGGER = "IERP Process Service";
  private IERPProcessService service = new IERPProcessService();

  @Override
  public boolean isTransactional() {
    return true;
  }

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<Long> requests) throws Exception {
    ChangeLogListener.setUser(COMMENT_LOGGER);
    this.service.initClient();

    this.service.setMultiMode(true);
    // this.service.setPendingReqIds(requests);
    this.service.monitorCreqcmr(entityManager, requests);
    keepAlive();

    return true;
  }

  @Override
  protected Queue<Long> getRequestsToProcess(EntityManager entityManager) {
    // Get Pending DR Request Ids
    List<Long> reqIds = this.service.gatherDRPending(entityManager);
    Queue<Long> queue = new LinkedList<>();

    if (reqIds != null && reqIds.size() > 0) {
      queue.addAll(reqIds);
    }

    return queue;
  }

  @Override
  protected String getThreadName() {
    return "IERP-Multi";
  }

  @Override
  protected boolean rolloverSupported() {
    return true;
  }

}
