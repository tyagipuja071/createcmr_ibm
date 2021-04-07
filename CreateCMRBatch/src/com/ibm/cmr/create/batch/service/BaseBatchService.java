/**
 * 
 */
package com.ibm.cmr.create.batch.service;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.BaseEntity;
import com.ibm.cio.cmr.request.entity.BaseEntityPk;
import com.ibm.cio.cmr.request.entity.ReqCmtLog;
import com.ibm.cio.cmr.request.entity.ReqCmtLogPK;
import com.ibm.cio.cmr.request.entity.WfHist;
import com.ibm.cio.cmr.request.entity.WfHistPK;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.SystemUtil;
import com.ibm.cmr.create.batch.entry.BatchEntryPoint;
import com.ibm.cmr.create.batch.util.BatchUtil;
import com.ibm.cmr.create.batch.util.TerminatorThread;

/**
 * Service for batch programs. This will have no entities and models to work on
 * 
 * @author Jeffrey Zamora
 * 
 */
public abstract class BaseBatchService extends BaseSimpleService<Boolean> {

  protected Logger LOG;
  private final List<Throwable> exceptionList;
  private long startTime;
  private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
  protected static String BATCH_USER_ID = "CreateCMR";
  public static String BATCH_SERVICE_URL = "";
  protected TerminatorThread terminator = null;

  /**
   * Constructor
   */
  public BaseBatchService() {
    BATCH_USER_ID = SystemConfiguration.getValue("BATCH_USERID");
    BATCH_SERVICE_URL = SystemConfiguration.getValue("BATCH_SERVICES_URL");
    LOG = Logger.getLogger(getClass());
    this.exceptionList = new ArrayList<Throwable>();
    if (useServicesConnections()) {
      initSSL();
    }
  }

  /**
   * Adds an exception encountered to the service
   * 
   * @param error
   */
  protected void addError(Throwable error) {
    this.exceptionList.add(error);
  }

  /**
   * Adds an exception message encountered to the service
   * 
   * @param errorMessage
   */
  protected void addError(String errorMessage) {
    this.exceptionList.add(new Exception(errorMessage));
  }

  /**
   * Executes the batch application. This will be the function called by entry
   * points extending {@link BatchEntryPoint}
   * 
   * @throws CmrException
   */
  public void execute() throws CmrException {

    this.startTime = new Date().getTime();

    LOG.info("Starting application at " + TIME_FORMATTER.format(new Date(this.startTime)));
    LOG.info("Executing " + getClass().getSimpleName() + " batch application..");
    int exitCode = 0;
    if (process(null, null)) {
      LOG.info("Successfully completed.");
    } else {
      LOG.info("Completed with Errors.");
      int cnt = 1;
      for (Throwable e : this.exceptionList) {
        LOG.error("Error " + cnt + " = " + e.getMessage());
        cnt++;
      }
      exitCode = 1;
    }
    long endTime = new Date().getTime();
    long elapsed = (endTime - startTime) / 1000;
    LOG.info("Application finished execution at " + TIME_FORMATTER.format(new Date(endTime)));
    LOG.info("Took " + elapsed + " seconds.");
    LOG.info("System exiting...");
    Runtime.getRuntime().halt(0);
    // System.exit(0);
  }

