/**
 * 
 */
package com.ibm.cio.cmr.request.entity.listeners;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.GenChangelog;
import com.ibm.cio.cmr.request.entity.GenChangelogPK;
import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.entity.Kna1PK;
import com.ibm.cio.cmr.request.entity.USEnterprise;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.JpaManager;

/**
 * Entity listener for creating GEN_CHANGE_LOG records
 * 
 * @author
 * 
 */
public class GenChangeLogListener {

  private static final Logger LOG = Logger.getLogger(GenChangeLogListener.class);

  private static final String ACTION_INSERT = "I";
  private static final String ACTION_UPDATE = "U";
  private static final String ACTION_DELETE = "D";

  private static final ThreadLocal<GenChangeLogParentContainer> storedParent = new ThreadLocal<>();
  private static final ThreadLocal<Map<String, Object>> storedChildren = new ThreadLocal<>();
  private static final ThreadLocal<EntityManager> currentEntityManager = new ThreadLocal<>();

  /**
   * Set the parent record, for tracking
   * 
   * @param parentObject
   */
  public static void setAsParentObject(Kna1 parentObject, String reqType, long reqId) {
    GenChangeLogParentContainer container = new GenChangeLogParentContainer();
    GenChangeLogDetails details = getGenChangeLogDetails(parentObject);
    if (details == null) {
      return;
    }
    container.setTabKey1((String) getFieldValue(Kna1.class, details.tab_key1(), parentObject));
    container.setTabKey2((String) getFieldValue(Kna1.class, details.tab_key2(), parentObject));
    container.setMandt((String) getFieldValue(Kna1.class, details.mandt(), parentObject));
    container.setChangeBy((String) getFieldValue(Kna1.class, details.change_by(), parentObject));

    container.setReqType(reqType);
    container.setReqId(reqId);
    storedParent.set(container);

    LOG.debug(">>> setAsParentObject >>> Incoming reqType=" + container.getReqType() + ", reqId=" + container.getReqId());
  }

  public static void setAsCurrentObject(USEnterprise entObject, String reqType, long reqId) {
    LOG.debug("setAsCurrentObject()....");
    GenChangeLogParentContainer container = new GenChangeLogParentContainer();
    GenChangeLogDetails details = getGenChangeLogDetails(entObject);
    if (details == null) {
      return;
    }

    container.setMandt((String) getFieldValue(USEnterprise.class, details.mandt(), entObject));
    container.setTabKey1((String) getFieldValue(USEnterprise.class, details.tab_key1(), entObject));
    container.setTabKey2((String) getFieldValue(USEnterprise.class, details.tab_key2(), entObject));
    container.setChangeBy((String) getFieldValue(USEnterprise.class, details.change_by(), entObject));

    container.setReqType(reqType);
    container.setReqId(reqId);

    LOG.debug(
        ">>>setAsCurrentObject >>> TabKey1=" + container.getTabKey1() + ",TabKey2=" + container.getTabKey2() + ",Mandt=" + container.getMandt());
    LOG.debug(
        ">>>setAsCurrentObject >>> ChangeBy=" + container.getChangeBy() + ",ReqType=" + container.getReqType() + ",ReqId=" + container.getReqId());

    storedParent.set(container);

    LOG.debug(">>> setAsCurrentObject >>> Incoming reqType=" + container.getReqType() + ", reqId=" + container.getReqId());
  }

  public static void clearParent() {
    LOG.debug("Clearing parent and stored children..");
    GenChangeLogParentContainer cont = storedParent.get();
    if (cont != null) {
      cont = null;
    }
    storedParent.remove();
  }

