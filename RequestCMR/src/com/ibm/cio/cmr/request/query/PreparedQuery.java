/**
 * 
 */
package com.ibm.cio.cmr.request.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.ColumnResult;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.Query;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.persistence.config.QueryHints;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.BaseEntity;
import com.ibm.cio.cmr.request.entity.CompoundEntity;

/**
 * Query class to use for JPA prepared query to ease the hanlding of parameters
 * 
 * @author Jeffrey Zamora
 * 
 */
public class PreparedQuery {

  private static final Logger LOG = Logger.getLogger(PreparedQuery.class);

  private EntityManager entityManager;
  private StringBuilder rawSql;
  private Map<String, Object> valueMap;
  private Map<Integer, String> indexMap;
  private boolean forReadOnly;
  private boolean flushOnCommit;

  /**
   * Creates a new instance
   * 
   * @param entityManager
   * @param sql
   */
  public PreparedQuery(EntityManager entityManager, String sql) {
    this.entityManager = entityManager;
    this.rawSql = new StringBuilder(sql);
    this.valueMap = new HashMap<String, Object>();
    this.indexMap = new HashMap<Integer, String>();
  }

  /**
   * Sets the parameter value for the given param name. Param names are in the
   * sql as :PARAM_NAME
   * 
   * @param paramName
   * @param value
   */
  public void setParameter(String paramName, Object value) {
    this.valueMap.put(paramName, value);
  }

  /**
   * Appends to the current SQL
   * 
   * @param sqlFragment
   */
  public void append(String sqlFragment) {
    this.rawSql.append(" ").append(sqlFragment);
  }

  /**
   * Returns a compound result of the query. The annotatedSqlName should be a
   * named {@link SqlResultSetMapping} on the entity class
   * 
   * @param maxRows
   * @param annotatedSqlName
   * @return
   */
  @SuppressWarnings("unchecked")
  public List<CompoundEntity> getCompundResults(int maxRows, Class<? extends BaseEntity<?>> entityClass, String annotatedSqlName) {
    String preparedSql = prepareSql();
    LOG.debug("Prepared Query (Compound): " + preparedSql);
    if (LOG.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder();
      for (Integer index : this.indexMap.keySet()) {
        sb.append(sb.length() > 0 ? ", " : "");
        sb.append(index + "=" + this.valueMap.get(this.indexMap.get(index)));
      }
      LOG.debug("Params: [" + sb.toString() + "]");
    }

    Query query = this.entityManager.createNativeQuery(preparedSql, annotatedSqlName);
    query.setHint(QueryHints.MAINTAIN_CACHE, false);

    if (maxRows > 0) {
      query.setMaxResults(maxRows);
    }

    for (Integer index : this.indexMap.keySet()) {
      query.setParameter(index, this.valueMap.get(this.indexMap.get(index)));
    }

    CompoundEntity entity = null;
    List<CompoundEntity> entities = new ArrayList<CompoundEntity>();
    List<Object[]> results = query.getResultList();
    int index = 0;
    String fieldName = null;
    for (Object[] result : results) {
      entity = new CompoundEntity();
      index = 0;
      for (Object object : result) {
        if (object == null || object instanceof String || object instanceof Date || object instanceof Integer || object instanceof Long) {
          fieldName = getFieldNameAtIndex(entityClass, annotatedSqlName, index);
          if (fieldName != null) {
            entity.setValue(fieldName, object);
          }
          index++;
        } else {
          if (this.forReadOnly && object.getClass().getAnnotation(Entity.class) != null) {
            entityManager.detach(object);
          }
          entity.addEntity((BaseEntity<?>) object);
        }
      }
      entities.add(entity);
    }
    return entities;
  }

  /**
   * Returns a compound result of the query. The annotatedSqlName should be a
   * named {@link SqlResultSetMapping} on the entity class
   * 
   * @param maxRows
   * @param annotatedSqlName
   * @return
   */
  public List<CompoundEntity> getCompundResults(Class<? extends BaseEntity<?>> entityClass, String annotatedSqlName) {
    return getCompundResults(-1, entityClass, annotatedSqlName);

  }

  /**
   * Parses the annotation on the source class and gets the field defined at an
   * index
   * 
   * @param sourceClass
   * @param index
   * @return
   */
  private String getFieldNameAtIndex(Class<?> sourceClass, String mappingName, int index) {
    SqlResultSetMappings maps = sourceClass.getAnnotation(SqlResultSetMappings.class);
    if (maps != null) {
      SqlResultSetMapping[] mapList = maps.value();
      if (mapList != null) {
        for (SqlResultSetMapping map : mapList) {
          if (mappingName.equals(map.name())) {
            ColumnResult[] columns = map.columns();
            if (columns != null && columns.length > index) {
              ColumnResult result = columns[index];
              return result.name();
            }
          }
        }
      }
    } else {
      SqlResultSetMapping map = sourceClass.getAnnotation(SqlResultSetMapping.class);
      if (map != null) {
        ColumnResult[] columns = map.columns();
        if (columns != null && columns.length > index) {
          ColumnResult result = columns[index];
          return result.name();
        }
      }
    }
    return null;
  }

