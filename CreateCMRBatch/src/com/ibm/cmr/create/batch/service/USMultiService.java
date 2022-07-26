/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.ibm.cmr.create.batch.util.mq.LandedCountryMap;

/**
 * @author Paul
 *
 */
public class USMultiService extends MultiThreadedBatchService<Long> {

  private static final Logger LOG = Logger.getLogger(USMultiService.class);
  private USService service = new USService();
  private boolean countryMapInit = false;

  public enum Mode {
    Single, MassUpdt
  };

  private Mode mode = Mode.Single;

  @Override
  protected Queue<Long> getRequestsToProcess(EntityManager entityManager) {
    List<Long> records = null;
    LOG.debug("Gathering requests. MODE: " + this.mode);
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
    this.service.initClientUS();
    LOG.debug("Processing requests. MODE: " + this.mode);
    synchronized (this) {
      if (!this.countryMapInit) {
        LandedCountryMap.init(entityManager);
        this.countryMapInit = true;
      }
    }
    switch (this.mode) {
    case MassUpdt:
      this.service.monitorCreqcmrMassUpd(entityManager, requests);
      break;
    case Single:
      this.service.monitorCreqcmr(entityManager, requests);
      break;
    default:
      break;
    }
    keepAlive();
    return true;
  }

  @Override
  protected String getThreadName() {
    return "USMulti";
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
