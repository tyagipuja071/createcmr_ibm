/**
 * 
 */
package com.ibm.cio.cmr.request.entity.listeners;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.util.Arrays;
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.RequestChangeLog;
import com.ibm.cio.cmr.request.entity.RequestChangeLogPK;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * Entity listener for creating REQUEST_CHANGE_LOG records
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ChangeLogListener {

  private static final Logger LOG = Logger.getLogger(ChangeLogListener.class);

  private static final String ACTION_INSERT = "I";
  private static final String ACTION_UPDATE = "U";
  private static final String ACTION_DELETE = "D";

  private static final ThreadLocal<Map<Object, Map<String, Object>>> loadedValues = new ThreadLocal<>();
  private static final ThreadLocal<String> currentUser = new ThreadLocal<>();
  private static final ThreadLocal<Map<Long, Admin>> currentParent = new ThreadLocal<>();
  private static final ThreadLocal<EntityManager> currentEntityManager = new ThreadLocal<>();

  /**
   * Called when values of an entity is retrieved. The values are placed on a
   * map and stored on a threadlocal variable
   * 
   * @param entity
   */
  @PostLoad
  public void loadValues(Object entity) {
    Class<?> entityClass = entity.getClass();
    if (entityClass.getAnnotation(ChangeLogDetails.class) == null) {
      // do not store if not for logging
      return;
    }
    String name = null;
    Object currentValue = null;
    Map<String, Object> valueMap = new HashMap<>();
    for (final Field field : entityClass.getDeclaredFields()) {
      try {
        if (field.getAnnotation(Column.class) != null) {
          name = field.getAnnotation(Column.class).name();
        } else {
          name = field.getName().toUpperCase();
        }
        if (!isLoggable(field, name)) {
          continue;
        }
        field.setAccessible(true);
        currentValue = field.get(entity);
        if (currentValue != null && (currentValue instanceof String)) {
          String val = (String) currentValue;
          if (StringUtils.isBlank(val)) {
            valueMap.put(name, null);
          } else {
            valueMap.put(name, val.trim());
          }
        } else {
          valueMap.put(name, currentValue);
        }
        if (valueMap.get(name) == null) {
          NullValue nullValue = field.getAnnotation(NullValue.class);
          if (nullValue != null) {
            valueMap.put(name, nullValue.value());
          }
        }
      } catch (Exception e) {
        LOG.warn("Current value for " + name + " cannot be determined.");
        // noop, let it be
      }
    }

    Map<Object, Map<String, Object>> map = loadedValues.get();
    if (map == null) {
      loadedValues.set(new HashMap<Object, Map<String, Object>>());
    }
    map = loadedValues.get();
    map.put(entity, valueMap);
    loadedValues.set(map);

    // start tracking parent here also
    if (entity != null && (entity instanceof Admin)) {
      Admin admin = (Admin) entity;
      Map<Long, Admin> parentMap = currentParent.get();
      if (parentMap == null) {
        currentParent.set(new HashMap<Long, Admin>());
      }
      parentMap = currentParent.get();
      parentMap.put(admin.getId().getReqId(), admin);
    }
  }

  /**
   * Processes change logs
   * 
   * @param entity
   * @param logType
   */
  private void processChangeLog(Object entity, String logType) {
    Class<?> entityClass = entity.getClass();
    if (entityClass.getAnnotation(ChangeLogDetails.class) == null) {
      // do not store if not for logging
      return;
    }
    if (entity != null && Admin.class.equals(entity.getClass())) {
      // an admin record was found, try to map it
      Map<Long, Admin> parentMap = currentParent.get();
      if (parentMap == null) {
        currentParent.set(new HashMap<Long, Admin>());
      }
      Admin temp = (Admin) entity;
      parentMap = currentParent.get();
      Admin parent = parentMap.get(temp.getId().getReqId());
      if (parent == null) {
        parentMap.put(temp.getId().getReqId(), temp);
        currentParent.set(parentMap);
      }
    }
    LOG.debug("Processing Change Log for " + getTable(entity.getClass()));
    ChangeLogDetails logDetails = entityClass.getAnnotation(ChangeLogDetails.class);
    switch (logType) {
    case (ACTION_INSERT):
      if (!logDetails.logInserts()) {
        return;
      }
      break;
    case (ACTION_UPDATE):
      if (!logDetails.logUpdates()) {
        return;
      }
      break;
    case (ACTION_DELETE):
      if (!logDetails.logDeletes()) {
        return;
      }
      break;
    }

    boolean ownTransaction = true;
    try {
      EntityManager entityManager = currentEntityManager.get();
      if (entityManager != null) {
        ownTransaction = false;
        LOG.debug("Using thread local entity manager..");
      } else {
        entityManager = JpaManager.getEntityManager();
      }
      try {
        EntityTransaction txn = entityManager.getTransaction();
        try {
          if (ownTransaction) {
            txn.begin();
          }

          Admin parent = null;
          if (logDetails.childTable()) {
            parent = getParentRecord(entityManager, entity, logDetails);
          } else if ("ADMIN".equals(getTable(entityClass))) {
            parent = (Admin) entity;
            ChangeLogDetails adminLogDetails = Admin.class.getAnnotation(ChangeLogDetails.class);
            String userId = (String) getFieldValue(Admin.class, adminLogDetails.userId(), parent);
            if (currentUser.get() == null && !StringUtils.isBlank(userId)) {
              currentUser.set(userId);
            }
          } else {
            LOG.warn("Parent Admin record cannot be determined. ");
            if (ownTransaction) {
              txn.rollback();
            }
            return;
          }

          String[] logForRequestType = logDetails.logForRequestType();
          if (logForRequestType != null && logForRequestType.length > 0 && !Arrays.asList(logForRequestType).contains(parent.getReqType())) {
            LOG.debug("Request type " + parent.getReqType() + " not logged for " + getTable(entity.getClass()));
            if (ownTransaction) {
              txn.rollback();
            }
            return;
          }
          switch (logType) {
          case (ACTION_INSERT):
            // I type log
            Timestamp ts = SystemUtil.getActualTimestamp();
            if (StringUtils.isBlank(logDetails.insertFieldName())) {
              createChangeLog(parent, logDetails, ts, entityClass, entityManager, ACTION_INSERT, "", null, null, entity, ownTransaction);
            } else {
              String[] columnNames = logDetails.insertFieldName().split(",");
              for (String columnName : columnNames) {
                String column = getColumnName(entityClass, entity, columnName);
                Object value = getFieldValue(entityClass, columnName, entity);
                createChangeLog(parent, logDetails, ts, entityClass, entityManager, ACTION_INSERT, column, null, value != null ? value.toString()
                    : null, entity, ownTransaction);
              }
            }
            loadValues(entity);
            break;
          case (ACTION_UPDATE):
            // U type log
            if (loadedValues.get() != null) {
              Map<String, Object> current = loadedValues.get().get(entity);
              if (current != null) {
                createChangeLogForFieldUpdates(parent, logDetails, entity.getClass(), entityManager, entity, current, ownTransaction);
              }
            }
            break;
          case (ACTION_DELETE):
            // D type log
            ts = SystemUtil.getActualTimestamp();
            if (StringUtils.isBlank(logDetails.deleteFieldName())) {
              createChangeLog(parent, logDetails, ts, entityClass, entityManager, ACTION_DELETE, "", null, null, entity, ownTransaction);
            } else {
              String[] columnNames = logDetails.deleteFieldName().split(",");
              for (String columnName : columnNames) {
                String column = getColumnName(entityClass, entity, columnName);
                Object value = getFieldValue(entityClass, columnName, entity);
                createChangeLog(parent, logDetails, ts, entityClass, entityManager, ACTION_DELETE, column, value != null ? value.toString() : null,
                    null, entity, ownTransaction);
              }
            }
            break;
          }

          if (ownTransaction) {
            txn.commit();
            LOG.trace("REQUEST_CHANGE_LOG record committed.");
          }

        } catch (Exception e) {
          LOG.error("Cannot create REQUEST_CHANGE_LOG record.", e);
          if (ownTransaction) {
            txn.rollback();
          }
        }
      } finally {
        if (ownTransaction) {
          entityManager.close();
        }
      }
    } catch (Exception e) {
      LOG.error("Error in creating REQUEST_CHANGE_LOG record.", e);
    }
  }

  /**
   * Creates REQUEST_CHANGE_LOG records after inserting the entity
   * 
   * @param entity
   * @throws Exception
   */
  @PostPersist
  public void createChangeLogForInsert(Object entity) throws Exception {
    processChangeLog(entity, ACTION_INSERT);
  }

  /**
   * Creates REQUEST_CHANGE_LOG records before updating the entity
   * 
   * @param entity
   * @throws Exception
   */
  @PreUpdate
  public void createChangeLogForUpdate(Object entity) throws Exception {
    processChangeLog(entity, ACTION_UPDATE);
  }

  /**
   * Create one change log record for each changed field for updates
   * 
   * @param changeLogDetails
   * @param entityClass
   * @param entityManager
   * @param entity
   * @param currentEntity
   * @throws Exception
   */
  private void createChangeLogForFieldUpdates(Admin parent, ChangeLogDetails logDetails, Class<?> entityClass, EntityManager entityManager,
      Object entity, Map<String, Object> current, boolean ownTransaction) throws Exception {

    Object currentValue = null;
    Object newValue = null;
    String name = null;
    Timestamp ts = SystemUtil.getActualTimestamp();
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
      currentValue = current.get(name);
      newValue = field.get(entity);
      if (currentValue == null && newValue == null) {
        // no change
        continue;
      }
      if (currentValue == null && newValue != null) {
        if ((newValue instanceof String) && !StringUtils.isBlank(newValue.toString()) || !(newValue instanceof String)) {
          createChangeLog(parent, logDetails, ts, entityClass, entityManager, ACTION_UPDATE, name.toUpperCase(), null, newValue.toString(), entity,
              ownTransaction);
        }
      } else if (currentValue != null && newValue == null) {
        if ((currentValue instanceof String) && !StringUtils.isBlank(currentValue.toString()) || !(currentValue instanceof String)) {
          createChangeLog(parent, logDetails, ts, entityClass, entityManager, ACTION_UPDATE, name.toUpperCase(), currentValue.toString(), null,
              entity, ownTransaction);
        }
      } else if (!currentValue.equals(newValue)) {
        if (currentValue instanceof String) {
          String sVal1 = currentValue.toString().trim();
          String sVal2 = newValue.toString().trim();
          if (!sVal1.equals(sVal2)) {
            createChangeLog(parent, logDetails, ts, entityClass, entityManager, ACTION_UPDATE, name.toUpperCase(), currentValue.toString(),
                newValue.toString(), entity, ownTransaction);
          }
        } else {
          createChangeLog(parent, logDetails, ts, entityClass, entityManager, ACTION_UPDATE, name.toUpperCase(), currentValue.toString(),
              newValue.toString(), entity, ownTransaction);
        }
      }
      current.put(name, newValue);
    }

  }

  /**
   * Creates REQUEST_CHANGE_LOG records after inserting the entity
   * 
   * @param entity
   * @throws Exception
   */
  @PreRemove
  public void createChangeLogForRemove(Object entity) throws Exception {
    processChangeLog(entity, ACTION_DELETE);
  }

  /**
   * Creates a {@link RequestChangeLog} record
   * 
   * @param parent
   * @param logDetails
   * @param ts
   * @param entityClass
   * @param entityManager
   * @param action
   * @param field
   * @param oldValue
   * @param newValue
   * @param entity
   * @throws Exception
   */
  private void createChangeLog(Admin parent, ChangeLogDetails logDetails, Timestamp ts, Class<?> entityClass, EntityManager entityManager,
      String action, String field, String oldValue, String newValue, Object entity, boolean ownTransaction) throws Exception {
    RequestChangeLogPK pk = new RequestChangeLogPK();
    if (logDetails.addressTable()) {
      String addrType = (String) getFieldValue(entityClass, logDetails.addressType(), entity);
      pk.setAddrTyp(addrType);
    } else {
      pk.setAddrTyp("");
    }
    pk.setChangeTs(ts);
    pk.setFieldName(field);
    if (pk.getFieldName().length() > 30) {
      pk.setFieldName(pk.getFieldName().substring(0, 30));
    }
    pk.setRequestId(parent.getId().getReqId());
    pk.setTablName(entity.getClass().getAnnotation(Table.class).name());
    RequestChangeLog log = new RequestChangeLog();
    log.setId(pk);
    log.setAction(action);
    if (logDetails.addressTable()) {
      String addrSeq = (String) getFieldValue(entityClass, logDetails.addressSeq(), entity);
      if (addrSeq.length() > 5) {
        log.setAddrSequence(addrSeq.substring(addrSeq.length() - 5));
      } else {
        log.setAddrSequence(addrSeq);
      }
    } else {
      log.setAddrSequence(null);
    }
    log.setNewValue(newValue);
    log.setOldValue(oldValue);
    log.setRequestStatus(parent.getReqStatus());
    ChangeLogDetails adminLogDetails = Admin.class.getAnnotation(ChangeLogDetails.class);
    String userId = (String) getFieldValue(Admin.class, adminLogDetails.userId(), parent);
    if (!ChangeLogDetails.ANONYMOUS.equals(logDetails.userId())) {
      userId = (String) getFieldValue(entityClass, logDetails.userId(), entity);
    }
    if (logDetails.childTable() && !StringUtils.isBlank(currentUser.get())) {
      LOG.trace("Setting child table User ID to " + currentUser.get());
      userId = currentUser.get();
    }
    log.setUserId(userId);
    entityManager.persist(log);
    LOG.debug("Creating Request Change Log for Request " + pk.getRequestId() + " = " + action + " (" + field + " / " + oldValue + " / " + newValue
        + " / " + userId);

    if (ownTransaction) {
      entityManager.flush();
    }
  }

  /**
   * Gets the table name to use. It will check the {@link Table} annotation if
   * it exists, and will use the class's name if not found
   * 
   * @param entityClass
   * @return
   */
  private String getTable(Class<?> entityClass) {
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
  private Object getFieldValue(Class<?> entityClass, String fieldName, Object entity) {
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
      LOG.error("Error when getting field name " + fieldName + " from " + entity.getClass().getName(), e);
    }
    return null;
  }

  /**
   * Gets the column name from the field
   * 
   * @param fieldName
   * @param entity
   * @return
   */
  private String getColumnName(Class<?> entityClass, Object entity, String fieldName) {
    try {

      String name = null;
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
              if (field.getAnnotation(Column.class) != null) {
                name = field.getAnnotation(Column.class).name();
              } else {
                name = field.getName().toUpperCase();
              }
              return name;
            }
          }
        }
      } else {
        // simple names
        Field field = entityClass.getDeclaredField(fieldName);
        if (field != null) {
          field.setAccessible(true);
          if (field.getAnnotation(Column.class) != null) {
            name = field.getAnnotation(Column.class).name();
          } else {
            name = field.getName().toUpperCase();
          }
          return name;
        }
      }
    } catch (Exception e) {
      LOG.error("Error when getting column name for " + fieldName + " from " + entity.getClass().getName(), e);
    }
    return "";
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
    if (name != null && ("PCSTATEMANAGER".equals(name) || "PCDETACHEDSTATE".equals(name))) {
      return false;
    }
    return true;
  }

  /**
   * Gets the parent {@link Admin} record for the entity being logged
   * 
   * @param entityManager
   * @param entity
   * @param logDetails
   * @return
   */
  private Admin getParentRecord(EntityManager entityManager, Object entity, ChangeLogDetails logDetails) {
    long reqId = (long) getFieldValue(entity.getClass(), logDetails.reqId(), entity);

    Map<Long, Admin> parentMap = currentParent.get();
    if (parentMap == null) {
      currentParent.set(new HashMap<Long, Admin>());
    }
    parentMap = currentParent.get();
    Admin parent = parentMap.get(reqId);
    if (parent == null) {

      // String sql = ExternalizedQuery.getSql("CHANGELOG.GET_ADMIN_DETAILS");
      // PreparedQuery query = new PreparedQuery(entityManager, sql);
      // query.setParameter("REQ_ID", reqId);
      // query.setForReadOnly(true);
      // query.setFlushOnCommit(true);
      // parent = query.getSingleResult(Admin.class);

      AdminPK pk = new AdminPK();
      pk.setReqId(reqId);
      parent = entityManager.find(Admin.class, pk);
      parentMap.put(reqId, parent);
      currentParent.set(parentMap);
      return parent;
    } else {
      LOG.trace("Parent for " + reqId + " from stored values");
      return parent;
    }
  }

  public static void clean() {
    Map<Object, Map<String, Object>> currentMap = loadedValues.get();
    if (currentMap != null) {
      currentMap.clear();
      currentMap = null;
    }

    Map<Long, Admin> parentMap = currentParent.get();
    if (parentMap != null) {
      parentMap.clear();
      parentMap = null;
    }
    // currentParent.set(null);
    // currentUser.set(null);
    // loadedValues.set(null);
    currentParent.remove();
    currentUser.remove();
    loadedValues.remove();
  }

  public static void setManager(EntityManager entityManager) {
    LOG.debug("Setting thread local entity manager..");
    currentEntityManager.set(entityManager);
  }

  public static void setUser(String userId) {
    LOG.debug("Setting thread local user..");
    currentUser.set(userId);
  }

  public static void clearManager() {
    LOG.debug("Clearing thread local entity manager..");
    // currentEntityManager.set(null);
    currentEntityManager.remove();
  }

}
