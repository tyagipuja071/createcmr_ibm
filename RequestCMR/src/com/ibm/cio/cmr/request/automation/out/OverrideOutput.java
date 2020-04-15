/**
 * 
 */
package com.ibm.cio.cmr.request.automation.out;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AutomationData;
import com.ibm.cio.cmr.request.entity.AutomationDataPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * Contains the data override values that an {@link AutomationElement} produced
 * 
 * @author JeffZAMORA
 * 
 */
public class OverrideOutput implements AutomationOutput {

  private static final Logger LOG = Logger.getLogger(OverrideOutput.class);

  private boolean imported;
  private Map<FieldResultKey, FieldResult> data = new HashMap<>();
  private List<AutomationData> recordedData = new ArrayList<AutomationData>();

  /**
   * Constructs a new instance of a {@link OverrideOutput} object
   * 
   * @param imported
   */
  public OverrideOutput(boolean imported) {
    this.imported = imported;
  }

  /**
   * Adds an override mapping to the specific field
   * 
   * @param field
   * @param oldValue
   * @param newValue
   */
  public void addOverride(String processCode, String addrType, String field, String oldValue, String newValue) {
    if (!"BR_CALCULATE".equalsIgnoreCase(processCode) && StringUtils.isBlank(oldValue) && StringUtils.isBlank(newValue)) {
      return;
    }
    this.data.put(new FieldResultKey(addrType, field), new FieldResult(processCode, addrType, field, oldValue, newValue));
  }

  @Override
  public void apply(EntityManager entityManager, AppUser user, RequestData requestData, long resultId, long itemNo, String processCd,
      boolean activeEngine) throws Exception {

    long reqId = requestData.getAdmin().getId().getReqId();

    if (!activeEngine) {
      // the data here is not on the container, query
      LOG.debug("Querying override data records from DB..");
      // the data here is not on the container, query
      String sql = ExternalizedQuery.getSql("AUTOMATION.GET_OVERRIDE_DATA");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("RESULT_ID", resultId);
      query.setParameter("PROCESS_CD", processCd);
      query.setForReadOnly(true);

      List<AutomationData> overrides = query.getResults(AutomationData.class);
      if (overrides != null) {
        for (AutomationData override : overrides) {

          addOverride(override.getId().getProcessCd(), override.getId().getAddrTyp(), override.getId().getFieldName(), override.getOldValue(),
              override.getNewValue());

          override.setImportedIndc("N");
          override.setLastUpdtBy(user.getIntranetId());
          override.setLastUpdtTs(SystemUtil.getActualTimestamp());
          entityManager.merge(override);
        }
      }
    }

    FieldResult fieldResult = null;
    Data dataObj = requestData.getData();
    Admin adminObj = requestData.getAdmin();
    AutomationData dataRecord = null;
    for (FieldResultKey fieldKey : this.data.keySet()) {
      fieldResult = this.data.get(fieldKey);
      if (fieldResult != null) {
        LOG.trace("Importing override data record for Request " + reqId);

        dataRecord = findOverrideRecord(entityManager, fieldResult, resultId);
        if ("DATA".equals(fieldResult.getAddrType())) {
          setEntityValue(dataObj, fieldResult.getFieldName(), fieldResult.getNewValue());
        } else if ("ADMN".equals(fieldResult.getAddrType())) {
          setEntityValue(adminObj, fieldResult.getFieldName(), fieldResult.getNewValue());
        } else {
          String addrType = fieldResult.getAddrType();
          Addr addr = requestData.getAddress(addrType);
          if (addr != null) {
            setEntityValue(addr, fieldResult.getFieldName(), fieldResult.getNewValue());
          }
        }
        if (dataRecord != null) {
          dataRecord.setImportedIndc("Y");
          dataRecord.setLastUpdtBy(user.getIntranetId());
          dataRecord.setLastUpdtTs(SystemUtil.getActualTimestamp());
          entityManager.merge(dataRecord);
        }
      }
    }

    LOG.debug("Data imported from overrides. Saving request records..");

    Admin admin = requestData.getAdmin();

    admin.setLastUpdtBy(user.getIntranetId());
    admin.setLastUpdtTs(SystemUtil.getActualTimestamp());
    entityManager.merge(admin);

    Data data = requestData.getData();
    entityManager.merge(data);

  }

