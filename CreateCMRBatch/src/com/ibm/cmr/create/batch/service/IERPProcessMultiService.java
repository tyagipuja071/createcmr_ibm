package com.ibm.cmr.create.batch.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author Joseph Ramos
 *
 */

public class IERPProcessMultiService extends MultiThreadedBatchService<Long> {

  private IERPProcessService service = new IERPProcessService();

  @Override
  public boolean isTransactional() {
    return true;
  }

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<Long> requests) throws Exception {

    this.service.setMultiMode(true);
    this.service.setPendingReqIds(requests);
    this.service.executeBatch(entityManager);
    keepAlive();

    return true;
  }

  @Override
  protected Queue<Long> getRequestsToProcess(EntityManager entityManager) {
    // Get Pending DR Request Ids
    List<Long> reqIds = this.service.gatherDRPending(entityManager);
    Queue<Long> queue = new LinkedList<>();

    if (reqIds != null && reqIds.size() > 0) {
      System.out.println("moja! reqIds: " + StringUtils.join(reqIds, ",")); // todo
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
