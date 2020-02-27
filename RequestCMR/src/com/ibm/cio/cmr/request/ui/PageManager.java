/**
 * 
 */
package com.ibm.cio.cmr.request.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.controller.DropdownListController;
import com.ibm.cio.cmr.request.entity.CompoundEntity;
import com.ibm.cio.cmr.request.entity.FieldInfo;
import com.ibm.cio.cmr.request.entity.FieldLbl;
import com.ibm.cio.cmr.request.model.DropdownItemModel;
import com.ibm.cio.cmr.request.model.DropdownModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.ui.taglib.TabInfo;
import com.ibm.cio.cmr.request.util.JpaManager;

/**
 * Handles the customizable pages
 * 
 * @author Jeffrey Zamora
 * 
 */
public class PageManager implements Serializable {

  private static final long serialVersionUID = 1L;

  private static PageManager instance;

  private static final Logger LOG = Logger.getLogger(PageManager.class);

  private static final String REQUIRED = "R";
  private static final String CONDITIONAL = "C";
  private static final String OPTIONAL = "O";
  private static final String GRAYED_OUT = "G";
  private static final String HIDDEN = "H";
  private static final String READONLY = "D";

  private static final String DROPDOWN = "D";
  private static final String RADIO = "R";
  private static final String TEXT = "T";
  private static final String BLUEPAGES = "B";
  // private static final String READONLY = "O";

  private static final String LOV = "L";

  // private static final String BDS = "B";

  private static final String REQUEST_TYPE_FIELD_ID = "##RequestType";

  private Map<String, FieldManager> fieldMap = new HashMap<String, FieldManager>();
  private List<String> countries = new ArrayList<String>();
  private Map<String, List<String>> geos = new HashMap<>();
  private Map<String, String> defaultLandedCountries = new HashMap<>();
  private List<String> autoProcCountries = new ArrayList<String>();
  private List<String> laReactivateCountries = new ArrayList<String>();

  public static void init() throws Exception {
    initManager();
  }

  public static void init(EntityManager entityManager) throws Exception {
    initManager(entityManager);
  }

  public void addFieldManager(String fieldId, FieldManager mgr) {
    this.fieldMap.put(fieldId, mgr);
  }

  public FieldManager getManager(String fieldId) {
    return this.fieldMap.get(fieldId);
  }

  public Map<String, FieldManager> getFieldMap() {
    return fieldMap;
  }

  public void setFieldMap(Map<String, FieldManager> fieldMap) {
    this.fieldMap = fieldMap;
  }

  private static void initManager() throws Exception {
    EntityManager em = JpaManager.getEntityManager();
    try {
      initManager(em);
    } finally {
      em.clear();
      em.close();
    }

  }

