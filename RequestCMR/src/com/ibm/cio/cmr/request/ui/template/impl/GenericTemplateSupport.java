package com.ibm.cio.cmr.request.ui.template.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.entity.CustScenarios;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.template.Template;
import com.ibm.cio.cmr.request.ui.template.TemplateDriver;
import com.ibm.cio.cmr.request.ui.template.TemplateSupport;
import com.ibm.cio.cmr.request.ui.template.TemplatedField;

/**
 * 
 * Handler for the new implementation of the Scenarios framework. The scenarios
 * are now retrieved on a common table CREQCMR.CUST_SCENARIOS
 * 
 * @author Garima Narang - initial version
 * @author Jeffrey Zamora - updates
 * 
 */
public class GenericTemplateSupport extends TemplateSupport {

  private static final ResourceBundle PROPS = ResourceBundle.getBundle("template_gen");

  @Override
  public String getCmrIssuingCountry() {
    return "GEN";
  }

  @Override
  protected TemplateDriver getDriverField() {
    TemplateDriver driver = new TemplateDriver();
    driver.setFieldId(PROPS.getString("driver.fieldId"));
    driver.setFieldName(PROPS.getString("driver.fieldName"));
    return driver;
  }

  @Override
  protected Template doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String driverValue = (String) params.getParam("value");
    assignValues(entityManager, request, driverValue);
    return createFromFields();
  }

  private void assignValues(EntityManager entityManager, HttpServletRequest request, String driverValue) throws Exception {

    String sql = PROPS.getString("driver.fieldsql");
    String driverCol = PROPS.getString("driver.field");
    sql = StringUtils.replace(sql, "?", driverCol);
    String cmrIssuingCntry = request.getParameter("cmrIssuingCntry");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    query.setParameter("DRIVER_VALUE", driverValue);
    List<CustScenarios> results = query.getResults(CustScenarios.class);
    if (results == null || results.size() == 0) {
      return;
    }
    int seqNo = 0;
    String fieldId = null;
    String fieldName = null;
    boolean retainValue = false;
    String parentTab = null;
    String requiredInd = null;
    boolean addressField = false;
    String lockInd = null;
    String addrType = null;
    String value = null;
    TemplatedField field = null;
    List<String> withTemplateValues = new ArrayList<String>();
    for (CustScenarios entry : results) {
      fieldId = entry.getId().getFieldId();
      if (!withTemplateValues.contains(fieldId)) {
        withTemplateValues.add(fieldId);
      }
      fieldId = fieldId.replaceAll("#", "");
      fieldName = entry.getFieldName();
      addrType = entry.getId().getAddrTyp();
      seqNo = entry.getId().getSeqNo();
      value = entry.getValue();
      retainValue = "Y".equals(entry.getRetainValInd());
      requiredInd = entry.getReqInd();
      // by default lock, don't affect previous logic
      lockInd = entry.getLockedIndc();
      if (StringUtils.isBlank(lockInd)) {
        lockInd = "Y";
      }
      parentTab = entry.getParTabId();
      addressField = !StringUtils.isBlank(addrType);
      field = getField(fieldId);
      if (field == null) {
        addField(fieldId, fieldId, fieldName, retainValue, parentTab, requiredInd, lockInd);
        field = getField(fieldId);
      }
      if (field != null) {
        if (addressField) {
          field.setAddressField(true);
          if (addrType != null) {
            if (seqNo == 1) {
              field.clearValues(addrType);
              field.setRequiredInd(requiredInd);
              field.setRetainValue(retainValue);
              field.setLockInd(lockInd);
            }
            field.addValue(value, addrType);
          }
        } else {
          field.setAddressField(false);
          if (seqNo == 1) {
            field.clearValues();
            field.setRequiredInd(requiredInd);
            field.setRetainValue(retainValue);
            field.setLockInd(lockInd);
          }
          field.addValue(value);
        }
      }

    }
    clearFieldWithNoValues(entityManager, withTemplateValues, cmrIssuingCntry);

  }

  private void clearFieldWithNoValues(EntityManager entityManager, List<String> withTemplateValues, String cmrIssuingCntry) {
    String sql = PROPS.getString("driver.allfields");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CMR_ISSUING_CNTRY", cmrIssuingCntry);
    List<Object[]> results = query.getResults();
    if (results == null || results.size() == 0) {
      return;
    }
    String fieldId = null;
    String fieldName = null;
    boolean retainValue = false;
    String parentTab = null;
    String requiredInd = null;
    boolean addressField = false;
    String addrType = null;

    TemplatedField field = null;
    for (Object[] entry : results) {

      fieldId = (String) entry[0];
      if (!withTemplateValues.contains(fieldId)) {
        fieldId = fieldId.replaceAll("#", "");
        fieldName = (String) entry[1];
        addrType = (String) entry[3];
        retainValue = true;
        // Field Info: country specific is Required, or Country specific empty
        // and General entry is required
        requiredInd = (String) (StringUtils.isBlank((String) entry[4]) ? entry[5] : entry[4]);
        parentTab = (String) entry[2];
        addressField = !StringUtils.isBlank(addrType);
        field = getField(fieldId);
        if (field == null) {
          addField(fieldId, fieldId, fieldName, retainValue, parentTab, requiredInd, "Y");
          field = getField(fieldId);
        }
        if (field != null) {

          if (addressField) {
            field.setAddressField(true);
            if (addrType != null) {
              field.clearValues(addrType);
              field.setRequiredInd(requiredInd);
              field.setRetainValue(retainValue);
              field.setLockInd("Y");
            }
          } else {
            field.setAddressField(false);
            field.clearValues();
            field.setRequiredInd(requiredInd);
            field.setRetainValue(retainValue);
            field.setLockInd("Y");
          }
        }
      }
    }

  }
}
