/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cmr.create.batch.util.BatchThreadWorker;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.masscreate.WorkerThreadFactory;

/**
 * Base batch service class to handle multi-threaded processing
 * 
 * @author JeffZAMORA
 * 
 */
public abstract class MultiThreadedBatchService extends BaseBatchService {

  private static final Logger LOG = Logger.getLogger(MultiThreadedBatchService.class);

  @Override
  public Boolean process(HttpServletRequest request, ParamContainer params) throws CmrException {

    LOG.debug("Retrieving requests to process..");
    // get first all request ids using one entitymanager
    Queue<Long> requestIds = null;
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      requestIds = getRequestsToProcess(entityManager);
      if (requestIds == null || requestIds.isEmpty()) {
        LOG.info("No pending requests to process at this time.");
        return true;
      }
      LOG.info(requestIds.size() + " requests to process..");
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }

    // allocate the requests and start a fixed thread pool of workers
    int threads = 5;
    String threadCount = BatchUtil.getProperty("multithreaded.threadCount");
    if (threadCount != null && StringUtils.isNumeric(threadCount)) {
      threads = Integer.parseInt(threadCount);
    }
    LOG.debug(threads + " threads to use in execution.");
    // ExecutorService executor = Executors.newFixedThreadPool(threads, new
    // WorkerThreadFactory(getThreadName()));
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads, new WorkerThreadFactory(getThreadName()));

    List<List<Long>> allocatedRequests = allocateRequests(requestIds, threads);

    BatchThreadWorker worker = null;
    List<BatchThreadWorker> workers = new ArrayList<BatchThreadWorker>();

    int delay = 15;
    int currCount = 1;
    for (List<Long> requestBatch : allocatedRequests) {
      worker = new BatchThreadWorker(this, requestBatch);
      if (worker != null) {
        executor.schedule(worker, delay * currCount, TimeUnit.SECONDS);
      }
      workers.add(worker);
      currCount++;
    }

    try {
      executor.shutdown();
      executor.awaitTermination(3, TimeUnit.HOURS);
    } catch (InterruptedException e1) {
      LOG.warn("Executor encountered an error while awaiting task execution", e1);
    }
    // wait till execution finish
    // executor.shutdown();

    while (!executor.isTerminated()) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        // noop
      }
    }

    boolean success = true;
    for (BatchThreadWorker workerThread : workers) {
      if (workerThread.isError()) {
        success = false;
        addError(workerThread.getErrorMessage());
      }
    }

    LOG.info("Batch thread workers finished.");

    return success;
  }

  /**
   * Distributes the requests evenly on lists that equate to the thread count
   * 
   * @param requests
   * @param threadCount
   * @return
   */
  private static List<List<Long>> allocateRequests(Queue<Long> requests, int threadCount) {
    List<List<Long>> lists = new ArrayList<List<Long>>();
    if (requests.size() < threadCount) {
      for (int i = 0; i < threadCount; i++) {
        Long reqId = requests.poll();
        if (reqId != null) {
          lists.add(Collections.singletonList(reqId));
        }
      }
    } else {
      while (requests.peek() != null) {
        for (int i = 0; i < threadCount; i++) {
          if (lists.size() < threadCount) {
            lists.add(new ArrayList<Long>());
          }
          Long reqId = requests.poll();
          if (reqId != null) {
            List<Long> queueForThread = lists.get(i);
            queueForThread.add(reqId);
          }
        }
      }
    }

    return lists;
  }

  @Override
  protected Boolean executeBatch(EntityManager entityManager) throws Exception {
    // override and make as a NOOP method, use #executeBatchForRequests
    return null;
  }

  /**
   * Gets the batch user id
   * 
   * @return
   */
  public String getUserId() {
    return BATCH_USER_ID;
  }

  /**
   * Gets the service id
   * 
   * @return
   */
  public String getServiceURL() {
    return BATCH_SERVICE_URL;
  }

  @Override
  public abstract boolean isTransactional();

  /**
   * Executes the processing of this batch service only against the given list
   * of requests
   * 
   * @param entityManager
   * @param requests
   * @return
   * @throws Exception
   */
  public abstract Boolean executeBatchForRequests(EntityManager entityManager, List<Long> requests) throws Exception;

  /**
   * Queries the requests on the database and returns a list of the IDs to be
   * included in the processing
   * 
   * @param entityManager
   * @return
   */
  protected abstract Queue<Long> getRequestsToProcess(EntityManager entityManager);

  /**
   * Gets the thread name to assign to the worker threads
   * 
   * @return
   */
  protected abstract String getThreadName();

}