  public void trackChildren(Object entity) throws Exception {
    if (entity == null) {
      return;
    }
    GenChangeLogDetails genChangeLogDetails = entity.getClass().getAnnotation(GenChangeLogDetails.class);
    if (genChangeLogDetails == null) {
      return;
    }
    Table tableAnnot = entity.getClass().getAnnotation(Table.class);
    String table = null;
    if (tableAnnot != null) {
      table = tableAnnot.name();
    }

    String pkValue = constructPk(entity, genChangeLogDetails);
    if (!StringUtils.isEmpty(pkValue)) {
      Object clone = cloneEntity(entity);
      Map<String, Object> children = storedChildren.get();
      if (children == null) {
        children = new HashMap<String, Object>();
      }
      LOG.debug("Putting child: " + table + "|" + pkValue);
      children.put(table + "|" + pkValue, clone);
      storedChildren.set(children);
    }
  }

  /**
   * Creates GEN_CHANGE_LOG records after inserting the entity
   * 
   * @param entity
   * @throws Exception
   */
  @PostPersist
  public void createGenChangeLogForInsert(Object entity) throws Exception {

    GenChangeLogDetails genChangeLogDetails = getGenChangeLogDetails(entity);
    if (genChangeLogDetails == null) {
      // not an entity, no genChangelog annotation, do nothing
      return;
    }

    if (!genChangeLogDetails.logCreates()) {
      return;
    }

    Class<?> markedClass = getMarkedClass(entity);

    /*
     * // if the entity has been processed, skip this step if
     * (isProcessed(entity, markedClass)) { return; }
     */

    boolean ownTransaction = true;
    EntityManager entityManager = currentEntityManager.get();
    if (entityManager != null) {
      ownTransaction = false;
    } else {
      entityManager = JpaManager.getEntityManager();
    }
    try {
      EntityTransaction txn = entityManager.getTransaction();
      try {
        if (ownTransaction) {
          txn.begin();
        }

        createGenChangeLog(markedClass, entityManager, ACTION_INSERT, "", null, null, entity, genChangeLogDetails);

        if (ownTransaction) {
          txn.commit();
          LOG.info("GEN_CHANGE_LOG record committed.");
        }
      } catch (Exception e) {
        LOG.error("Cannot create GEN_CHANGE_LOG record.", e);
        if (ownTransaction) {
          txn.rollback();
        }
      }
    } finally {
      if (ownTransaction) {
        entityManager.close();
      }
    }

    setProcessed(entity, markedClass);
  }

  /**
   * Creates GEN_CHANGE_LOG records before updating the entity
   * 
   * @param entity
   * @throws Exception
   */
  @PreUpdate
  public void createGenChangeLogForUpdate(Object entity) throws Exception {

    GenChangeLogDetails genChangeLogDetails = getGenChangeLogDetails(entity);
    if (genChangeLogDetails == null) {
      // not an entity, no genChangelog annotation, do nothing
      return;
    }

    if (!genChangeLogDetails.logUpdates()) {
      return;
    }

    Class<?> markedClass = getMarkedClass(entity);

    // if the entity has been processed, skip this step
    /*
     * if (isProcessed(entity, markedClass)) { return; }
     */

    boolean ownTransaction = true;
    EntityManager entityManager = currentEntityManager.get();
    if (entityManager != null) {
      ownTransaction = false;
    } else {
      entityManager = JpaManager.getEntityManager();
    }
    try {

      EntityTransaction txn = entityManager.getTransaction();
      try {
        if (ownTransaction) {
          txn.begin();
        }

        Object currentEntity = getCurrentObject(markedClass, entityManager, entity, genChangeLogDetails);
        if (currentEntity == null) {
          return;
        }
        entityManager.detach(currentEntity);

        createGenChangeLogForFieldUpdates(genChangeLogDetails, markedClass, entityManager, entity, currentEntity);

        if (ownTransaction) {
          txn.commit();
          LOG.info("GEN_CHANGE_LOG records committed.");
        }
      } catch (Exception e) {
        LOG.error("Cannot create GEN_CHANGE_LOG record.", e);
        if (ownTransaction) {
          txn.rollback();
        }
      }
    } finally {
      if (ownTransaction) {
        entityManager.close();
      }
    }
    setProcessed(entity, markedClass);

  }

