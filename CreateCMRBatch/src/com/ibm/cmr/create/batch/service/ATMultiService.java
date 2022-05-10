package com.ibm.cmr.create.batch.service;

import java.util.List;
import java.util.Queue;

import javax.persistence.EntityManager;

import org.apache.poi.ss.formula.functions.T;

import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;

/*
 * Multi-threaded service for {@link ATService}
 */
public class ATMultiService extends MultiThreadedBatchService<T> {

  private ATService service = new ATService();

  @Override
  public boolean isTransactional() {
    return true;
  }

  @Override
  public Boolean executeBatchForRequests(EntityManager entityManager, List<T> requests) throws Exception {
    this.service.initClient();
    ChangeLogListener.setUser(BATCH_USER_ID);

    return null;
  }

  @Override
  protected Queue<T> getRequestsToProcess(EntityManager entityManager) {
    return null;
  }

  @Override
  protected String getThreadName() {
    return "AT-Multi";
  }

}
