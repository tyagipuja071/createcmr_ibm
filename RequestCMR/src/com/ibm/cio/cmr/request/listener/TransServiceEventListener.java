package com.ibm.cio.cmr.request.listener;

import java.sql.Timestamp;
import java.util.Arrays;
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
  public static final List<String> EMEA_DB2_COUNTRIES = Arrays.asList("624", "788", "603", "607", "358", "626", "699", "644", "704", "668", "651",
      "740", "694", "695", "705", "787", "820", "826", "821", "707", "693", "708", "363", "359", "889", "741", "758", "680", "610", "620", "840",
      "636", "841", "645", "692", "669", "810", "881", "667", "662", "865", "383", "745", "698", "656", "753", "725", "691", "879", "675", "750",
      "752", "637", "762", "764", "767", "768", "770", "772", "700", "769", "382", "717", "373", "642", "782", "880", "804", "805", "808", "823",
      "670", "831", "827", "832", "635", "876", "833", "835", "864", "842", "850", "851", "718", "729", "862", "857", "677", "849", "883", "825",
      "678", "702", "806", "846", "726", "755", "822", "838", "666", "866", "754");

  @PrePersist
  @PreUpdate
  public void updateTransServiceBasedOnActiveOrInactiveKna1(Kna1 kna1) {
    try {
      String kunnr = kna1.getId().getKunnr();
      if (EMEA_DB2_COUNTRIES.contains(kna1.getKatr6())) {
        if (kna1.getZzkvNode2() == null) {
          kna1.setZzkvNode2("");
        }
      }
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
