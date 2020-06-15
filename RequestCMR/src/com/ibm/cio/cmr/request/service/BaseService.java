/**
 * 
 */
package com.ibm.cio.cmr.request.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.BaseEntity;
import com.ibm.cio.cmr.request.entity.BaseEntityPk;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * Base Service to handle the transactions
 * 
 * @author Jeffrey Zamora
 * 
 */
public abstract class BaseService<M extends BaseModel, E extends BaseEntity<?>> {

  protected Logger log = initLogger();

  protected abstract Logger initLogger();

  protected Timestamp currentTimestamp;

  /**
   * Processes saving of the current model
   * 
   * @param model
   * @param request
   * @return
   * @throws CmrException
   */
  @SuppressWarnings("unchecked")
  public M save(M model, HttpServletRequest request) throws CmrException {
    log.debug("Entering save()");
    EntityTransaction transaction = null;
    EntityManager entityManager = JpaManager.getEntityManager();
    try {

      ChangeLogListener.setManager(entityManager);
      assignCurrentTimestamp(entityManager);
      // start the transaction
      transaction = entityManager.getTransaction();
      transaction.begin();
      String action = model.getAction();

      E current = null;
      if (BaseModel.ACT_INSERT.equals(action)) {
        log.debug("Inserting " + model);
        // insert record process

        // create new entity
        current = createFromModel(model, entityManager, request);

        // insert
        doBeforeInsert(current, entityManager, request);
        createEntity(current, entityManager);
        doAfterInsert(current, entityManager, request);

        // switch model's state to existing after creation
        model.setState(BaseModel.STATE_EXISTING);
        model.setAction(BaseModel.ACT_UPDATE);
      } else if (BaseModel.ACT_UPDATE.equals(action) || BaseModel.ACT_DELETE.equals(action)) {

        // gets the current record from the database
        current = getCurrentRecord(model, entityManager, request);

        if (current == null) {
          throw new CmrException(MessageUtil.ERROR_CANNOT_GET_CURRENT, model.getRecordDescription());
        }
        if (BaseModel.ACT_UPDATE.equals(action)) {
          // update record process

          log.debug("Updating " + model);
          copyValuesToEntity(model, current);
          doBeforeUpdate(current, entityManager, request);
          updateEntity(current, entityManager);
          doAfterUpdate(current, entityManager, request);

        } else if (BaseModel.ACT_DELETE.equals(action)) {
          // delete record process

          log.debug("Deleting " + model);
          doBeforeDelete(current, entityManager, request);
          deleteEntity(current, entityManager);
          doAfterDelete(current, entityManager, request);

        }
      }

      log.debug("Committing transaction...");
      // commit only once
      transaction.commit();

      // create a copy of a new model, instead of keeping only one
      M newModel = (M) model.getClass().newInstance();
      copyValuesFromEntity(current, newModel);
      return newModel;
    } catch (Exception e) {
      if (e instanceof CmrException) {
        log.error("CMR Error:" + ((CmrException) e).getMessage());
      } else {
        // log only unexpected errors, exclude validation errors
        log.error("Error in saving record " + model, e);
      }
      // rollback transaction when exception occurs
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }

      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      // try to rollback, for safekeeping
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }

