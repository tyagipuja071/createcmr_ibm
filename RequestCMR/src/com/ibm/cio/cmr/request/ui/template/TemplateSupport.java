/**
 * 
 */
package com.ibm.cio.cmr.request.ui.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.service.BaseSimpleService;

/**
 * Base class to handle template support for countries
 * 
 * @author Jeffrey Zamora
 * 
 */
public abstract class TemplateSupport extends BaseSimpleService<Template> {

  private Map<String, TemplatedField> fields = new HashMap<String, TemplatedField>();

  public abstract String getCmrIssuingCountry();

  protected abstract TemplateDriver getDriverField();

  public TemplateSupport() {
    init();
  }

  // can override by implementations if needed
  protected void init() {
    // NOOP
  }

  protected void addField(String mapField, String fieldId, String fieldName, boolean retainValue, String parentTab, String requiredInd) {
    addField(mapField, fieldId, fieldName, retainValue, parentTab, requiredInd, "Y");
  }

  protected void addField(String mapField, String fieldId, String fieldName, boolean retainValue, String parentTab, String requiredInd, String lockInd) {
    TemplatedField field = new TemplatedField();
    field.setFieldId(fieldId);
    field.setFieldName(fieldName);
    field.setRetainValue(retainValue);
    field.setParentTab(parentTab);
    field.setRequiredInd(requiredInd);
    field.setLockInd(lockInd);
    this.fields.put(mapField, field);
  }

  protected TemplatedField getField(String mapField) {
    return this.fields.get(mapField);
  }

  protected void clearValues(String fieldId) {
    TemplatedField field = this.fields.get(fieldId);
    if (field != null) {
      field.clearValues();
    }
  }

  protected void addValue(String fieldId, String value) {
    if (value == null) {
      return;
    }
    TemplatedField field = this.fields.get(fieldId);
    if (field != null) {
      field.addValue(value);
    }
  }

  public Template getTemplate(String driverFieldValue, HttpServletRequest request) throws Exception {
    ParamContainer params = new ParamContainer();
    params.addParam("value", driverFieldValue);
    return process(request, params);
  }

  protected Template createFromFields() {
    Template template = new Template();
    template.setCmrIssuingCountry(getCmrIssuingCountry());
    template.setDriver(getDriverField());
    List<TemplatedField> fields = new ArrayList<TemplatedField>();
    fields.addAll(this.fields.values());
    template.setFields(fields);
    return template;
  }

}
