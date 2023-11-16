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
      e.printStackTrace();
      return Collections.emptyList();
    } finally {
      if (rdcMgr != null) {
        rdcMgr.clear();
        rdcMgr.close();
      }
    }
  }

  private void updateTransService(List<TransService> transServicesList, Boolean activation) {
    EntityManager rdcMgr = null;
    EntityTransaction transaction = null;

    try {
      rdcMgr = JpaManager.getEntityManager();
      transaction = rdcMgr.getTransaction();
      transaction.begin();

      if (transServicesList != null && !transServicesList.isEmpty()) {
        Timestamp logDelTimestamp = activation ? Timestamp.valueOf("9999-12-31 00:00:00") : SystemUtil.getActualTimestamp();
        setTransServiceTimestamp(rdcMgr, transServicesList, logDelTimestamp);
      }
      transaction.commit();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      if (rdcMgr != null) {
        rdcMgr.clear();
        rdcMgr.close();
      }
    }
  }

  private void setTransServiceTimestamp(EntityManager rdcMgr, List<TransService> transServicesList, Timestamp time) {
    for (TransService ts : transServicesList) {
      ts.setLogDelTimestamp(time);
      rdcMgr.merge(ts);
      rdcMgr.flush();
    }
  }

}