  @Override
  public Boolean process(HttpServletRequest request, ParamContainer params) throws CmrException {
    EntityManager entityManager = JpaManager.getEntityManager();
    EntityTransaction transaction = null;
    try {

      int terminatorTime = 40; // 40 mins default;
      String terminatorMins = BatchUtil.getProperty("TERMINATOR.MINS");
      if (!StringUtils.isEmpty(terminatorMins) && StringUtils.isNumeric(terminatorMins)) {
        terminatorTime = Integer.parseInt(terminatorMins);
      }

      if (terminateOnLongExecution()) {
        LOG.info("Starting terminator thread..");
        this.terminator = new TerminatorThread(1000 * 60 * terminatorTime, entityManager);
        this.terminator.start();
      } else {
        LOG.warn("Terminator thread skipped for the run.");
      }

      LOG.info("Batch User ID: " + BATCH_USER_ID);
      if (isTransactional()) {
        ChangeLogListener.setManager(entityManager);
        ChangeLogListener.setUser(BATCH_USER_ID);
        transaction = entityManager.getTransaction();
        transaction.begin();
      }

      Boolean executionStatus = doProcess(entityManager, request, params);

      if (isTransactional() && transaction != null && transaction.isActive() && !transaction.getRollbackOnly()) {
        transaction.commit();
      }

      return executionStatus;
    } catch (Throwable e) {
      addError(e);
      LOG.error("An error was encountered during processing. Transaction will be rolled back.", e);
      if (isTransactional() && transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      return false;
    } finally {
      if (isTransactional()) {
        ChangeLogListener.clearManager();
      }
      if (isTransactional() && transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }

  }

  /**
   * Where the actual processing logic for the service will take place.
   * Extending classes should implement this and use only (if possible) the
   * entity manager passed as parameter
   * 
   * @param entityManager
   * @return
   * @throws Exception
   */
  protected abstract Boolean executeBatch(EntityManager entityManager) throws Exception;

  @Override
  protected Boolean doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    return executeBatch(entityManager);
  }

  @Override
  protected abstract boolean isTransactional();

  /**
   * Copies the values from the entity (JPA) to the model
   * 
   * @param from
   * @param to
   */
  public void copyValuesFromEntity(Object from, Object to) {
    try {
      PropertyUtils.copyProperties(to, from);

      if (from instanceof BaseEntity<?>) {
        BaseEntity<?> ent = (BaseEntity<?>) from;
        BaseEntityPk id = ent.getId();

        PropertyUtils.copyProperties(to, id);
      }

    } catch (Exception e) {
      e.printStackTrace();
      // noop
    }
  }

  /**
   * Copies the values of the model to the entity (JPA)
   * 
   * @param from
   * @param to
   */
  public void copyValuesToEntity(Object from, Object to) {
    try {
      PropertyUtils.copyProperties(to, from);

      if (to instanceof BaseEntity<?>) {
        BaseEntity<?> ent = (BaseEntity<?>) to;
        BaseEntityPk id = ent.getId();

        PropertyUtils.copyProperties(id, from);
      }

    } catch (Exception e) {

    }
  }

  /**
   * Calls the JPA method to update the entity
   * 
   * @param entity
   * @param entityManager
   */
  public synchronized void updateEntity(BaseEntity<?> entity, EntityManager entityManager) {
    entityManager.merge(entity);
    entityManager.flush();
  }

  /**
   * Calls the JPA method to create the entity
   * 
   * @param entity
   * @param entityManager
   */
  public synchronized void createEntity(BaseEntity<?> entity, EntityManager entityManager) {
    entityManager.persist(entity);
    entityManager.flush();
  }

  /**
   * Calls the JPA method to delete the entity
   * 
   * @param entity
   * @param entityManager
   */
  public synchronized void deleteEntity(BaseEntity<?> entity, EntityManager entityManager) {
    BaseEntity<?> merged = entityManager.merge(entity);
    if (merged != null) {
      entityManager.remove(merged);
    }
    entityManager.flush();
  }

  /**
   * Commits the current transaction actions, and resumes the transaction
   * 
   * @param entityManager
   */
  public synchronized void partialCommit(EntityManager entityManager) {
    EntityTransaction transaction = entityManager.getTransaction();
    if (transaction != null && transaction.isActive() && !transaction.getRollbackOnly()) {
      LOG.debug("Transaction partially committed");
      transaction.commit();
      transaction.begin();
    } else if (transaction != null && transaction.isActive() && transaction.getRollbackOnly()) {
      LOG.debug("Transaction partially rolled back and resumed.");
      transaction.rollback();
      transaction.begin();
    }

    if (this.terminator != null) {
      this.terminator.keepAlive();
    }

  }

  /**
   * Rolls back the actions on the current transaction, and resumes the
   * transaction
   * 
   * @param entityManager
   */
  public synchronized void partialRollback(EntityManager entityManager) {
    EntityTransaction transaction = entityManager.getTransaction();
    if (transaction != null && transaction.isActive()) {
      LOG.debug("Transaction partially rolled back");
      transaction.rollback();
      transaction.begin();
    }
    if (this.terminator != null) {
      this.terminator.keepAlive();
    }
  }

  public synchronized void refresh(EntityManager entityManager, Object entity) {
    try {
      entityManager.refresh(entity);
    } catch (Exception e) {
      LOG.error("An error occurred while refreshing the data.", e);
    }
  }

  protected void initSSL() {
    try {
      String dir = SystemConfiguration.getValue("CMR_HOME");
      if (!StringUtils.isEmpty(dir)) {
        LOG.info("Initializing SSL context using keystore (home): " + dir + BatchUtil.getProperty("ssl.keystore.loc.relative"));
        System.setProperty("javax.net.ssl.keyStore", dir + BatchUtil.getProperty("ssl.keystore.loc.relative"));
        System.setProperty("javax.net.ssl.trustStore", dir + BatchUtil.getProperty("ssl.keystore.loc.relative"));
      } else {
        LOG.info("Initializing SSL context using keystore: " + BatchUtil.getProperty("ssl.keystore.loc"));
        System.setProperty("javax.net.ssl.keyStore", BatchUtil.getProperty("ssl.keystore.loc"));
        System.setProperty("javax.net.ssl.trustStore", BatchUtil.getProperty("ssl.keystore.loc"));
      }
      System.setProperty("javax.net.ssl.keyStorePassword", BatchUtil.getProperty("ssl.keystore.pass"));

      HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

        @Override
        public boolean verify(String arg0, SSLSession arg1) {
          return true;
        }
      });
    } catch (Exception e) {
      LOG.error("Error in initializing SSL context");
    }
  }

  /**
   * Creates a {@link WfHist} record
   * 
   * @param entityManager
   * @param comment
   * @param status
   * @param action
   * @param reqId
   * @throws SQLException
   * @throws CmrException
   */
  protected WfHist createHistory(EntityManager entityManager, String comment, String status, String action, long reqId)
      throws CmrException, SQLException {
    // create workflow history record
    WfHist hist = new WfHist();
    WfHistPK histPk = new WfHistPK();
    long wfId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "WF_ID");
    histPk.setWfId(wfId);
    hist.setId(histPk);
    if (comment != null && comment.length() > 250) {
      hist.setCmt(comment.substring(0, 250));
    } else {
      hist.setCmt(comment);
    }
    hist.setCreateById(BATCH_USER_ID);
    hist.setCreateByNm(BATCH_USER_ID);
    hist.setCreateTs(SystemUtil.getCurrentTimestamp());
    hist.setReqId(reqId);
    hist.setReqStatus(status);
    hist.setReqStatusAct(action);
    createEntity(hist, entityManager);

    return hist;
  }

  /**
   * Creates a {@link ReqCmtLog} record
   * 
   * @param entityManager
   * @param comment
   * @param reqId
   * @throws SQLException
   * @throws CmrException
   */
  protected void createComment(EntityManager entityManager, String comment, long reqId) throws CmrException, SQLException {
    // create request comment
    ReqCmtLog cmt = new ReqCmtLog();
    ReqCmtLogPK cmtPk = new ReqCmtLogPK();
    long cmtId = SystemUtil.getNextID(entityManager, SystemConfiguration.getValue("MANDT"), "CMT_ID");
    cmtPk.setCmtId(cmtId);
    cmt.setId(cmtPk);
    cmt.setCmt(comment);
    cmt.setCmtLockedIn("Y");
    cmt.setCreateById(BATCH_USER_ID);
    cmt.setCreateByNm(BATCH_USER_ID);
    cmt.setCreateTs(SystemUtil.getCurrentTimestamp());
    cmt.setReqId(reqId);
    cmt.setUpdateTs(cmt.getCreateTs());
    createEntity(cmt, entityManager);
  }

  protected boolean useServicesConnections() {
    return false;
  }

  /**
   * Switch to determine if the batch should skip the terminator thread
   * 
   * @return
   */
  protected boolean terminateOnLongExecution() {
    return true;
  }

}