  /**
   * Creates GEN_CHANGE_LOG records before removing the entity
   * 
   * @param entity
   * @throws Exception
   */
  @PreRemove
  public void createGenChangeLogForRemove(Object entity) throws Exception {

    GenChangeLogDetails genChangeLogDetails = getGenChangeLogDetails(entity);
    if (genChangeLogDetails == null) {
      // not an entity, no genChangelog annotation, do nothing
      return;
    }

    if (!genChangeLogDetails.logDeletes()) {
      return;
    }

    Class<?> markedClass = getMarkedClass(entity);

    /*
     * // if the entity has been processed, skip this step if
     * (isProcessed(entity, markedClass)) { return; }
     */

    boolean ownTransaction = true;
    EntityManager entityManager = currentEntityManager.get();
    if (entityManager != null) {
      ownTransaction = false;
    } else {
      entityManager = JpaManager.getEntityManager();
    }
    try {
      EntityTransaction txn = entityManager.getTransaction();
      try {
        if (ownTransaction) {
          txn.begin();
        }

        createGenChangeLog(markedClass, entityManager, ACTION_DELETE, "", null, null, entity, genChangeLogDetails);

        if (ownTransaction) {
          txn.commit();
          LOG.info("GEN_CHANGE_LOG record committed.");
        }
      } catch (Exception e) {
        LOG.error("Cannot create GEN_CHANGE_LOG record.", e);
        if (ownTransaction) {
          txn.rollback();
        }
      }
    } finally {
      if (ownTransaction) {
        entityManager.close();
      }
    }

    setProcessed(entity, markedClass);
  }

  /**
   * Extracts the {@link GenChangeLogDetails} from the relevant class
   * 
   * @param entity
   * @return
   */
  private static GenChangeLogDetails getGenChangeLogDetails(Object entity) {
    Class<?> entityClass = entity.getClass().getSuperclass();
    GenChangeLogDetails details = entityClass.getAnnotation(GenChangeLogDetails.class);
    if (details == null) {
      entityClass = entity.getClass();
      if (entityClass != null) {
        details = entityClass.getAnnotation(GenChangeLogDetails.class);
      }
    }
    return details;
  }

  /**
   * Extracts the relevant class to use. This method has been made safe for
   * runtime subclasses created by JPA
   * 
   * @param entity
   * @return
   */
  private Class<?> getMarkedClass(Object entity) {
    Class<?> entityClass = entity.getClass().getSuperclass();
    GenChangeLogDetails details = entityClass.getAnnotation(GenChangeLogDetails.class);
    if (details == null) {
      entityClass = entity.getClass();
      details = entityClass.getAnnotation(GenChangeLogDetails.class);
      if (details != null) {
        return entityClass;
      }
    } else {
      return entityClass;
    }
    return null;
  }

