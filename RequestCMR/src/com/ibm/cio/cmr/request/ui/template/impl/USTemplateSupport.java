/**
 * 
 */
package com.ibm.cio.cmr.request.ui.template.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
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

/**
 * @author Jeffrey Zamora
 * 
 */
public class USTemplateSupport extends TemplateSupport {

  private static final ResourceBundle PROPS = ResourceBundle.getBundle("template_us");

  public static void main(String[] args) throws Exception {
    String name = "C:\\projects\\RDC\\projects\\CMMA\\createCMR\\scenariosmap.txt";
    File f = new File(name);
    FileReader fr = new FileReader(f);
    try {
      BufferedReader br = new BufferedReader(fr);
      try {
        PrintWriter pw = new PrintWriter(name + ".sql");
        try {

          String line = null;
          String[] parts = null;
          StringBuilder main = new StringBuilder(
              "insert into CMMA.USCMR_SCENARIO_G (REFT_USCMR_COL_KEY, PICK_ORDER_NUM, SCREEN_NAME, LABEL_NAME,  ADDR_TYPE, VALUE_1, VALUE_2, VALUE_3, VALUE_4, VALUE_5, VALUE_6, VALUE_7, VALUE_8, VALUE_9, VALUE_10, VALUE_11, VALUE_12, VALUE_13, VALUE_14, VALUE_15, VALUE_16, VALUE_17, VALUE_18, VALUE_19, VALUE_20, VALUE_21, VALUE_22, VALUE_23, VALUE_24, VALUE_25, VALUE_26, VALUE_27, VALUE_28, VALUE_29, VALUE_30, VALUE_31, VALUE_32, VALUE_33, VALUE_34, VALUE_35, VALUE_36, VALUE_37, VALUE_38, VALUE_39, VALUE_40, VALUE_41, VALUE_42, VALUE_43, VALUE_44, VALUE_45, VALUE_46, VALUE_47, VALUE_48, VALUE_49, VALUE_50, VALUE_51, VALUE_52, VALUE_53, VALUE_54, VALUE_55, VALUE_56, VALUE_57) values (");
          while ((line = br.readLine()) != null) {
            StringBuilder sb = new StringBuilder();
            parts = line.split("\t");
            if (parts.length == 62) {
              // noop
              for (int i = 0; i < 62; i++) {
                if (i < 2) {
                  sb.append(i == 0 ? "" : ", ").append(nullClean(parts[i], true)).append("");
                } else {
                  sb.append(", ").append(nullClean(parts[i], false));
                }
              }
            } else if (parts.length == 5) {
              sb.append("").append(clean(parts[0])).append("");
              sb.append(", ").append(clean(parts[1])).append("");
              sb.append(", '").append(clean(parts[2])).append("'");
              sb.append(", '").append(clean(parts[3])).append("'");
              sb.append(", '").append(clean(parts[4])).append("'");
            } else if (parts.length == 4) {
              sb.append("").append(clean(parts[0])).append("");
              sb.append(", '").append(clean(parts[1])).append("'");
              sb.append(", '").append(clean(parts[2])).append("'");
              sb.append(", '").append(clean(parts[3])).append("'");
            } else {
              throw new Exception("One line has an error");
            }
            if (parts.length != 62) {
              sb.append(", current date, 'Inserted by Dev Team', current timestamp, current timestamp");
            }
            pw.println(main.toString() + sb.toString() + ");");
          }

        } finally {
          pw.close();
        }

      } finally {
        br.close();
      }
    } finally {
      fr.close();
    }
  }

  private static String nullClean(String input, boolean number) {
    if (StringUtils.isEmpty(input)) {
      return "null";
    }
    if ("xxx".equals(input)) {
      return "null";
    }
    if (number) {
      return StringUtils.replace(input, "'", "''").trim();
    }
    return "'" + StringUtils.replace(input, "'", "''").trim() + "'";
  }

  private static String clean(String input) {
    return StringUtils.replace(input, "'", "''").trim();
  }

  @Override
  protected void init() {
    String value = null;
    String[] parts = null;

    String mapField = null;
    String fieldId = null;
    String fieldName = null;
    boolean retainValue = false;
    String parentTab = null;
    String requiredInd = null;
    for (String key : PROPS.keySet()) {
      if (key.startsWith("field.")) {

        value = PROPS.getString(key);
        parts = value.split(",");

        mapField = key.substring(6);
        fieldId = parts[0];
        fieldName = parts[1];
        retainValue = "Y".equals(parts[2]);
        parentTab = parts.length >= 4 && !"X".equals(parts[3]) ? parts[3] : null;
        requiredInd = parts.length == 5 ? ("Y".equals(parts[4]) ? "Y" : "Y") : "N";

        if (parts.length >= 3) {
          addField(mapField, fieldId, fieldName, retainValue, parentTab, requiredInd);
        }
      }
    }
  }

  @Override
  public String getCmrIssuingCountry() {
    return "897";
  }

  @Override
  public TemplateDriver getDriverField() {
    TemplateDriver driver = new TemplateDriver();
    driver.setFieldId(PROPS.getString("driver.fieldId"));
    driver.setFieldName(PROPS.getString("driver.fieldName"));
    return driver;
  }

  @Override
  protected Template doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String driverValue = (String) params.getParam("value");

    String valueColumn = getValueColumn(entityManager, driverValue);
    if (valueColumn == null) {
      return null;
    }
    assignValues(entityManager, driverValue, valueColumn);

    return createFromFields();

  }

  private void assignValues(EntityManager entityManager, String driverValue, String valueColumn) {
    String sql = PROPS.getString("driver.fieldsql");
    sql = StringUtils.replace(sql, "?", valueColumn);
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Object[]> results = query.getResults();
    if (results == null || results.size() == 0) {
      return;
    }

    String label = null;
    String value = null;
    int pickOrder = 0;
    boolean addressField = false;
    String addrType = null;
    TemplatedField field = null;
    for (Object[] result : results) {
      pickOrder = (int) result[1];
      value = (String) result[2];
      label = (String) result[0];
      addressField = "Y".equals(result[3]);

      label = StringUtils.replace(label, " ", "_");
      label = StringUtils.replace(label, "/", "_");
      label = StringUtils.replace(label, "'", "_");

      field = getField(label);
      if (field != null) {

        if (addressField) {
          field.setAddressField(true);
          addrType = (String) result[4];
          if (addrType != null) {
            if (pickOrder == 1) {
              field.clearValues(addrType);
            }
            field.addValue(value, addrType);
          }
        } else {
          if (pickOrder == 1) {
            field.clearValues();
          }
          field.addValue(value);
        }
      }
    }
  }

  private String getValueColumn(EntityManager entityManager, String value) {
    String sql = PROPS.getString("driver.templatesql");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("SUB_GROUP", value);
    String valueColumn = query.getSingleResult(String.class);
    if (valueColumn != null) {
      return StringUtils.replace(valueColumn, "COL", "VALUE_");
    }
    return null;
  }

}