  private static void initManager(EntityManager em) throws Exception {
    instance = new PageManager();
    LOG.info("Initializing Page Manager information...");

    LOG.debug("Getting field information...");
    String sql = ExternalizedQuery.getSql("FIELDINFO.INIT");
    PreparedQuery query = new PreparedQuery(em, sql);
    query.setForReadOnly(true);
    List<CompoundEntity> entities = query.getCompundResults(FieldInfo.class, FieldInfo.FIELD_INFO_MAP);
    FieldInfo info = null;
    String lbl = null;
    FieldManager mgr = null;
    FieldInformation fieldInfo = null;
    String fieldId = null;
    String cmrCntry = null;
    for (CompoundEntity entity : entities) {
      info = entity.getEntity(FieldInfo.class);
      lbl = (String) entity.getValue("LBL_CNTRY");

      fieldId = info.getId().getFieldId().replaceAll("#", "");
      cmrCntry = convertCntry(info.getId().getCmrIssuingCntry());

      if (instance.getManager(fieldId) == null) {
        if (mgr != null) {
          instance.addFieldManager(mgr.getFieldId(), mgr);
        }
        mgr = new FieldManager();
        mgr.setFieldId(fieldId);
        mgr.setType(info.getType());
        mgr.setValidation(info.getValidation());
        if (RADIO.equals(info.getType()) && LOV.equals(info.getChoice()) && mgr.getItemMap().size() == 0) {
          Map<String, List<DropdownItemModel>> map = getChoices(em, fieldId);
          mgr.setItemMap(map);
        }
        instance.addFieldManager(fieldId, mgr);
      }
      fieldInfo = new FieldInformation();
      PropertyUtils.copyProperties(fieldInfo, info);
      fieldInfo.setFieldId(fieldId);
      fieldInfo.setCmrIssuingCntry(cmrCntry);
      fieldInfo.setSeqNo(info.getId().getSeqNo());
      fieldInfo.setLabel(lbl);
      mgr.addFieldInfo(cmrCntry, fieldInfo);
      if (!instance.countries.contains(cmrCntry)) {
        instance.countries.add(cmrCntry);
      }
    }
    instance.addFieldManager(mgr.getFieldId(), mgr);

    LOG.debug("Getting field labels...");
    sql = ExternalizedQuery.getSql("FIELDINFO.INITLBL");
    query = new PreparedQuery(em, sql);
    query.setForReadOnly(true);
    List<FieldLbl> labels = query.getResults(FieldLbl.class);
    for (FieldLbl label : labels) {
      fieldId = label.getId().getFieldId().replaceAll("#", "");
      cmrCntry = convertCntry(label.getId().getCmrIssuingCntry());

      if (!instance.countries.contains(cmrCntry)) {
        instance.countries.add(cmrCntry);
      }

      if (instance.getManager(fieldId) == null) {
        mgr = new FieldManager();
        mgr.setFieldId(fieldId);
        instance.addFieldManager(fieldId, mgr);
      }
      mgr = instance.getManager(fieldId);
      List<FieldInformation> list = mgr.getFieldInfo(cmrCntry);
      if (list == null || list.size() == 0) {
        List<FieldInformation> listDefault = mgr.getFieldInfo("all");
        if (listDefault != null && !listDefault.isEmpty()) {
          FieldInformation defaultInfo = listDefault.get(0);
          fieldInfo = new FieldInformation();
          try {
            PropertyUtils.copyProperties(fieldInfo, defaultInfo);
          } catch (Exception e) {
            // noop
          }
          fieldInfo.setLabel(label.getLbl());
          fieldInfo.setFieldId(fieldId);
          fieldInfo.setCmrIssuingCntry(cmrCntry);
          mgr.addFieldInfo(cmrCntry, fieldInfo);
        } else {
          fieldInfo = new FieldInformation();
          fieldInfo.setLabel(label.getLbl());
          fieldInfo.setSeqNo(0);
          fieldInfo.setFieldId(fieldId);
          fieldInfo.setCmrIssuingCntry(cmrCntry);
          mgr.addFieldInfo(cmrCntry, fieldInfo);
        }
      } else {
        for (FieldInformation info2 : list) {
          String lblCntry = cmrCntry;
          String infoCntry = convertCntry(info2.getCmrIssuingCntry());
          if (infoCntry.equals(lblCntry)) {
            info2.setLabel(label.getLbl());
          }
        }
      }
    }

    // init GEOS
    LOG.debug("Initializing GEOs...");

    sql = ExternalizedQuery.getSql("GEOS.INIT");

    query = new PreparedQuery(em, sql);
    query.setForReadOnly(true);
    List<Object[]> geos = query.getResults();
    if (geos != null && geos.size() > 0) {
      String geoCd = null;
      String cntry = null;
      String defaultLandCntry = null;
      for (Object[] geo : geos) {
        geoCd = (String) geo[0];
        cntry = (String) geo[1];
        defaultLandCntry = (String) geo[2];
        if (instance.geos.get(geoCd) == null) {
          instance.geos.put(geoCd, new ArrayList<String>());
        }
        instance.geos.get(geoCd).add(cntry);
        if (!instance.defaultLandedCountries.containsKey(cntry)) {
          instance.defaultLandedCountries.put(cntry, defaultLandCntry);
        }
      }
    }

    LOG.debug("Loading auto-processing enabled countries...");
    sql = ExternalizedQuery.getSql("AUTOPROCCNTRY.GET");
    query = new PreparedQuery(em, sql);
    query.setForReadOnly(true);
    List<String> countries = query.getResults(String.class);
    for (String code : countries) {
      instance.autoProcCountries.add(code);
    }

    LOG.debug("Loading LA Reactivate enabled countries...");
    sql = ExternalizedQuery.getSql("LAREACTIVATECOUNTRY.GET");
    query = new PreparedQuery(em, sql);
    // query.setParameter("CNTRY_CD", cmrCntry);
    query.setForReadOnly(true);
    List<String> countriesLAReactivate = query.getResults(String.class);
    for (String suppReqTyp : countriesLAReactivate) {
      instance.laReactivateCountries.add(suppReqTyp);
    }
  }

  private static Map<String, List<DropdownItemModel>> getChoices(EntityManager entityManager, String fieldId) {
    String sql = ExternalizedQuery.getSql("FIELDINFO.GETCHOICES");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("FIELD", "##" + fieldId);
    query.setForReadOnly(true);
    List<Object[]> results = query.getResults();
    Map<String, List<DropdownItemModel>> choiceMap = new HashMap<String, List<DropdownItemModel>>();
    String currCntry = "";
    DropdownItemModel model = null;
    for (Object[] result : results) {
      if (!currCntry.equals(result[2])) {
        choiceMap.put(result[2].toString(), new ArrayList<DropdownItemModel>());
        currCntry = result[2].toString();
      }
      model = new DropdownItemModel();
      model.setId((String) result[0]);
      model.setName((String) result[1]);
      choiceMap.get(result[2].toString()).add(model);
    }
    return choiceMap;
  }

