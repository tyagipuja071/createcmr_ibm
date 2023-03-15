package com.ibm.cio.cmr.request.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

public class JpaManager {

  private static final Logger LOG = Logger.getLogger(JpaManager.class);
  private static Map<String, EntityManagerFactory> managerMap = new HashMap<String, EntityManagerFactory>();
  private static String UNIT = "RDC";

  public static EntityManager getEntityManager(String name) {
    EntityManager s = getSessionFactory(name).createEntityManager();
    return s;
  }

  private static String getActualPersistenceUnitName(String name) {
    if ("RDC".equals(UNIT) && "CEDP".equals(name)) {
      return "CEDP_UI";
    }
    return name;
  }

  public static EntityManager getEntityManager() {
    return getEntityManager(UNIT);
  }

  protected static synchronized EntityManagerFactory getSessionFactory(String name) {
    name = getActualPersistenceUnitName(name);
    EntityManagerFactory sf = managerMap.get(name);
    if (sf == null) {
      try {
        if ("BATCH".equals(name)) {
          sf = InjectedEMFactory.createEntityManagerFactory(name, Collections.emptyMap());
        } else {
          sf = Persistence.createEntityManagerFactory(name);
        }
        managerMap.put(name, sf);
      } catch (Exception e) {
        LOG.warn("Persistence Unit " + name + " cannot be initialized.", e);
      }
    }
    return sf;
  }

  /*
   * public static void init() { EntityManagerFactory sf = managerMap.get(UNIT);
   * if (sf == null) { sf = Persistence.createEntityManagerFactory(UNIT);
   * managerMap.put(UNIT, sf); } }
   */

  public static void init() {
    getSessionFactory("RDC");
    getSessionFactory("CEDP");
  }

  public static synchronized void setDefaultUnitName(String unitName) {
    UNIT = unitName;
  }

  /**
   * Calls the JPA method to create the entity
   * 
   * @param entity
   * @param entityManager
   */
  public static void createEntity(Object entity, EntityManager entityManager) {
    entityManager.persist(entity);
    entityManager.flush();
  }
}