  /**
   * Creates a GEN_CHANGE_LOG record based on the supplied details
   * 
   * @param entityManager
   * @param action
   * @param field
   * @param oldValue
   * @param newValue
   * @param entity
   * @param genChangeLogDetails
   * @throws Exception
   */
  private void createGenChangeLog(Class<?> entityClass, EntityManager entityManager, String action, String field, String oldValue, String newValue,
      Object entity, GenChangeLogDetails genChangeLogDetails) throws Exception {

    // extract the relevant fields from the entity
    Object mandt = getFieldValue(entityClass, genChangeLogDetails.mandt(), entity);// MANDT
    Object tabKey1 = getFieldValue(entityClass, genChangeLogDetails.tab_key1(), entity);// COMP_NO
    Object tabKey2 = getFieldValue(entityClass, genChangeLogDetails.tab_key2(), entity);// ENT_NO
    // Object changeBy = getFieldValue(entityClass,
    // genChangeLogDetails.change_by(), entity);// USERID

    // CREATCMR-8617
    // Object changeSrcType = getFieldValue(entityClass,
    // genChangeLogDetails.change_src_typ(), entity);// REQ
    // TYPE
    // Object changeSrcId = getFieldValue(entityClass,
    // genChangeLogDetails.change_src_id(), entity);// REQ
    // ID

    Object changeBy = "CreateCMR";

    if (mandt == null) {
      GenChangeLogParentContainer container = storedParent.get();
      if (container != null) {
        // if the parent was set along the way
        mandt = container.getMandt();
      } else {
        // mandt = "100";
        mandt = SystemConfiguration.getValue("MANDT");
      }
    }

    if (tabKey1 == null) {
      GenChangeLogParentContainer container = storedParent.get();
      if (container != null) {
        // if the parent was set along the way
        tabKey1 = container.getTabKey1();
      }
    }

    if (tabKey2 == null) {
      GenChangeLogParentContainer container = storedParent.get();
      if (container != null) {
        // if the parent was set along the way
        tabKey2 = container.getTabKey2();
      }
    }

    String table = getTable(entityClass);

    LOG.debug(">>>createGenChangeLog >>> tabKey1=" + tabKey1 + ",tabKey2=" + tabKey2 + ",changeBy=" + changeBy);
    LOG.debug(">>>createGenChangeLog >>> action=" + action + ",newValue=" + newValue + ",oldValue=" + oldValue);

    String tabKey = null;
    if ("US_COMPANY".equalsIgnoreCase(table)) {
      tabKey = (String) tabKey1;
    }

    if ("US_ENTERPRISE".equalsIgnoreCase(table)) {
      tabKey = (String) tabKey2;
    }

    LOG.debug("GenChangeLogListener >>> MANDT=" + mandt + ", TAB_NM=" + table + ", TAB_KEY=" + tabKey + ", FIELD_NM=" + field);
    if (mandt == null || table == null || tabKey == null || field == null) {
      throw new Exception("One or more required fields is empty. GEN_CHANGE_LOG cannot be created.");
    }

    GenChangelogPK genChangeLogId = new GenChangelogPK();
    genChangeLogId.setField_nm(field);
    genChangeLogId.setTab_key(tabKey.toString());
    genChangeLogId.setMandt(mandt.toString());
    genChangeLogId.setTab_nm(table);
    genChangeLogId.setChgts(new Date());

    GenChangelog genChangelog = new GenChangelog();
    genChangelog.setId(genChangeLogId);
    genChangelog.setAction_ind(action);
    genChangelog.setNew_value(newValue);
    genChangelog.setOld_value(oldValue);

    genChangelog.setChange_by(changeBy.toString());

    genChangelog.setChange_src_typ("ADMIN");// REQUEST TYPE
    genChangelog.setChange_src_id("");// REQUEST
                                      // ID

    // create the log
    LOG.trace("Creating GEN_CHANGE_LOG Record: Table " + table + " Mandt: " + mandt + " Kunnr: " + tabKey + " Action: " + action + " Field: " + field
        + " (" + oldValue + "/" + newValue + ")");
    JpaManager.createEntity(genChangelog, entityManager);

  }

  /**
   * Gets the table name to use. It will check the {@link Table} annotation if
   * it exists, and will use the class's name if not found
   * 
   * @param entityClass
   * @return
   */
  private String getTable(Class<?> entityClass) {
    GenChangeLogDetails details = entityClass.getAnnotation(GenChangeLogDetails.class);
    if (details != null && !StringUtils.isEmpty(details.tab_nm())) {
      return details.tab_nm();
    }
    Table table = entityClass.getAnnotation(Table.class);
    if (table == null) {
      table = entityClass.getSuperclass() != null ? entityClass.getSuperclass().getAnnotation(Table.class) : null;
      if (table == null) {
        return entityClass.getSimpleName().toUpperCase();
      }
    }
    return table.name();
  }

