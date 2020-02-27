package com.ibm.cio.cmr.request.util;

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

  public static EntityManager getEntityManager() {
    return getEntityManager(UNIT);
  }

  protected static synchronized EntityManagerFactory getSessionFactory(String name) {
    EntityManagerFactory sf = managerMap.get(name);
    if (sf == null) {
      try {
        sf = Persistence.createEntityManagerFactory(name);
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

  public static void setDefaultUnitName(String unitName) {
    UNIT = unitName;
  }
}
