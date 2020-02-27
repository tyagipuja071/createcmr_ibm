package com.ibm.cio.cmr.request.ui.template.impl;

import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.template.Template;
import com.ibm.cio.cmr.request.ui.template.TemplateDriver;
import com.ibm.cio.cmr.request.ui.template.TemplateSupport;
import com.ibm.cio.cmr.request.ui.template.TemplatedField;

public class LATemplateSupport extends TemplateSupport {

  private static final ResourceBundle PROPS = ResourceBundle.getBundle("template_la");

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
    List<Object[]> results = query.getResults();
    if (results == null || results.size() == 0) {
      return;
    }
    int seqNo = 0;
    String fieldId = null;
    String fieldName = null;
    boolean retainValue = false;
    String parentTab = null;
    boolean requiredForManualInput = false;
    boolean addressField = false;
    String addrType = null;
    String value = null;
    TemplatedField field = null;
    for (Object[] result : results) {
      fieldId = (String) result[3];
      fieldId = fieldId.replaceAll("#", "");
      fieldName = (String) result[4];
      addrType = (String) result[5];
      seqNo = (int) result[6];
      value = (String) result[7];
      retainValue = "Y".equals(result[8]);
      requiredForManualInput = "Y".equals(result[9]);
      parentTab = (String) result[10];
      addressField = "Y".equals(result[11]);
      field = getField(fieldId);
      if (field == null) {
        addField(fieldId, fieldId, fieldName, retainValue, parentTab, requiredForManualInput ? "Y" : "N");
        field = getField(fieldId);
      }
      if (field != null) {

        if (addressField) {
          field.setAddressField(true);
          if (addrType != null) {
            if (seqNo == 1) {
              field.clearValues(addrType);
            }
            field.addValue(value, addrType);
          }
        } else {
          if (seqNo == 1) {
            field.clearValues();
          }
          field.addValue(value);
        }
      }

    }

  }
}
