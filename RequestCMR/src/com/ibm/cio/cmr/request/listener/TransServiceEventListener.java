package com.ibm.cio.cmr.request.listener;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.TransService;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.SystemUtil;

public class TransServiceEventListener {
  private static final Logger LOG = Logger.getLogger(TransServiceEventListener.class);
  private static final ThreadLocal<EntityManager> currentEntityManager = new ThreadLocal<>();

  @PrePersist
  @PreUpdate
  public void updateTransServiceBasedOnActiveOrInactiveKna1(Kna1 kna1) {
    try {
      String kunnr = kna1.getId().getKunnr();
      if (isDeactivation(kna1)) {
        updateTransServiceForDeactivation(kunnr);
        return;
      }

      updateTransServiceForActivation(kunnr);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Boolean isDeactivation(Kna1 kna1) {
    return "93".equals(kna1.getAufsd()) && "X".equals(kna1.getLoevm());
  }

  public void updateTransServiceForDeactivation(String kunnr) {
    Boolean isActivation = false;
    List<TransService> activeTransServices = getTransService(kunnr, isActivation);
    if (activeTransServices.isEmpty()) {
      LOG.info("No Active TransService found for the kunnr: " + kunnr);
      return;
    }

    LOG.info("Deactivating all TransServices records for kunnr: " + kunnr);
    updateTransService(activeTransServices, isActivation);
  }

  public void updateTransServiceForActivation(String kunnr) {
    Boolean isActivation = true;
    List<TransService> inactiveTransServices = getTransService(kunnr, isActivation);
    if (inactiveTransServices.isEmpty()) {
      LOG.info("No Inactive TransService records found for the kunnr: " + kunnr);
      return;
    }

    LOG.info("Activating all TransServices records for kunnr: " + kunnr);
    updateTransService(inactiveTransServices, isActivation);
  }

  private List<TransService> getTransService(String kunnr, Boolean isActivation) {
    EntityManager rdcMgr = null;
    try {
      rdcMgr = JpaManager.getEntityManager();
      String queryKey = isActivation ? "QUERY.GET_INACTIVE_TS_BY_KUNNR_TIME" : "QUERY.GET_TS_BY_KUNNR_TIME";
      String sql = ExternalizedQuery.getSql(queryKey);
      PreparedQuery query = new PreparedQuery(rdcMgr, sql);
      query.setParameter("KUNNR", kunnr);
      return query.getResults(TransService.class);
    } catch (Exception e) {
      LOG.error("Error querying TransServices for kunnr " + kunnr, e);
      return Collections.emptyList();
    } finally {
      if (rdcMgr != null) {
        rdcMgr.close();
      }
    }
  }

  private void updateTransService(List<TransService> transServicesList, Boolean activation) {
    EntityManager rdcMgr = null;
    EntityTransaction transaction = null;
    Boolean ownTransaction = true;
    try {
      rdcMgr = loadEntityManager(ownTransaction);
      transaction = getTransaction(ownTransaction, rdcMgr);

      if (transServicesList != null && !transServicesList.isEmpty()) {
        Timestamp logDelTimestamp = activation ? Timestamp.valueOf("9999-12-31 00:00:00") : SystemUtil.getActualTimestamp();
        updateTransServiceTimestamp(rdcMgr, transServicesList, logDelTimestamp);
      }

      if (ownTransaction) {
        transaction.commit();
      }

    } catch (Exception e) {
      LOG.error("Could not update TransService records.", e);
      if (ownTransaction) {
        transaction.rollback();
      }
    } finally {
      if (ownTransaction) {
        rdcMgr.close();
      }
    }
  }

  private EntityManager loadEntityManager(Boolean ownTransaction) {
    EntityManager entityManager = currentEntityManager.get();
    if (entityManager != null) {
      ownTransaction = false;
      LOG.trace("Using thread local entity manager..");
    } else {
      LOG.debug("Opening own changelog entity manager..");
      entityManager = JpaManager.getEntityManager();
    }

    return entityManager;
  }

  private EntityTransaction getTransaction(Boolean ownTransaction, EntityManager rdcMgr) {
    EntityTransaction transaction = rdcMgr.getTransaction();
    if (ownTransaction) {
      transaction.begin();
    }

    return transaction;
  }

  private void updateTransServiceTimestamp(EntityManager rdcMgr, List<TransService> transServicesList, Timestamp time) {
    for (TransService ts : transServicesList) {
      ts.setLogDelTimestamp(time);
      rdcMgr.merge(ts);
      rdcMgr.flush();
    }
  }

}