  /**
   * Executes the query and gets the results
   * 
   * @param maxRows
   * @param returnClass
   * @return
   */
  @SuppressWarnings("unchecked")
  public <M> List<M> getResults(int maxRows, Class<M> returnClass) {
    if (!this.valueMap.containsKey("MANDT")) {
      String mandt = SystemConfiguration.getValue("MANDT");
      setParameter("MANDT", mandt);
    }
    String preparedSql = prepareSql();
    LOG.debug("Prepared Query: " + preparedSql);
    if (LOG.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder();
      for (Integer index : this.indexMap.keySet()) {
        sb.append(sb.length() > 0 ? ", " : "");
        sb.append(index + "=" + this.valueMap.get(this.indexMap.get(index)));
      }
      LOG.debug("Params: [" + sb.toString() + "]");
    }
    Query query = null;
    if (returnClass != null && returnClass.getAnnotation(Entity.class) != null) {
      query = this.entityManager.createNativeQuery(preparedSql, returnClass);
    } else {
      query = this.entityManager.createNativeQuery(preparedSql);
    }
    if (maxRows > 0) {
      query.setMaxResults(maxRows);
    }
    for (Integer index : this.indexMap.keySet()) {
      query.setParameter(index, this.valueMap.get(this.indexMap.get(index)));
    }

    if (this.flushOnCommit) {
      query.setFlushMode(FlushModeType.COMMIT);
    }
    query.setHint(QueryHints.MAINTAIN_CACHE, false);

    if (returnClass != null && returnClass.getAnnotation(Entity.class) != null && this.forReadOnly) {
      List<M> result = query.getResultList();
      for (M entity : result) {
        this.entityManager.detach(entity);
      }
      return result;
    } else if (returnClass != null && returnClass.getAnnotation(Entity.class) == null) {
      return (List<M>) convertList((List<Object[]>) query.getResultList(), returnClass);
    } else {
      return query.getResultList();
    }
  }

  /**
   * Converts the raw result list to the target class
   * 
   * @param results
   * @param returnClass
   * @return
   */
  @SuppressWarnings("unchecked")
  private <T> List<T> convertList(List<Object[]> results, T returnClass) {
    List<T> list = new ArrayList<T>();
    // do a dirty one by one here

    Object peek = results != null && !results.isEmpty() ? results.get(0) : null;
    for (Object result : results) {
      if (result != null) {
        peek = result;
        break;
      }
    }
    if (peek == null) {
      return null;
    }
    if (peek instanceof Object[]) {
      if (Object[].class.equals(returnClass)) {
        return (List<T>) results;
      } else {
        for (Object[] result : results) {
          list.add((T) result[0]);
        }
      }
    } else {
      for (Object result : results) {
        list.add((T) result);
      }
    }
    return list;
  }

  /**
   * Executes the query and gets all the results
   * 
   * @param returnClass
   * @return
   */
  public <M> List<M> getResults(Class<M> returnClass) {
    return getResults(-1, returnClass);
  }

  /**
   * Executes the query and gets the results
   * 
   * @param maxRows
   * @return
   */
  public List<Object[]> getResults(int maxRows) {
    return getResults(maxRows, null);
  }

  /**
   * Executes the query and gets the results with no limit
   * 
   * @param maxRows
   * @return
   */
  public List<Object[]> getResults() {
    return getResults(-1, null);
  }

  /**
   * Executes the query and returns true if there are results
   * 
   * @return
   */
  public boolean exists() {
    List<Object[]> results = getResults(1);
    return results != null && results.size() > 0;
  }

  /**
   * Prepares the SQL
   * 
   * @return
   */
  private String prepareSql() {
    String sql = this.rawSql.toString();
    int index = 1;
    List<String> sortedKeys = new ArrayList<String>();
    sortedKeys.addAll(this.valueMap.keySet());
    ParamComparator comparator = new ParamComparator();
    Collections.sort(sortedKeys, comparator);
    for (String paramName : sortedKeys) {
      while (sql.contains(":" + paramName)) {
        sql = StringUtils.replaceOnce(sql, ":" + paramName, "?" + index);
        this.indexMap.put(index, paramName);
        index++;
      }
      while (sql.contains(":" + paramName.toUpperCase())) {
        sql = StringUtils.replaceOnce(sql, ":" + paramName.toUpperCase(), "?" + index);
        this.indexMap.put(index, paramName);
        index++;
      }
    }
    if (this.forReadOnly) {
      sql += " for read only with UR";
    }
    return sql;
  }

  /**
   * Executes the SQL
   * 
   * @return
   */
  public int executeSql() {
    String preparedSql = prepareSql();
    LOG.debug("Prepared Query: " + preparedSql);
    if (LOG.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder();
      for (Integer index : this.indexMap.keySet()) {
        sb.append(sb.length() > 0 ? ", " : "");
        sb.append(index + "=" + this.valueMap.get(this.indexMap.get(index)));
      }
      LOG.debug("Params: [" + sb.toString() + "]");
    }
    Query query = null;
    query = this.entityManager.createNativeQuery(preparedSql);
    for (Integer index : this.indexMap.keySet()) {
      query.setParameter(index, this.valueMap.get(this.indexMap.get(index)));
    }
    return query.executeUpdate();
  }

  public <M> M getSingleResult(Class<M> returnClass) {
    List<M> results = getResults(1, returnClass);
    if (results != null && results.size() > 0) {
      return results.get(0);
    }
    return null;
  }

  /**
   * Gets the result defaulting to the Object[] result set
   * 
   * @return
   */
  public Object[] getSingleResult() {
    List<Object[]> results = getResults(1);
    if (results != null && results.size() > 0) {
      return results.get(0);
    }
    return null;
  }

  public boolean isForReadOnly() {
    return forReadOnly;
  }

  public void setForReadOnly(boolean forReadOnly) {
    this.forReadOnly = forReadOnly;
  }

  private class ParamComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
      if (o1 == null && o2 != null) {
        return 1;
      }
      if (o2 == null && o1 != null) {
        return -1;
      }
      if (o1.length() > o2.length()) {
        return -1;
      }
      if (o1.length() < o2.length()) {
        return 1;
      }
      return 0;
    }

  }

  public boolean isFlushOnCommit() {
    return flushOnCommit;
  }

  public void setFlushOnCommit(boolean flushOnCommit) {
    this.flushOnCommit = flushOnCommit;
  }
}