  /**
   * Gets the value of the specific field
   * 
   * @param fieldName
   * @param entity
   * @return
   */
  private static Object getFieldValue(Class<?> entityClass, String fieldName, Object entity) {
    try {

      // for compound fields with . e.g. id.mandt
      // only one level is supported
      if (fieldName.contains(".")) {
        Field parentField = entityClass.getDeclaredField(fieldName.substring(0, fieldName.indexOf(".")));
        if (parentField != null) {
          parentField.setAccessible(true);
          Object parentFieldValue = parentField.get(entity);
          if (parentFieldValue != null) {
            Field field = parentFieldValue.getClass().getDeclaredField(fieldName.substring(fieldName.indexOf(".") + 1));
            if (field != null) {
              field.setAccessible(true);
              Object value = field.get(parentFieldValue);
              return value;
            }
          }
        }
      } else {
        // simple names
        Field field = entityClass.getDeclaredField(fieldName);
        if (field != null) {
          field.setAccessible(true);
          Object value = field.get(entity);
          return value;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private Kna1 findKna1Parent(EntityManager entityManager, Kna1PK pk) {
    return entityManager.find(Kna1.class, pk);
  }

  /**
   * Gets the current Object on the database
   * 
   * @param entityClass
   * @param entityManager
   * @param entity
   * @param genChangeLogDetails
   * @return
   * @throws Exception
   */
  private Object getCurrentObject(Class<?> entityClass, EntityManager entityManager, Object entity, GenChangeLogDetails genChangeLogDetails)
      throws Exception {

    String pk = genChangeLogDetails.pk();
    Field pkField = entityClass.getDeclaredField(pk);

    Table table = entityClass.getAnnotation(Table.class);
    if (table == null || table.schema() == null) {
      throw new Exception("Entities for Gen change log must have the Table annotation with schema value.");
    }

    String pkStringValue = constructPk(entity, genChangeLogDetails);
    LOG.debug(table.name() + " PK Value: " + pkStringValue);
    if (!StringUtils.isEmpty(pkStringValue)) {
      Object parentObj = storedChildren.get() != null ? storedChildren.get().get(table.name() + "|" + pkStringValue) : null;
      if (parentObj != null) {
        LOG.debug("Parent for " + table.name() + "|" + pkStringValue + " found.");
        return parentObj;
      }
    }
    LOG.warn("Retrieving parent for " + table.name() + "-" + pkStringValue + " via query");

    if (pkField != null) {
      pkField.setAccessible(true);
      Object pkValue = pkField.get(entity);

      String primarySql = "select * from " + table.schema().toUpperCase() + "." + table.name() + " where 1=1 ";
      PreparedQuery query = new PreparedQuery(entityManager, primarySql);
      // query.setForReadOnly(true);
      String name = null;
      for (final Field field : pkValue.getClass().getDeclaredFields()) {
        try {
          if (pkValue.getClass().getMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1),
              (Class[]) null) != null) {
            if (field.getAnnotation(Column.class) != null) {
              name = field.getAnnotation(Column.class).name();
            } else {
              name = field.getName().toUpperCase();
            }
            field.setAccessible(true);
            // build the existence query
            query.append("and " + name.toUpperCase() + " = :" + name.toUpperCase());
            query.setParameter(name.toUpperCase(), field.get(pkValue));
          }
        } catch (NoSuchMethodException e) {

        }
      }
      // get the result
      return query.getSingleResult(entityClass);
    }
    return null;
  }

  /**
   * Create one Gen change log record for each changed field for updates
   * 
   * @param genChangeLogDetails
   * @param entityClass
   * @param entityManager
   * @param entity
   * @param currentEntity
   * @throws Exception
   */
  private void createGenChangeLogForFieldUpdates(GenChangeLogDetails genChangeLogDetails, Class<?> entityClass, EntityManager entityManager,
      Object entity, Object currentEntity) throws Exception {

    Object currentValue = null;
    Object newValue = null;
    String name = null;
    for (final Field field : entityClass.getDeclaredFields()) {
      if (field.getAnnotation(Column.class) != null) {
        name = field.getAnnotation(Column.class).name();
      } else {
        name = field.getName().toUpperCase();
      }

      if (!isLoggable(field, name)) {
        continue;
      }

      field.setAccessible(true);
      currentValue = field.get(currentEntity);
      newValue = field.get(entity);
      if (currentValue == null && newValue == null) {
        // no change
        continue;
      }

      if (currentValue != null && (currentValue instanceof Date)) {
        Timestamp currentValueTs = new Timestamp(((Date) currentValue).getTime());
        String strCurrentValueDate = currentValueTs.toString() + "000";
        currentValue = strCurrentValueDate;
      }

      if (newValue != null && (newValue instanceof Date)) {
        Timestamp newValueTs = new Timestamp(((Date) newValue).getTime());
        String strNewValueDate = newValueTs.toString() + "000";
        newValue = strNewValueDate;
      }

      if (currentValue == null && newValue != null) {
        createGenChangeLog(entityClass, entityManager, ACTION_UPDATE, name.toUpperCase(), null, newValue.toString(), entity, genChangeLogDetails);
      } else if (currentValue != null && newValue == null) {
        createGenChangeLog(entityClass, entityManager, ACTION_UPDATE, name.toUpperCase(), currentValue.toString(), null, entity, genChangeLogDetails);
      } else if (!currentValue.equals(newValue)) {
        if (currentValue instanceof String) {
          String sVal1 = currentValue.toString().trim();
          String sVal2 = newValue.toString().trim();
          if (!sVal1.equals(sVal2)) {
            createGenChangeLog(entityClass, entityManager, ACTION_UPDATE, name.toUpperCase(), currentValue.toString(), newValue.toString(), entity,
                genChangeLogDetails);
          }
        } else {
          createGenChangeLog(entityClass, entityManager, ACTION_UPDATE, name.toUpperCase(), currentValue.toString(), newValue.toString(), entity,
              genChangeLogDetails);
        }
      }
    }

  }

  /**
   * Determines if the entity has been processed, to avoid cyclic calls
   * 
   * @param entity
   * @param entityClass
   * @return
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  private boolean isProcessed(Object entity, Class<?> entityClass) throws IllegalArgumentException, IllegalAccessException {

    /*
     * for (final Field field : entityClass.getDeclaredFields()) { if
     * (field.getAnnotation(ProcessedIndicator.class) != null) {
     * field.setAccessible(true); Boolean processed = (Boolean)
     * field.get(entity); return processed != null ? processed : false; } }
     */

    // if the field doesn't have a processedindicator, it's a danger, so always
    // indicate it's processed
    return true;
  }

  /**
   * Sets the {@link ProcessedIndicator} field of the class to true
   * 
   * @param entity
   * @param entityClass
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   */
  private void setProcessed(Object entity, Class<?> entityClass) throws IllegalArgumentException, IllegalAccessException {
    /*
     * for (final Field field : entityClass.getDeclaredFields()) { if
     * (field.getAnnotation(ProcessedIndicator.class) != null) {
     * field.setAccessible(true); field.set(entity, true); } }
     */
  }

  public static void start() throws Exception {
    LOG.debug("Starting genChangelog session..");
    EntityManager entityManager = JpaManager.getEntityManager();
    setManager(entityManager);
    EntityTransaction txn = entityManager.getTransaction();
    txn.begin();

    // Map<String, Object> children = storedChildren.get();
    // if (children != null) {
    // children = null;
    // }
    // storedChildren.set(new HashMap<String, Object>());

  }

  public static void end() {
    try {
      LOG.debug("Ending genChangelog session..");
      EntityManager entityManager = currentEntityManager.get();
      if (entityManager != null) {
        entityManager.flush();

        EntityTransaction txn = entityManager.getTransaction();
        if (txn != null && txn.isActive() && !txn.getRollbackOnly()) {
          LOG.debug("Committing genChangelog records..");
          txn.commit();
        }
        if (txn != null && txn.isActive() && txn.getRollbackOnly()) {
          txn.rollback();
        }

        entityManager.clear();
        entityManager.close();
      }
    } catch (Exception e) {
      LOG.error("Error in committing Gen change logs.", e);
    }
    clearManager();

    Map<String, Object> children = storedChildren.get();
    if (children != null) {
      children.clear();
      children = null;
    }
    storedChildren.remove();
    // storedChildren.set(null);
    clearParent();

  }

  public static void backout() {
    try {
      LOG.debug("Backing out of genChangelog changes..");
      EntityManager entityManager = currentEntityManager.get();
      if (entityManager != null) {

        EntityTransaction txn = entityManager.getTransaction();
        if (txn != null && txn.isActive()) {
          LOG.debug("Rolling back changes..");
          txn.rollback();
        }
        entityManager.clear();
        entityManager.close();
      }
    } catch (Exception e) {
      LOG.error("Error in backing out Gen change logs.", e);
    }
    clearManager();

    Map<String, Object> children = storedChildren.get();
    if (children != null) {
      children.clear();
      children = null;
    }
    storedChildren.remove();
    // storedChildren.set(null);
    clearParent();
  }

  public static void setManager(EntityManager entityManager) {
    LOG.debug("Setting thread local entity manager..");
    currentEntityManager.set(entityManager);
  }

  private static void clearManager() {
    LOG.debug("Clearing thread local entity manager..");
    currentEntityManager.remove();
    // currentEntityManager.set(null);
  }

  /**
   * Constructs the PK
   * 
   * @param entity
   * @param genChangeLogDetails
   * @return
   * @throws Exception
   */
  private String constructPk(Object entity, GenChangeLogDetails genChangeLogDetails) throws Exception {
    StringBuilder sb = new StringBuilder();

    Class<?> entityClass = entity.getClass();
    String pk = genChangeLogDetails.pk();
    Field pkField = entityClass.getDeclaredField(pk);

    if (pkField != null) {
      pkField.setAccessible(true);
      Object pkValue = pkField.get(entity);

      for (final Field field : pkValue.getClass().getDeclaredFields()) {
        try {
          if (pkValue.getClass().getMethod("get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1),
              (Class[]) null) != null) {
            field.setAccessible(true);
            sb.append(sb.length() > 0 ? "|" : "");
            sb.append(field.get(pkValue));
          }
        } catch (NoSuchMethodException e) {

        }
      }
    }

    return sb.toString();
  }

  /**
   * Determines if a field on the entity is a loggable field
   * 
   * @param field
   * @param name
   * @return
   */
  private boolean isLoggable(Field field, String name) {
    // id, transient and no log fields skipped
    if (field.getAnnotation(Transient.class) != null || field.getAnnotation(NoLog.class) != null || field.getAnnotation(EmbeddedId.class) != null) {
      return false;
    }
    // final fields excluded
    if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
      return false;
    }
    // exclude JPA internal fields
    if (name != null && ("PCSTATEMANAGER".equals(name) || "PCDETACHEDSTATE".equals(name) || name.toUpperCase().contains("PERSISTENCE"))) {
      return false;
    }
    // exclude Date and Timestamp
    if (Date.class.equals(field.getType()) || Timestamp.class.equals(field.getType())) {
      return false;
    }
    return true;

  }

  private Object cloneEntity(Object entity) throws Exception {
    if (entity == null) {
      return null;
    }
    Class<?> entityClass = entity.getClass();
    Object clone = entityClass.newInstance();
    try {
      PropertyUtils.copyProperties(clone, entity);
    } catch (Exception e) {
      LOG.warn("Error in cloning entity.", e);
      // noop
    }
    return clone;
  }
}
