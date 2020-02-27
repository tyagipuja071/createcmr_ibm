/**
 * 
 */
package com.ibm.cio.cmr.request.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.listeners.ChangeLogListener;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;

/**
 * Simple Service base class to handle more generic services
 * 
 * @author Jeffrey Zamora
 * 
 */
public abstract class BaseSimpleService<R> {

  private static final Logger LOG = Logger.getLogger(BaseSimpleService.class);

  public R process(HttpServletRequest request, ParamContainer params) throws CmrException {
    EntityManager entityManager = JpaManager.getEntityManager();
    EntityTransaction tx = null;
    try {

      if (isTransactional()) {
        LOG.debug("Opening transaction for " + getClass().getName());
        tx = entityManager.getTransaction();
        tx.begin();
        ChangeLogListener.setManager(entityManager);
      }
      R ret = doProcess(entityManager, request, params);

      if (isTransactional() && tx != null && !tx.getRollbackOnly()) {
        LOG.debug("Committing transaction..");
        tx.commit();
      }

      return ret;

    } catch (Exception e) {
      if (isTransactional() && tx != null && tx.isActive()) {
        LOG.debug("Rolling back transaction..");
        tx.rollback();
      }
      LOG.error("Error in processing.", e);
      // only wrap non CmrException errors
      if (e instanceof CmrException) {
        throw (CmrException) e;
      } else {
        throw new CmrException(MessageUtil.ERROR_GENERAL);
      }
    } finally {
      if (isTransactional()) {
        ChangeLogListener.clearManager();
      }
      // empty the manager
      entityManager.clear();
      entityManager.close();
    }

  }

  /**
   * Checks if the entity manager needs to open a transaction
   * 
   * @return
   */
  protected boolean isTransactional() {
    return false;
  }

  /**
   * Does the actual processing needed by the service.
   * 
   * @param entityManager
   * @param request
   * @param extraParams
   * @return
   * @throws CmrException
   */
  protected abstract R doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception;

}
