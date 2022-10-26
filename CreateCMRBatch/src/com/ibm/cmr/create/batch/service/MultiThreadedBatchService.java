/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cmr.create.batch.util.BatchThreadWorker;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.TerminatorThread;
import com.ibm.cmr.create.batch.util.masscreate.WorkerThreadFactory;

/**
 * Base batch service class to handle multi-threaded processing
 * 
 * @author JeffZAMORA
 * 
 */
public abstract class MultiThreadedBatchService<T> extends BaseBatchService {

  private static final Logger LOG = Logger.getLogger(MultiThreadedBatchService.class);

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Boolean process(HttpServletRequest request, ParamContainer params) throws CmrException {

    Queue<T> requestIds = preparePendingItems();
    List<T> allProcessed = new ArrayList<T>();
    allProcessed.addAll(requestIds);
    EntityManager entityManager = JpaManager.getEntityManager();

    boolean rollover = !requestIds.isEmpty();

    int terminatorTime = 40; // 40 mins default;
    String terminatorMins = BatchUtil.getProperty("TERMINATOR.MINS");
    if (!StringUtils.isEmpty(terminatorMins) && StringUtils.isNumeric(terminatorMins)) {
      terminatorTime = Integer.parseInt(terminatorMins);
    }
    if (getTerminatorWaitTime() > 0) {
      terminatorTime = getTerminatorWaitTime();
      LOG.debug("Terimator wait time indicated by batch: " + terminatorTime);
    }

    if (terminateOnLongExecution()) {
      LOG.info("Starting terminator thread. Wait time: " + terminatorTime + " mins");
      this.terminator = new TerminatorThread(1000 * 60 * terminatorTime, null);
      this.terminator.start();
    } else {
      LOG.warn("Terminator thread skipped for the run.");
    }

    // allocate the requests and start a fixed thread pool of workers
    int threads = 5;
    String threadCount = BatchUtil.getProperty("multithreaded.threadCount");
    if (threadCount != null && StringUtils.isNumeric(threadCount)) {
      threads = Integer.parseInt(threadCount);
    }
    int fixedThreads = fixedThreadCount(threads);
    if (fixedThreads > 0) {
      threads = fixedThreads;
    }
    LOG.debug(threads + " threads to use in execution.");
    // ExecutorService executor = Executors.newFixedThreadPool(threads, new
    // WorkerThreadFactory(getThreadName()));
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(threads, new WorkerThreadFactory(getThreadName()));

    List<List<T>> allocatedRequests = allocateRequests(requestIds, threads);

    BatchThreadWorker worker = null;
    List<BatchThreadWorker> workers = new ArrayList<BatchThreadWorker>();
    if (hasPreProcess()) {
      preProcess(entityManager);
    }

    for (List<T> requestBatch : allocatedRequests) {
      worker = new BatchThreadWorker(this, requestBatch);
      if (worker != null) {
        executor.execute(worker);
      }
      workers.add(worker);
    }

    String rolloverParam = BatchUtil.getProperty("multithreaded.rollover");
    if (!rolloverSupported() || rolloverParam == null || !"Y".equals(rolloverParam)) {
      rollover = false;
      LOG.debug("No roll over of queue.");
    } else {
      LOG.debug("-Rolling over queueing of requests-");
    }

    // if there are no items on current queue, on rollover and let it end
    if (allocatedRequests == null || allocatedRequests.isEmpty()) {
      LOG.debug("No items for this run, skipping roll-over");
      rollover = false;
    }

    int rolloverCount = 0;
    while (rollover) {
      try {
        Thread.sleep(1000 * (rolloverCount == 0 ? 50 : 30));
      } catch (InterruptedException e) {
      }
      rolloverCount++;
      Queue<T> newPendingItems = preparePendingItems();
      LOG.debug(newPendingItems.size() + " items gathered on rollover..");
      Queue<T> truePendingItems = new LinkedList<>();
      for (T item : newPendingItems) {
        if (!allProcessed.contains(item)) {
          truePendingItems.add(item);
          allProcessed.add(item);
        }
      }
      LOG.debug(" - " + truePendingItems.size() + " NEW items gathered.");

      boolean workersDone = true;
      // before ending, check if any worker is still being executed. rollover if
      // yes. this prevents single long running requests from holding the batch
      for (BatchThreadWorker<?> currWorker : workers) {
        if (!currWorker.isFinished()) {
          workersDone = false;
          break;
        }
      }

      rollover = !truePendingItems.isEmpty();
      if (rollover) {
        List<List<T>> newAllocatedRequests = allocateRequests(truePendingItems, threads);
        for (List<T> requestBatch : newAllocatedRequests) {
          worker = new BatchThreadWorker(this, requestBatch);
          if (worker != null) {
            executor.execute(worker);
          }
          workers.add(worker);
        }
      }
      if (!workersDone) {
        LOG.debug("Worker threads are still running. Rolling over..");
        rollover = true;
      } else if (truePendingItems.size() < threads) {
        // if the pending items is less than the thread count, stop on next
        // execution to ensure requeueing of previous pending records
        LOG.debug("Added items less than thread count (" + truePendingItems.size() + "/" + threads + "), rollover will stop");
        rollover = false;
      }
    }

    LOG.debug("Initiating shutdown of executor...");
    executor.shutdown();

    while (!executor.isTerminated()) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        // noop
      }
    }

    if (hasCleanup()) {
      EntityTransaction tx = null;
      entityManager = JpaManager.getEntityManager();
      try {
        LOG.debug("Performing cleanup activities..");
        tx = entityManager.getTransaction();
        tx.begin();

        cleanUp(entityManager);

        tx.commit();
      } catch (Exception e) {
        LOG.debug("Error during cleanup", e);
        if (tx != null && tx.isActive()) {
          tx.rollback();
        }
      } finally {
        // empty the manager
        entityManager.clear();
        entityManager.close();
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
   * Gets the latest pending items from the queue
   * 
   * @return
   */
  private Queue<T> preparePendingItems() {
    LOG.debug("Retrieving requests to process..");
    // get first all request ids using one entitymanager
    Queue<T> requestIds = null;
    EntityManager entityManager = JpaManager.getEntityManager();
    try {
      requestIds = getRequestsToProcess(entityManager);
      if (requestIds == null || requestIds.isEmpty()) {
        LOG.info("No pending requests to process at this time.");
      } else {
        LOG.info(requestIds.size() + " requests to process..");
      }
      return requestIds;
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }

  }

  /**
   * Override and set to true if flushes will only be done on explicit calls and
   * commits
   * 
   * @return
   */
  public boolean flushOnCommitOnly() {
    return false;
  }

  /**
   * Distributes the requests evenly on lists that equate to the thread count
   * 
   * @param requests
   * @param threadCount
   * @return
   */
  private List<List<T>> allocateRequests(Queue<T> requests, int threadCount) {
    List<List<T>> lists = new ArrayList<List<T>>();
    if (requests.size() < threadCount) {
      for (int i = 0; i < threadCount; i++) {
        T reqId = requests.poll();
        if (reqId != null) {
          lists.add(Collections.singletonList(reqId));
        }
      }
    } else {
      while (requests.peek() != null) {
        for (int i = 0; i < threadCount; i++) {
          if (lists.size() < threadCount) {
            lists.add(new ArrayList<T>());
          }
          T reqId = requests.poll();
          if (reqId != null) {
            List<T> queueForThread = lists.get(i);
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
  public abstract Boolean executeBatchForRequests(EntityManager entityManager, List<T> requests) throws Exception;

  /**
   * Queries the requests on the database and returns a list of the IDs to be
   * included in the processing
   * 
   * @param entityManager
   * @return
   */
  protected abstract Queue<T> getRequestsToProcess(EntityManager entityManager);

  /**
   * Gets the thread name to assign to the worker threads
   * 
   * @return
   */
  protected abstract String getThreadName();

  /**
   * Override to call the cleanup method
   * 
   * @return
   */
  protected boolean hasCleanup() {
    return false;
  }

  /**
   * Called after all batch processing is done, does a cleanup
   * 
   * @throws Exception
   */
  protected void cleanUp(EntityManager entityManager) {
    // NOOP
  }

  /**
   * Override to call the {@link #preProcess(EntityManager)} function
   * 
   * @return
   */
  protected boolean hasPreProcess() {
    return false;
  }

  /**
   * Preprocess function, to be called before multi threading starts
   * 
   * @throws Exception
   */
  protected void preProcess(EntityManager entityManager) {
    // NOOP
  }

  /**
   * Indicates whether this batch supports rollover of queues
   * 
   * @return
   */
  protected boolean rolloverSupported() {
    return false;
  }

  protected int fixedThreadCount(int currCount) {
    return 0;
  }

  /**
   * Updates the current thread name to add more tracking capabilities
   * 
   * @param reqId
   */
  protected void trackRequestLogs(long reqId) {
    Thread.currentThread().setName("REQ-" + reqId);
  }

  /**
   * Resets the thread name to the original
   */
  protected void resetThreadName() {
    Thread.currentThread().setName(getThreadName() + "-" + Thread.currentThread().getId());
  }
}