      ChangeLogListener.clearManager();
      // empty the manager
      entityManager.clear();
      entityManager.close();
      log.debug("Exiting save()");
    }
  }

  /**
   * Processes generic transactions
   * 
   * @param model
   * @param request
   * @throws CmrException
   */
  public void processTransaction(M model, HttpServletRequest request) throws CmrException {
    EntityTransaction transaction = null;
    EntityManager entityManager = JpaManager.getEntityManager();
    try {

      ChangeLogListener.setManager(entityManager);

      assignCurrentTimestamp(entityManager);
      // start the transaction
      transaction = entityManager.getTransaction();
      transaction.begin();

      log.debug("Transaction Action: " + model.getAction());

      // perform the transaction
      performTransaction(model, entityManager, request);

      // commit once
      transaction.commit();
      log.debug(" - transaction committed.");

    } catch (Exception e) {
      if (e instanceof CmrException) {
        log.error("CMR Error:" + ((CmrException) e).getMessage());
      } else {
        // log only unexpected errors, exclude validation errors
        log.error("Error in processing transaction " + model, e);
      }
      // rollback transaction when exception occurs
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }

      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        CmrException cmre = new CmrException(MessageUtil.ERROR_GENERAL, e);
        throw cmre;
      }
    } finally {
      // try to rollback, for safekeeping
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      ChangeLogListener.clearManager();

      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
  }

  /**
   * Performs a search on the database based on the model's values. This is
   * non-transactional
   * 
   * @param model
   * @param request
   * @return
   * @throws CmrException
   */
  public List<M> search(M model, HttpServletRequest request) throws CmrException {
    EntityManager entityManager = JpaManager.getEntityManager();
    try {

      log.debug("Performing search on model " + model);

      // do the actual search
      List<M> results = doSearch(model, entityManager, request);

      doAfterSearch(model, entityManager, request);

      log.debug("Search completed.");
      return results;

    } catch (Exception e) {
      if (e instanceof CmrException) {
        log.error("CMR Error:" + ((CmrException) e).getMessage());
      } else {
        // log only unexpected errors, exclude validation errors
        log.error("Error in searching for records " + model, e);
      }
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }
  }

  /**
   * Copies the values from the entity (JPA) to the model
   * 
   * @param from
   * @param to
   */
  public void copyValuesFromEntity(E from, M to) {
    try {
      PropertyUtils.copyProperties(to, from);

      BaseEntityPk id = from.getId();

      PropertyUtils.copyProperties(to, id);

    } catch (Exception e) {
      this.log.error("Error when copying properties", e);
      // noop
    }
  }

  /**
   * Copies the values of the model to the entity (JPA)
   * 
   * @param from
   * @param to
   */
  public void copyValuesToEntity(M from, E to) {
    try {
      PropertyUtils.copyProperties(to, from);

      BaseEntityPk id = to.getId();

      PropertyUtils.copyProperties(id, from);

    } catch (Exception e) {

    }
  }

  /**
   * Calls the JPA method to update the entity
   * 
   * @param entity
   * @param entityManager
   */
  public void updateEntity(BaseEntity<?> entity, EntityManager entityManager) {
    entityManager.merge(entity);
    entityManager.flush();
  }

  /**
   * Calls the JPA method to create the entity
   * 
   * @param entity
   * @param entityManager
   */
  public void createEntity(BaseEntity<?> entity, EntityManager entityManager) {
    entityManager.persist(entity);
    entityManager.flush();
  }

  /**
   * Calls the JPA method to delete the entity
   * 
   * @param entity
   * @param entityManager
   */
  public void deleteEntity(BaseEntity<?> entity, EntityManager entityManager) {
    entityManager.remove(entity);
    entityManager.flush();
  }

  /**
   * Commits the current transaction actions, and resumes the transaction
   * 
   * @param entityManager
   */
  public void partialCommit(EntityManager entityManager) {
    EntityTransaction transaction = entityManager.getTransaction();
    if (transaction != null && transaction.isActive() && !transaction.getRollbackOnly()) {
      transaction.commit();
      this.log.debug("Transaction partially committed");
      transaction.begin();
    }
  }

  /**
   * Rolls back the actions on the current transaction, and resumes the
   * transaction
   * 
   * @param entityManager
   */
  public void partialRollback(EntityManager entityManager) {
    EntityTransaction transaction = entityManager.getTransaction();
    if (transaction != null && transaction.isActive()) {
      transaction.rollback();
      this.log.debug("Transaction partially rolled back");
      transaction.begin();
    }
  }

  /**
   * Extract the keys from the model based on the gridChk field.
   * 
   * @param model
   * @return
   */
  protected List<KeyContainer> extractKeys(M model) {
    List<KeyContainer> list = new ArrayList<KeyContainer>();
    if (model.getGridchk() == null) {
      return list;
    }

    KeyContainer cont = null;
    for (String chkValue : model.getGridchk()) {
      cont = new KeyContainer(model);
      cont.parse(chkValue);
      list.add(cont);
    }

    return list;
  }

  /**
   * Performs the actual generic transaction to be processed
   * 
   * @param model
   * @param entityManager
   * @param request
   * @throws CmrException
   */
  protected abstract void performTransaction(M model, EntityManager entityManager, HttpServletRequest request) throws Exception;

  /**
   * Performs the actual search on the database
   * 
   * @param model
   * @param entityManager
   * @param request
   * @return
   * @throws CmrException
   */
  protected abstract List<M> doSearch(M model, javax.persistence.EntityManager entityManager, HttpServletRequest request) throws Exception;

  /**
   * Gets the current record on the database for the given model
   * 
   * @param model
   * @param entityManager
   * @param request
   * @return
   * @throws CmrException
   */
  protected abstract E getCurrentRecord(M model, EntityManager entityManager, HttpServletRequest request) throws Exception;

  /**
   * Creates a new detached JPA entity from the model
   * 
   * @param model
   * @param entityManager
   * @param request
   * @return
   * @throws CmrException
   */
  protected abstract E createFromModel(M model, EntityManager entityManager, HttpServletRequest request) throws CmrException;

  /**
   * Called before the doSearch method, in case any process should be done
   * before querying
   * 
   * @param model
   * @param entityManager
   * @param request
   * @throws CmrException
   */
  protected void doBeforeSearch(M model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop, override if needed;
  }

  /**
   * Called after the doSearch method, in case any process should be done after
   * querying
   * 
   * @param model
   * @param entityManager
   * @param request
   * @throws CmrException
   */
  protected void doAfterSearch(M model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop, override if needed;
  }

  /**
   * Called before the actual insert call, where validations on the database can
   * be performed and other logical processing
   * 
   * @param entity
   * @param entityManager
   * @param request
   * @throws CmrException
   */
  protected void doBeforeInsert(E entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop, override if needed
  }

  /**
   * Called after the actual insert call, where extra logic can be executed
   * after inserting to the database
   * 
   * @param entity
   * @param entityManager
   * @param request
   * @throws CmrException
   */
  protected void doAfterInsert(E entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    if (isSystemAdminService()) {
      Table table = entity.getClass().getAnnotation(Table.class);
      String tblName = entity.getClass().getSimpleName().toUpperCase();
      if (table != null) {
        tblName = table.name();
      }
      String cntry = SystemUtil.getIssuingCountry(entity);
      String field = SystemUtil.getFieldId(entity);
      if ("?".equals(field)) {
        field = SystemUtil.getRelevantKey(entity);
      }
      SystemUtil.logSystemAdminAction(entityManager, AppUser.getUser(request), tblName, "I", field, cntry, "", "");
    }
  }

  /**
   * Called before the actual update call, where validations on the database can
   * be performed and other logical processing
   * 
   * @param entity
   * @param entityManager
   * @param request
   * @throws CmrException
   */
  protected void doBeforeUpdate(E entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop, override if needed
  }

  /**
   * Called after the actual update call, where extra logic can be executed
   * after update to the database
   * 
   * @param entity
   * @param entityManager
   * @param request
   * @throws CmrException
   */
  protected void doAfterUpdate(E entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    if (isSystemAdminService()) {
      Table table = entity.getClass().getAnnotation(Table.class);
      String tblName = entity.getClass().getSimpleName().toUpperCase();
      if (table != null) {
        tblName = table.name();
      }
      String cntry = SystemUtil.getIssuingCountry(entity);
      String field = SystemUtil.getFieldId(entity);
      if ("?".equals(field)) {
        field = SystemUtil.getRelevantKey(entity);
      }
      SystemUtil.logSystemAdminAction(entityManager, AppUser.getUser(request), tblName, "U", field, cntry, "", "");
    }
  }

  /**
   * Called before the actual delete call, where validations on the database can
   * be performed and other logical processing
   * 
   * @param entity
   * @param entityManager
   * @param request
   * @throws CmrException
   */
  protected void doBeforeDelete(E entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // noop, override if needed
  }

  /**
   * Called after the actual delete call, where extra logic can be executed
   * after deleting from the database
   * 
   * @param entity
   * @param entityManager
   * @param request
   * @throws CmrException
   */
  protected void doAfterDelete(E entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    if (isSystemAdminService()) {
      Table table = entity.getClass().getAnnotation(Table.class);
      String tblName = entity.getClass().getSimpleName().toUpperCase();
      if (table != null) {
        tblName = table.name();
      }
      String cntry = SystemUtil.getIssuingCountry(entity);
      String field = SystemUtil.getFieldId(entity);
      if ("?".equals(field)) {
        field = SystemUtil.getRelevantKey(entity);
      }
      SystemUtil.logSystemAdminAction(entityManager, AppUser.getUser(request), tblName, "I", field, cntry, "", "");
    }
  }

  /**
   * Used to concatenate values from 2 different strings
   * 
   * @param value1
   * @param value1
   * @return
   */
  protected String concat(String value1, String value2) {
    if (!StringUtils.isEmpty(value2)) {
      if (StringUtils.isEmpty(value1)) {
        return value2.trim();
      } else {
        return value1.trim() + " " + value2.trim();
      }
    } else {
      if (StringUtils.isEmpty(value1)) {
        return null;
      } else {
        return value1.trim();
      }
    }
  }

  private void assignCurrentTimestamp(EntityManager entityManager) {
    PreparedQuery q = new PreparedQuery(entityManager, "select current_timestamp from sysibm.sysdummy1");
    this.currentTimestamp = q.getSingleResult(Timestamp.class);
  }

  protected boolean isSystemAdminService() {
    return false;
  }
}