  public static String toJSON() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.writeValueAsString(instance);
  }

  private static String convertCntry(String cmrIssuingCntry) {
    String cntry = null;
    if ("*".equals(cmrIssuingCntry)) {
      cntry = "all";
    } else {
      cntry = "c" + cmrIssuingCntry;
    }
    return cntry;
  }

  public static String getLabel(HttpServletRequest request, String fieldId, String defaultLabel) {
    String cntry = getCurrentCountry(request);
    return getLabel(cntry, fieldId, defaultLabel);
  }

  public static String getLabel(String cmrCountry, String fieldId, String defaultLabel) {
    String cntry = cmrCountry;
    if (cntry == null) {
      cntry = "all";
    } else {
      cntry = "c" + cntry;
    }
    FieldManager mgr = instance.getManager(fieldId);
    if (mgr == null) {
      return defaultLabel;
    }
    List<FieldInformation> info = mgr.getFieldInfo(cntry);
    String label = null;
    if (info != null) {
      for (FieldInformation inf : info) {
        label = inf.getLabel();
        if (!StringUtils.isEmpty(label)) {
          break;
        }
      }
    }
    if (StringUtils.isEmpty(label)) {
      info = mgr.getFieldInfo("all");
      label = null;
      if (info != null) {
        for (FieldInformation inf : info) {
          label = inf.getLabel();
          if (!StringUtils.isEmpty(label)) {
            break;
          }
        }
      }
    }
    return StringUtils.isEmpty(label) ? defaultLabel : label.trim();
  }

  public static void initFor(HttpServletRequest request, RequestEntryModel reqEntry) {
    request.setAttribute("pageMgrCMRCntry", reqEntry.getCmrIssuingCntry());
    request.setAttribute("pageMgrReqType", reqEntry.getReqType());
    request.setAttribute("pageConfig", new PageConfig());
    request.setAttribute("pageTabs", new ArrayList<TabInfo>());
  }

  public static void initFor(HttpServletRequest request, String cntry, String reqType) {
    request.setAttribute("pageMgrCMRCntry", cntry);
    request.setAttribute("pageMgrReqType", reqType);
    request.setAttribute("pageConfig", new PageConfig());
  }

  public static PageConfig getConfig(HttpServletRequest request) {
    PageConfig config = (PageConfig) request.getAttribute("pageConfig");
    if (config == null) {
      config = new PageConfig();
      request.setAttribute("pageConfig", config);
    }
    return config;
  }

  /**
   * Creates the dynamic scripts for configuration
   * 
   * @param request
   * @return
   */
  public static String getScripts(HttpServletRequest request, boolean readOnly, boolean newEntry) {
    StringBuilder sb = new StringBuilder();
    sb.append("<script>\n");
    sb.append("  dojo.addOnLoad(function() {\n");
    if (readOnly) {
      sb.append("     PageManager.setReadOnly(true);\n");
    }
    sb.append("    // dropdowns\n");
    PageConfig config = getConfig(request);
    String field = null;
    String fieldNm = null;
    String refTable = null;
    FieldManager fieldMgr = null;
    FieldInformation mainInfo = null;

    String cmrIssuingCountry = getCurrentCountry(request);
    String reqType = getCurrentReqType(request);

    for (String dropdown : config.getDropdowns().keySet()) {
      field = config.getFieldMap().get(dropdown);
      fieldMgr = instance.getManager(dropdown);
      if (fieldMgr != null) {
        List<FieldInformation> infos = fieldMgr.getFieldInfo(cmrIssuingCountry == null ? "all" : "c" + cmrIssuingCountry);
        if (infos == null) {
          infos = fieldMgr.getFieldInfo("all");
        }
        if (infos != null) {
          for (FieldInformation inf : infos) {
            if (inf.getSeqNo() == 1) {
              mainInfo = inf;
              break;
            }
          }
        }
      }
      refTable = config.getDropdowns().get(dropdown);
      if (refTable.contains(":")) {
        String[] reference = refTable.split(":");
        refTable = reference[0];
        dropdown = reference[1];
      }

      if (mainInfo != null && !StringUtils.isEmpty(mainInfo.getValDependsOn())) {
        StringBuilder dropParams = new StringBuilder();
        String[] params = mainInfo.getValDependsOn().split(",");
        for (String param : params) {
          if ("nocache".equals(param)) {
            dropParams.append(dropParams.length() > 0 ? "&" : "");
            dropParams.append(param + "=Y");
          } else {
            param = param.replaceAll("#", "");
            fieldNm = config.getFieldMap().get(param);
            dropParams.append(dropParams.length() > 0 ? "&" : "");
            dropParams.append(fieldNm + "=_" + fieldNm);
          }
        }
        if ("nocache".equals(mainInfo.getValDependsOn())) {
          // only no cache param, straight load
          sb.append("    FilteringDropdown.loadItems('" + field + "', '" + field + "_spinner', '" + refTable + "', 'fieldId=" + dropdown + "&"
              + dropParams.toString() + "');\n");
        } else {
          sb.append("    FilteringDropdown.loadOnChange('" + field + "', '" + field + "_spinner', '" + refTable + "', 'fieldId=" + dropdown + "&"
              + dropParams.toString() + "', '" + fieldNm + "');\n");
        }
      } else {
        if (!newEntry && ("CMRIssuingCountry".equals(dropdown) || "RequestType".equals(dropdown))) {
          DropdownModel model = DropdownListController.getCachedModel(dropdown, cmrIssuingCountry);
          if (model != null && model.getItems() != null && model.getItems().size() > 0) {
            ObjectMapper mapper = new ObjectMapper();
            try {
              String json = mapper.writeValueAsString(model);
              json = "{ \"listItems\" : " + json + " }";
              sb.append("    FilteringDropdown.loadFixedItems('" + field + "', '" + field + "_spinner', " + json + ");\n");
            } catch (Exception e) {
              sb.append("    FilteringDropdown.loadItems('" + field + "', '" + field + "_spinner', '" + refTable + "', 'fieldId=" + dropdown
                  + "');\n");
            }
          } else {
            sb.append("    FilteringDropdown.loadItems('" + field + "', '" + field + "_spinner', '" + refTable + "', 'fieldId=" + dropdown + "');\n");
          }
        } else {
          sb.append("    FilteringDropdown.loadItems('" + field + "', '" + field + "_spinner', '" + refTable + "', 'fieldId=" + dropdown + "');\n");
        }
      }
    }
    sb.append("\n");
    if (newEntry) {
      sb.append("    window.setTimeout('waitForConfig()', 2000);\n");
    } else {
      sb.append("    window.setTimeout('configChangeFor_c" + cmrIssuingCountry + "()', 500);\n");
    }
    sb.append("  });\n\n");

    if (newEntry) {
      sb.append("  function waitForConfig(){\n");
      sb.append("     var cmrCntryhandle = dojo.connect(FormManager.getField('cmrIssuingCntry'), 'onChange', function(value){\n");
      sb.append("       try {\n");
      sb.append("         if (!value){\n");
      sb.append("           value = FormManager.getActualValue('cmrIssuingCntry');\n");
      sb.append("         }\n");
      sb.append("         eval('configChangeFor_c'+value+'();')\n");
      sb.append("       } catch (e){\n");
      sb.append("         configChangeFor_all();\n");
      sb.append("       }\n");
      sb.append("     });\n");
      sb.append("     cmrCntryhandle[0].onChange();\n");
      sb.append("  }\n\n");
    }
    // create the handlers
    List<FieldInformation> infoList = null;
    String required = null;
    String tabId = null;
    String label = null;
    String depends = null;
    String dependsNm = null;
    String dependsType = null;
    FieldInformation reqTypeDependsInfo = null;

    List<String> availCountries = instance.countries;
    if (!newEntry) {
      // limit the configurations here
      availCountries = new ArrayList<>();
      availCountries.add("c" + cmrIssuingCountry);
    }
    for (String country : availCountries) {
      sb.append("  function configChangeFor_" + country + "(){\n");
      sb.append("    if (FormManager) {\n");
      if ("all".equals(country) || !newEntry) {
        // sb.append("      FormManager.resetValidations();\n");
        sb.append("      PageManager.clearHandles();\n");
      } else {
        sb.append("      configChangeFor_all();\n");
      }
      for (String fieldId : config.getFieldMap().keySet()) {
        fieldMgr = instance.getManager(fieldId);
        fieldNm = config.getFieldMap().get(fieldId);
        label = PageManager.getLabel(request, fieldId, fieldId);
        tabId = config.getTabs().get(fieldId);
        if (tabId != null) {
          tabId = "'" + tabId + "'";
        }
        if (fieldMgr != null) {
          infoList = fieldMgr.getFieldInfo(country);
          if (!newEntry && (infoList == null || infoList.size() == 0)) {
            // regress to use the general config
            infoList = fieldMgr.getFieldInfo("all");
          }
          if (infoList != null && infoList.size() > 0) {
            Collections.sort(infoList);

            for (FieldInformation info : infoList) {
              // set the default settings
              if (info.getSeqNo() == 1) {
                mainInfo = info;
              }

              required = info.getRequired();

              reqTypeDependsInfo = getRequestTypeDependentInfo(infoList);
              if (!newEntry && reqTypeDependsInfo != null && info.getSeqNo() != reqTypeDependsInfo.getSeqNo()) {
                if (reqType != null && reqType.equals(reqTypeDependsInfo.getDependsSetting())) {
                  required = ("Y".equals(reqTypeDependsInfo.getCondReqInd()) ? REQUIRED : OPTIONAL);
                } else {
                  // switch to default setting
                  required = mainInfo.getRequired();// ("Y".equals(reqTypeDependsInfo.getCondReqInd())
                                                    // ? OPTIONAL : REQUIRED);
                }
              }

              depends = info.getDependsOn();

              if (!newEntry && REQUEST_TYPE_FIELD_ID.equals(depends)) {
                continue;
              }

              if (depends != null) {
                depends = depends.replaceAll("#", "");
              }
              dependsNm = config.getFieldMap().get(depends);
              if (depends != null) {
                dependsType = instance.getManager(depends) != null ? instance.getManager(depends).getType() : null;
              }

              sb.append("\n     /* " + fieldNm + " (" + label + ") */\n");
              if (readOnly) {
                // sb.append("      FormManager.resetValidations('" + fieldNm +
                // "');\n");
                sb.append("      FormManager.readOnly('" + fieldNm + "');\n");
              } else {
                boolean main = "all".equals(country) || !newEntry;
                if (REQUIRED.equals(required)) {
                  addRequiredValidations(request, fieldMgr, sb, fieldNm, label, tabId, main, fieldId, false);
                } else if (READONLY.equals(required)) {
                  addReadOnlyConditions(sb, fieldNm, main, fieldId, false);
                } else if (GRAYED_OUT.equals(required)) {
                  addGrayedOutConditions(sb, fieldNm, main, fieldId, false);
                } else if (HIDDEN.equals(required)) {
                  addHiddenConditions(sb, fieldNm, main, fieldId, false);
                } else if (CONDITIONAL.equalsIgnoreCase(required)) {
                  if (!StringUtils.isEmpty(dependsNm)) {
                    if (DROPDOWN.equals(dependsType) || TEXT.equals(dependsType)) {
                      generateScriptsForDependencies(request, fieldMgr, config, info, mainInfo, sb, fieldNm, label, tabId, main);
                    } else if (RADIO.equals(dependsType)) {
                      generateScriptsForRadioDependencies(request, fieldMgr, config, info, mainInfo, sb, fieldNm, label, tabId, main);
                    }
                  }
                } else if (OPTIONAL.equalsIgnoreCase(required)) {
                  addOptionalConditions(request, fieldMgr, sb, fieldNm, label, tabId, main, fieldId, false);
                }

                if (!StringUtils.isEmpty(info.getLabel())) {
                  sb.append("      FormManager.changeLabel('" + fieldId + "',\"" + info.getLabel() + "\");\n\n");
                }
              }
            }
          }
        }
      }
      sb.append("      if (afterConfigChange){\n");
      if (!newEntry) {
        sb.append("    window.setTimeout('afterConfigChange()', 1500);\n");
      } else {
        sb.append("        afterConfigChange();\n");
      }
      sb.append("      }\n");
      sb.append("      FormManager.ready();\n");
      sb.append("    }\n");
      sb.append("  };\n");
    }
    sb.append("</script>\n");
    return sb.toString();
  }

  private static FieldInformation getRequestTypeDependentInfo(List<FieldInformation> infoList) {
    for (FieldInformation info : infoList) {
      if (REQUEST_TYPE_FIELD_ID.equals(info.getDependsOn())) {
        return info;
      }
    }
    return null;
  }

  private static void generateScriptsForDependencies(HttpServletRequest request, FieldManager fieldMgr, PageConfig config, FieldInformation info,
      FieldInformation mainInfo, StringBuilder sb, String fieldNm, String label, String tabId, boolean main) {
    String depends = info.getDependsOn();
    String dependsType = null;
    FieldManager dependsMgr = instance.getManager(depends);
    if (dependsMgr != null) {
      dependsType = dependsMgr.getType();
    }
    if (depends != null) {
      depends = depends.replaceAll("#", "");
    }
    String dependsNm = config.getFieldMap().get(depends);

    String dependsCondition = null;
    String dependsSetting = info.getDependsSetting();
    String dependsRequired = info.getCondReqInd();

    sb.append("      var " + fieldNm + "_handle" + info.getSeqNo() + " = ");

    sb.append("dojo.connect(FormManager.getField('" + dependsNm + "'),'onChange', function(value){\n");
    if (StringUtils.isEmpty(dependsSetting)) {
      dependsCondition = "value && dojo.string.trim(value) != ''";
    } else {
      String[] values = dependsSetting.split(",");
      StringBuilder condSb = new StringBuilder();
      for (String value : values) {
        condSb.append(condSb.length() > 0 ? " || " : "");
        condSb.append("(value && dojo.string.trim(value) == '" + value + "')");
      }
      dependsCondition = condSb.toString();
    }
    sb.append("          var value = FormManager.getActualValue('" + dependsNm + "');\n");
    sb.append("          if (" + dependsCondition + ") {\n");
    if (CmrConstants.YES_NO.Y.toString().equals(dependsRequired)) {
      addRequiredValidations(request, fieldMgr, sb, fieldNm, label, tabId, main, info.getFieldId(), true);
    } else {
      addOptionalConditions(request, fieldMgr, sb, fieldNm, label, tabId, main, info.getFieldId(), true);
    }
    sb.append("          } else {\n");
    if (mainInfo != null && REQUIRED.equals(mainInfo.getRequired())) {
      addRequiredValidations(request, fieldMgr, sb, fieldNm, label, tabId, main, info.getFieldId(), true);
    } else if (mainInfo != null && HIDDEN.equals(mainInfo.getRequired())) {
      addHiddenConditions(sb, fieldNm, main, info.getFieldId(), true);
    } else if (mainInfo != null && READONLY.equals(mainInfo.getRequired())) {
      addReadOnlyConditions(sb, fieldNm, main, info.getFieldId(), true);
    } else if (mainInfo != null && GRAYED_OUT.equals(mainInfo.getRequired())) {
      addGrayedOutConditions(sb, fieldNm, main, info.getFieldId(), true);
    } else if (mainInfo != null && OPTIONAL.equals(mainInfo.getRequired())) {
      addOptionalConditions(request, fieldMgr, sb, fieldNm, label, tabId, main, info.getFieldId(), true);
    }
    sb.append("          }\n");
    sb.append("        });\n");
    sb.append("      PageManager.addHandler(" + fieldNm + "_handle" + info.getSeqNo() + ", '" + fieldNm + "_handle" + info.getSeqNo() + "');\n");
    sb.append("      " + fieldNm + "_handle" + info.getSeqNo() + "[0].onChange();");
    if (TEXT.equals(dependsType)) {
      sb.append("      if (" + fieldNm + "_handle" + info.getSeqNo() + "[0]._lastValueReported && " + fieldNm + "_handle" + info.getSeqNo()
          + "[0]._lastValueReported != ''){\n");
      if (CmrConstants.YES_NO.Y.toString().equals(dependsRequired)) {
        addRequiredValidations(request, fieldMgr, sb, fieldNm, label, tabId, main, info.getFieldId(), true);
      } else {
        addOptionalConditions(request, fieldMgr, sb, fieldNm, label, tabId, main, info.getFieldId(), true);
      }
      sb.append("      }\n");
    }
  }

  private static void generateScriptsForRadioDependencies(HttpServletRequest request, FieldManager fieldMgr, PageConfig config,
      FieldInformation info, FieldInformation mainInfo, StringBuilder sb, String fieldNm, String label, String tabId, boolean main) {
    String depends = info.getDependsOn();
    if (depends != null) {
      depends = depends.replaceAll("#", "");
    }
    String dependsNm = config.getFieldMap().get(depends);

    String dependsCondition = null;
    String dependsSetting = info.getDependsSetting();
    String dependsRequired = info.getCondReqInd();

    if (!StringUtils.isEmpty(dependsSetting)) {
      String[] values = dependsSetting.split(",");
      StringBuilder condSb = new StringBuilder();
      for (String value : values) {
        condSb.append(condSb.length() > 0 ? " || " : "");
        condSb.append("FormManager.getActualValue('" + dependsNm + "') == '" + value + "'");
      }
      dependsCondition = condSb.toString();
      int i = 0;
      for (String value : values) {
        sb.append("      var " + fieldNm + "_handle_sub" + i + " = ");
        sb.append("dojo.connect(FormManager.getField('" + dependsNm + "_" + value + "'),'onChange', function(value){\n");
        sb.append("          var value = FormManager.getActualValue('" + dependsNm + "');\n");
        sb.append("          if (" + dependsCondition + ") {\n");
        if (CmrConstants.YES_NO.Y.toString().equals(dependsRequired)) {
          addRequiredValidations(request, fieldMgr, sb, fieldNm, label, tabId, main, info.getFieldId(), true);
        } else {
          addOptionalConditions(request, fieldMgr, sb, fieldNm, label, tabId, main, info.getFieldId(), true);
        }
        sb.append("          } else {\n");
        if (mainInfo != null && REQUIRED.equals(mainInfo.getRequired())) {
          addRequiredValidations(request, fieldMgr, sb, fieldNm, label, tabId, main, info.getFieldId(), true);
        } else if (mainInfo != null && GRAYED_OUT.equals(mainInfo.getRequired())) {
          addGrayedOutConditions(sb, fieldNm, main, info.getFieldId(), true);
        } else if (mainInfo != null && READONLY.equals(mainInfo.getRequired())) {
          addReadOnlyConditions(sb, fieldNm, main, info.getFieldId(), true);
        } else if (mainInfo != null && OPTIONAL.equals(mainInfo.getRequired())) {
          addOptionalConditions(request, fieldMgr, sb, fieldNm, label, tabId, main, info.getFieldId(), true);
        }
        sb.append("          }\n");
        sb.append("        });\n");
        sb.append("      PageManager.addHandler(" + fieldNm + "_handle_sub" + i + ", '" + fieldNm + "_handle_sub" + i + "');\n");
        sb.append("      " + fieldNm + "_handle_sub" + i + "[0].onChange();");

        i++;
      }
    }

  }

  private static void addRequiredValidations(HttpServletRequest request, FieldManager fieldMgr, StringBuilder sb, String fieldNm, String label,
      String tabId, boolean main, String fieldId, boolean retainHandlers) {
    sb.append("      FormManager.resetValidations('" + fieldNm + "');\n");
    if (!main && !retainHandlers) {
      sb.append("      PageManager.removeHandlers('" + fieldNm + "');\n");
    }
    sb.append("      FormManager.show('" + fieldId + "');\n");
    sb.append("      FormManager.enable('" + fieldNm + "');\n");
    sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.REQUIRED, [ \"" + label + "\" ], " + tabId + ");\n");
    if (TEXT.equals(fieldMgr.getType())) {
      int maxLength = getMaxLength(fieldMgr, request);
      if (maxLength > 0) {
        sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.MAXLENGTH, [ \"" + label + "\", " + maxLength + " ], " + tabId
            + ");\n");
      }
    }
    if (DROPDOWN.equals(fieldMgr.getType())) {
      sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.INVALID_VALUE, [ \"" + label + "\" ], " + tabId + ");\n");
    }
    if (BLUEPAGES.equals(fieldMgr.getType())) {
      sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.BLUEPAGES, [ \"" + label + "\" ], " + tabId + ");\n");
    }

    FieldInformation cntryInfo = getFieldInfoForCountry(fieldMgr, request);
    String validation = cntryInfo.getValidation();

    if (!StringUtils.isEmpty(validation) && validation.startsWith("DB")) {
      sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.DB('" + validation + "',{mandt : cmr.MANDT}), [ \"" + label + "\" ], "
          + tabId + ");\n");
    }

    if (!StringUtils.isEmpty(validation)) {
      validation = validation.toUpperCase();
      switch (validation) {
      case "DIGIT":
        sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.DIGIT, [ \"" + label + "\" ], " + tabId + ");\n");
        break;
      case "NO_SPECIAL_CHAR":
        sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.NO_SPECIAL_CHAR, [ \"" + label + "\" ], " + tabId + ");\n");
        break;
      case "ALPHA":
        sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.ALPHA, [ \"" + label + "\" ], " + tabId + ");\n");
        break;
      case "ALPHA_NUM":
        sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.ALPHANUM, [ \"" + label + "\" ], " + tabId + ");\n");
        break;
      }
    }
  }

  private static void addGrayedOutConditions(StringBuilder sb, String fieldNm, boolean main, String fieldId, boolean retainHandlers) {
    sb.append("      FormManager.resetValidations('" + fieldNm + "');\n");
    if (!main && !retainHandlers) {
      sb.append("      PageManager.removeHandlers('" + fieldNm + "');\n");
    }
    sb.append("      FormManager.show('" + fieldId + "', '" + fieldNm + "');\n");
    sb.append("      FormManager.disable('" + fieldNm + "');\n");
  }

  private static void addReadOnlyConditions(StringBuilder sb, String fieldNm, boolean main, String fieldId, boolean retainHandlers) {
    sb.append("      FormManager.resetValidations('" + fieldNm + "');\n");
    if (!main && !retainHandlers) {
      sb.append("      PageManager.removeHandlers('" + fieldNm + "');\n");
    }
    sb.append("      FormManager.show('" + fieldId + "', '" + fieldNm + "');\n");
    sb.append("      FormManager.readOnly('" + fieldNm + "');\n");
  }

  private static void addHiddenConditions(StringBuilder sb, String fieldNm, boolean main, String fieldId, boolean retainHandlers) {
    sb.append("      FormManager.resetValidations('" + fieldNm + "');\n");
    if (!main && !retainHandlers) {
      sb.append("      PageManager.removeHandlers('" + fieldNm + "');\n");
    }
    sb.append("      FormManager.hide('" + fieldId + "', '" + fieldNm + "');\n");
  }

  private static void addOptionalConditions(HttpServletRequest request, FieldManager fieldMgr, StringBuilder sb, String fieldNm, String label,
      String tabId, boolean main, String fieldId, boolean retainHandlers) {
    sb.append("      FormManager.resetValidations('" + fieldNm + "');\n");
    if (!main && !retainHandlers) {
      sb.append("      PageManager.removeHandlers('" + fieldNm + "');\n");
    }
    sb.append("      FormManager.show('" + fieldId + "', '" + fieldNm + "');\n");
    if (TEXT.equals(fieldMgr.getType())) {
      int maxLength = getMaxLength(fieldMgr, request);
      if (maxLength > 0) {
        sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.MAXLENGTH, [ \"" + label + "\", " + maxLength + " ], " + tabId
            + ");\n");
      }
    }
    if (DROPDOWN.equals(fieldMgr.getType())) {
      sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.INVALID_VALUE, [ \"" + label + "\" ], " + tabId + ");\n");
    }
    if (BLUEPAGES.equals(fieldMgr.getType())) {
      sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.BLUEPAGES, [ \"" + label + "\" ], " + tabId + ");\n");
    }
    FieldInformation cntryInfo = getFieldInfoForCountry(fieldMgr, request);
    String validation = cntryInfo.getValidation();

    if (!StringUtils.isEmpty(validation) && validation.startsWith("DB")) {
      sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.DB('" + validation + "',{mandt : cmr.MANDT}), [ \"" + label + "\" ], "
          + tabId + ");\n");
    }

    if (!StringUtils.isEmpty(validation)) {
      validation = validation.toUpperCase();
      switch (validation) {
      case "DIGIT":
        sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.DIGIT, [ \"" + label + "\" ], " + tabId + ");\n");
        break;
      case "NO_SPECIAL_CHAR":
        sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.NO_SPECIAL_CHAR, [ \"" + label + "\" ], " + tabId + ");\n");
        break;
      case "ALPHA":
        sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.ALPHA, [ \"" + label + "\" ], " + tabId + ");\n");
        break;
      case "ALPHA_NUM":
        sb.append("      FormManager.addValidator('" + fieldNm + "', Validators.ALPHANUM, [ \"" + label + "\" ], " + tabId + ");\n");
        break;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static String generateTabScripts(HttpServletRequest request, String mainTabId, String switchFunction, String newEntryFunction,
      boolean newEntry) {
    List<TabInfo> tabInfoList = (List<TabInfo>) request.getAttribute("pageTabs");
    StringBuilder sb = new StringBuilder();
    sb.append("<script>\n");
    sb.append("  function openTabDetails(sectionId){\n");
    if (tabInfoList != null) {
      for (TabInfo tab : tabInfoList) {
        sb.append("    cmr.hideElement('" + tab.getSectionId() + "');\n");
      }
      sb.append("\n");
      sb.append("    cmr.showElement(sectionId);\n");
      sb.append("\n");
      int cnt = 0;
      for (TabInfo tab : tabInfoList) {
        if (tab.getGrids() != null && tab.getGrids().size() > 0) {
          sb.append(cnt > 0 ? "    } else if (sectionId == '" + tab.getSectionId() + "') {\n" : "    if (sectionId == '" + tab.getSectionId()
              + "') {\n");
          for (String gridId : tab.getGrids()) {
            sb.append("      CmrGrid.correct('" + gridId + "');\n");
          }
          cnt++;
        }
      }
      sb.append(cnt > 0 ? "    }\n" : "");
    }
    sb.append("  }\n");
    sb.append("    cmr.sectionMap = {\n");
    int cnt = 0;
    for (TabInfo tab : tabInfoList) {
      sb.append(cnt > 0 ? ",\n" : "");
      sb.append("      " + tab.getTabId() + " : '" + tab.getSectionId() + "'");
      cnt++;
    }
    sb.append("\n");
    sb.append("    };\n");
    sb.append("</script>");
    return sb.toString();
  }

  public static String getCurrentCountry(HttpServletRequest request) {
    return (String) request.getAttribute("pageMgrCMRCntry");
  }

  public static String getCurrentReqType(HttpServletRequest request) {
    return (String) request.getAttribute("pageMgrReqType");
  }

  private static int getMaxLength(FieldManager fieldMgr, HttpServletRequest request) {
    String cntry = PageManager.getCurrentCountry(request);
    List<FieldInformation> info = fieldMgr.getFieldInfo(cntry == null ? "all" : "c" + cntry);
    if (info == null || info.size() == 0) {
      info = fieldMgr.getFieldInfo("all");
    }
    try {
      if (info != null && info.size() > 0) {
        int maxLength = info.get(0).getMaxLength();
        if (maxLength == 0) {
          info = fieldMgr.getFieldInfo("all");
          maxLength = info.size() > 0 ? info.get(0).getMaxLength() : 0;
        }
        return maxLength > 0 ? maxLength : 0;
      }
    } catch (Exception e) {

    }
    return 0;
  }

  private static FieldInformation getFieldInfoForCountry(FieldManager fieldMgr, HttpServletRequest request) {
    String cntry = PageManager.getCurrentCountry(request);
    List<FieldInformation> info = fieldMgr.getFieldInfo(cntry == null ? "all" : "c" + cntry);
    if (info == null || info.size() == 0) {
      info = fieldMgr.getFieldInfo("all");
    }
    if (info != null && info.size() > 0) {
      return info.get(0);
    }
    return null;
  }

  public static boolean fromGeo(String geo, String countryCode) {
    List<String> countries = instance.geos.get(geo);
    return countries != null && countries.contains(countryCode);
  }

  public static boolean autoProcEnabled(String cmrIssuingCntry, String reqType) {
    for (String code : instance.autoProcCountries) {
      String reqTypes = null;
      if (code != null && code.startsWith(cmrIssuingCntry)) {
        reqTypes = code.substring(code.indexOf("-") + 1);
        if (reqTypes != null && reqTypes.contains("-")) {
          reqTypes = reqTypes.substring(0, reqTypes.indexOf("-"));
        }
        return reqTypes != null && reqType != null && reqTypes.contains(reqType) && !reqTypes.contains(reqType + "0");
      }
    }
    return false;
  }

  public static String getProcessingType(String cmrIssuingCntry, String reqType) {
    boolean autoProc = autoProcEnabled(cmrIssuingCntry, reqType);
    if (!autoProc) {
      return null;
    }
    String[] parts = null;
    for (String code : instance.autoProcCountries) {
      if (code.startsWith(cmrIssuingCntry)) {
        parts = code.split("[\\-]");
        if (parts.length == 3) {
          return parts[2];
        }
      }
    }
    return null;
  }

  public static boolean laReactivateEnabled(String cmrIssuingCntry, String reqType) {
    for (String code : instance.laReactivateCountries) {
      String suppReqTyp = null;
      if (code.startsWith(cmrIssuingCntry)) {
        suppReqTyp = code.substring(code.indexOf("-") + 1);
        if (suppReqTyp.contains("1") && "U".equalsIgnoreCase(reqType))
          return true;
      }
    }
    return false;
  }

  public static String getDefaultLandedCountry(String cmrIssuingCntry) {
    return instance.defaultLandedCountries.get(cmrIssuingCntry);
  }

  public static PageManager get() {
    return instance;
  }

  public List<String> getCountries() {
    return countries;
  }

  public void setCountries(List<String> countries) {
    this.countries = countries;
  }

  private void setGeos(Map<String, List<String>> geos) {
    this.geos = geos;
  }

  private void setDefaultLandedCountries(Map<String, String> defaultLandedCountries) {
    this.defaultLandedCountries = defaultLandedCountries;
  }

  private void setAutoProcCountries(List<String> autoProcCountries) {
    this.autoProcCountries = autoProcCountries;
  }

  private void setLaReactivateCountries(List<String> laReactivateCountries) {
    this.laReactivateCountries = laReactivateCountries;
  }

}