  @Override
  public void recordData(EntityManager entityManager, long resultId, AppUser user, RequestData requestData) throws Exception {
    if (this.data != null) {
      for (FieldResultKey field : this.data.keySet()) {
        FieldResult result = this.data.get(field);

        AutomationData data = new AutomationData();
        AutomationDataPK pk = new AutomationDataPK();
        pk.setAddrTyp(result.getAddrType());
        pk.setAutomationResultId(resultId);
        pk.setFieldName(result.getFieldName());
        pk.setProcessCd(result.getProcessCode());
        data.setId(pk);

        Timestamp ts = SystemUtil.getActualTimestamp();
        data.setCreateBy(user.getIntranetId());
        data.setCreateTs(ts);
        data.setImportedIndc("N");
        data.setLastUpdtBy(user.getIntranetId());
        data.setLastUpdtTs(ts);
        data.setNewValue(result.getNewValue());
        data.setOldValue(result.getOldValue());

        LOG.trace("Creating data record for " + result.getFieldName() + " = " + result.getOldValue() + " - " + result.getNewValue());
        entityManager.persist(data);

        this.recordedData.add(data);
      }
    }

  }

  /**
   * Clear all recorded overrides
   */
  public void clearOverrides() {
    this.data = new HashMap<FieldResultKey, FieldResult>();
  }

  /**
   * Removes the override record on the basis of fieldResultKey
   * 
   * @param fieldResultKey
   */
  public void clearOverride(FieldResultKey fieldResultKey) {
    if (this.data != null && this.data.containsKey(fieldResultKey)) {
      this.data.remove(fieldResultKey);
    }
  }

  /**
   * Sets the data value by finding the relevant column having the given field
   * name
   * 
   * @param entity
   * @param fieldName
   * @param value
   */
  protected void setEntityValue(Object entity, String fieldName, Object value) {
    boolean fieldMatched = false;
    for (Field field : entity.getClass().getDeclaredFields()) {
      // match the entity name to field name
      fieldMatched = false;
      Column col = field.getAnnotation(Column.class);
      if (col != null && fieldName.toUpperCase().equals(col.name().toUpperCase())) {
        fieldMatched = true;
      } else if (field.getName().toUpperCase().equals(fieldName.toUpperCase())) {
        fieldMatched = true;
      }
      if (fieldMatched) {
        try {
          field.setAccessible(true);
          try {
            Method set = entity.getClass().getDeclaredMethod("set" + (field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1)),
                value != null ? value.getClass() : String.class);
            if (set != null) {
              set.invoke(entity, value);
            }
          } catch (Exception e) {
            field.set(entity, value);
          }
        } catch (Exception e) {
          LOG.trace("Field " + fieldName + " cannot be assigned. Error: " + e.getMessage());
        }
      }
    }
  }

  /**
   * Finds the relevant AUTOMATION_DATA record
   * 
   * @param entityManager
   * @param fieldResult
   * @param resultId
   * @return
   */
  private AutomationData findOverrideRecord(EntityManager entityManager, FieldResult fieldResult, long resultId) {
    AutomationDataPK pk = new AutomationDataPK();
    pk.setAutomationResultId(resultId);
    pk.setFieldName(fieldResult.getFieldName());
    pk.setProcessCd(fieldResult.getProcessCode());
    pk.setAddrTyp(fieldResult.getAddrType());

    // try first to match with current recorded data to avoid 2x queries
    AutomationData data = findFromRecordedData(fieldResult, resultId);
    if (data == null) {
      data = entityManager.find(AutomationData.class, pk);
    }
    if (data == null) {
      data = new AutomationData();
      data.setId(pk);
      data.setOldValue(fieldResult.getOldValue());
      data.setNewValue(fieldResult.getNewValue());
    }
    return data;
  }

  /**
   * For an active engine, get the matched {@link AutomationData} record from
   * recorded data
   * 
   * @param result
   * @param resultId
   * @return
   */
  private AutomationData findFromRecordedData(FieldResult result, long resultId) {
    for (AutomationData data : this.recordedData) {
      if (data.getId().getAutomationResultId() == resultId && result.getFieldName().equals(data.getId().getFieldName())
          && result.getProcessCode().equals(data.getId().getProcessCd())) {
        LOG.trace("Override data found in recorded data..");
        return data;
      }
    }
    return null;
  }

  public Map<FieldResultKey, FieldResult> getData() {
    return data;
  }

  public boolean isImported() {
    return imported;
  }

  public void setImported(boolean imported) {
    this.imported = imported;
  }

}
