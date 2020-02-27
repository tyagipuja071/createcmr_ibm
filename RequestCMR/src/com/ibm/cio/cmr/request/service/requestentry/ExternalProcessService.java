/**
 * 
 */
package com.ibm.cio.cmr.request.service.requestentry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.FieldInfo;
import com.ibm.cio.cmr.request.model.DropdownItemModel;
import com.ibm.cio.cmr.request.model.DropdownModel;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.service.DropDownService;
import com.ibm.cio.cmr.request.ui.template.Template;
import com.ibm.cio.cmr.request.ui.template.TemplateManager;
import com.ibm.cio.cmr.request.ui.template.TemplatedField;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.external.FieldContainer;
import com.ibm.cio.cmr.request.util.external.FieldDefinition;
import com.ibm.cio.cmr.request.util.external.LovItem;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;

/**
 * Loads the field configurations, default values, scenario values for a given
 * country and scenario
 * 
 * @author JeffZAMORA
 * 
 */
@Component
public class ExternalProcessService extends BaseSimpleService<FieldContainer> {

  private static Map<String, FieldContainer> cache = new HashMap<String, FieldContainer>();

  private static final Logger LOG = Logger.getLogger(ExternalProcessService.class);

  private static final List<String> INTERNAL_IDENTIFIERS = Arrays.asList("%%", "%", "$", "@");

  @Override
  protected FieldContainer doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    String cmrIssuingCntry = (String) params.getParam("cmrIssuingCntry");
    String reqType = (String) params.getParam("reqType");
    String custGrp = (String) params.getParam("custGrp");
    String custSubGrp = (String) params.getParam("custSubGrp");
    String stateProv = (String) params.getParam("stateProv");
    String landCntry = (String) params.getParam("landCntry");
    boolean nocache = (Boolean) params.getParam("nocache");

    String cacheKey = cmrIssuingCntry + "/" + reqType;
    if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
      cacheKey += "/" + custGrp + "/" + custSubGrp;
      if (StringUtils.isNotEmpty(landCntry)) {
        cacheKey += "/" + landCntry;
      }
    }
    LOG.info("Getting field definitions for country " + cmrIssuingCntry + " request type: " + reqType + " scenario: " + custGrp + "/" + custSubGrp);

    FieldContainer definitions = cache.get(cacheKey);
    if (definitions != null && !nocache) {
      LOG.debug("Definition found from cache with key " + cacheKey);
      return definitions;
    }
    definitions = new FieldContainer();
    FieldDefinition definition = null;

    List<FieldInfo> fields = getFields(entityManager, cmrIssuingCntry);
    GEOHandler handler = RequestUtils.getGEOHandler(cmrIssuingCntry);

    Map<String, String> fieldIdMap = new HashMap<String, String>();
    if (handler != null && handler.getUIFieldIdMap() != null && handler.getUIFieldIdMap().size() > 0) {
      fieldIdMap = handler.getUIFieldIdMap();
    }

    String fieldId = null;
    String fieldName = null;
    String required = null;
    String dependsOn = null;
    String dependsOnSetting = null;
    LOG.debug("Loading field definitions..");

    DropDownService dropdownService = new DropDownService();
    for (FieldInfo field : fields) {
      fieldId = field.getId().getFieldId();
      fieldName = fieldIdMap.get(fieldId);
      required = field.getRequired();
      dependsOn = field.getDependsOn();
      dependsOnSetting = field.getDependsSetting();
      if (!definitions.contains(fieldId)) {
        // seq 1
        definition = new FieldDefinition();

        definition.setFieldId(fieldId);
        if (!StringUtils.isEmpty(fieldName)) {
          definition.setFieldName(fieldName);
        } else {
          definition.setFieldName(fieldId.replaceAll("#", ""));
        }
        // always optional for non-R
        definition.setRequiredInd("R".equals(required) ? "R" : "O");

        if ("D".equals(field.getType()) || "R".equals(field.getType())) {

          // for dropdowns, get the values via service
          ParamContainer dropdownParams = new ParamContainer();
          dropdownParams.addParam("cmrIssuingCntry", cmrIssuingCntry);
          dropdownParams.addParam("custSubGrp", custSubGrp);
          dropdownParams.addParam("custGrp", custGrp);
          dropdownParams.addParam("stateProv", stateProv);
          dropdownParams.addParam("landCntry", landCntry);
          dropdownParams.addParam("fieldId", fieldId.replaceAll("#", ""));
          if (field.getChoice() != null) {
            switch (field.getChoice()) {
            case "L":
              dropdownParams.addParam("queryId", "LOV");
              break;
            case "B":
              dropdownParams.addParam("queryId", "BDS");
              break;
            }
            List<DropdownItemModel> dropdownValues = getDropDownValues(dropdownService, entityManager, request, dropdownParams);
            if (dropdownValues != null) {
              LovItem lov = null;
              for (DropdownItemModel item : dropdownValues) {
                definition.getValues().add(item.getId());
                lov = new LovItem();
                lov.setId(item.getId());
                lov.setLabel(item.getName());
                definition.getLov().add(lov);
              }
            }
          }

        } else if ("T".equals(field.getType()) || "M".equals(field.getType())) {
          definition.setMaxLength(field.getMaxLength());
        }
        definitions.add(definition);
      } else if (field.getId().getSeqNo() == 2) {
        // only handle seq 2
        definition = definitions.getFieldDefinition(fieldId);
        if (definition != null) {
          // special handling for ##RequestType field id
          if ("##RequestType".equals(dependsOn)) {
            if (CmrConstants.REQ_TYPE_UPDATE.equals(dependsOnSetting) && CmrConstants.REQ_TYPE_UPDATE.equals(reqType)) {
              // an update request, and depends setting is update
              // definition.setRequiredInd("Y".equals(field.getCondReqInd()) ?
              // "R" : definition.getRequiredInd());
              // George20180924 set 2nd records of field-sensitiveFlag' REQUIRED
              // to "O" based on the setting.
              definition.setRequiredInd("Y".equals(field.getCondReqInd()) ? "R" : "O");
            }
            if (CmrConstants.REQ_TYPE_CREATE.equals(dependsOnSetting) && CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
              // an update request, and depends setting is update
              definition.setRequiredInd("Y".equals(field.getCondReqInd()) ? "R" : definition.getRequiredInd());
            }
          } else {
            // build dependency requirement
            if ("Y".equals(field.getCondReqInd())) {
              definition.setRequiredInd(dependsOn + "=" + dependsOnSetting);
            }
          }
        } else {
          LOG.warn("Sequence 2 found for Field ID " + fieldId + " but was not found on the container.");
        }

        cleanDefinition(definition);
        definitions.add(definition);
      }
    }

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqType)) {
      if (handler != null) {
        LOG.debug("Loading default create values..");
        loadCreateDefaults(entityManager, handler, definitions);
      }
      LOG.debug("Loading scenario values...");
      loadScenarioValues(entityManager, request, cmrIssuingCntry, custGrp, custSubGrp, definitions);
    }

    cache.put(cacheKey, definitions);
    LOG.debug(definitions.size() + " fields initialized. Added to cache.");
    return definitions;
  }

  /**
   * Gets the base {@link FieldInfo} filtered by the {@link GEOHandler} list of
   * UI field ids
   * 
   * @param entityManager
   * @param cmrIssuingCntry
   * @return
   */
  private List<FieldInfo> getFields(EntityManager entityManager, String cmrIssuingCntry) {
    GEOHandler handler = RequestUtils.getGEOHandler(cmrIssuingCntry);

    String sql = ExternalizedQuery.getSql("EXTERNAL.GET_FIELDS");

    if (handler != null && handler.getUIFieldIdMap() != null && handler.getUIFieldIdMap().size() > 0) {
      StringBuilder sb = new StringBuilder();
      Map<String, String> fields = handler.getUIFieldIdMap();
      for (String id : fields.keySet()) {
        sb.append(sb.length() > 0 ? "," : "");
        sb.append("'" + id + "'");
      }
      sql += " and info.FIELD_ID in (" + sb.toString() + ") ";
    }

    sql += ExternalizedQuery.getSql("EXTERNAL.GET_FIELDS.ORDER");

    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", cmrIssuingCntry);
    query.setForReadOnly(true);
    return query.getResults(FieldInfo.class);
  }

  /**
   * Loads the scenario values for the given country and scenario
   * 
   * @param entityManager
   * @param request
   * @param cmrIssuingCntry
   * @param custGrp
   * @param custSubGrp
   * @param definitions
   */
  private void loadScenarioValues(EntityManager entityManager, HttpServletRequest request, String cmrIssuingCntry, String custGrp, String custSubGrp,
      FieldContainer definitions) {
    try {
      TemplateParamWrapper wrapper = new TemplateParamWrapper(request, custSubGrp);
      Template template = TemplateManager.getTemplate(cmrIssuingCntry, wrapper);
      if (template != null) {
        String fieldId = null;
        FieldDefinition definition = null;
        for (TemplatedField field : template.getFields()) {
          fieldId = "##" + field.getFieldId();
          definition = definitions.getFieldDefinition(fieldId);
          if (definition == null) {
            definition = definitions.locateByFieldName(field.getFieldName());
          }
          if (definition != null) {
            definition.setRequiredInd("R".equals(field.getRequiredInd()) || "Y".equals(field.getRequiredInd()) ? "R"
                : (field.isAddressField() ? definition.getRequiredInd() : "O"));
            if (field.getValues() != null && !field.getValues().isEmpty()) {
              if (field.getValues().size() == 1 && "*".equals(field.getValues().get(0))) {
                definition.setRequiredInd("R");
              } else {
                if (field.getValues().size() == 1 && !INTERNAL_IDENTIFIERS.contains(field.getValues().get(0))) {
                  if ("Y".equals(field.getLockInd()) || "R".equals(field.getLockInd())) {
                    definition.setRequiredInd("R");
                    definition.getValues().clear();
                    definition.getValues().addAll(field.getValues());
                  }
                } else if (field.getValues().size() > 1) {
                  definition.getValues().clear();
                  definition.getValues().addAll(field.getValues());
                }
                if (field.getValues().size() > 0) {
                  definition.setDefaultValue(field.getValues().get(0));
                }
              }
            }
            if (field.getValueMap() != null && field.getValueMap().size() > 0) {
              definition.getValueMap().putAll(field.getValueMap());
            }
            cleanDefinition(definition);
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Cannot load template.", e);
    }
  }

  /**
   * Does a dummy create and loads any value on create
   * 
   * @param entityManager
   * @param definitions
   */
  private void loadCreateDefaults(EntityManager entityManager, GEOHandler handler, FieldContainer definitions) {
    Data dummy = new Data();
    try {
      // call handler
      handler.setDataDefaultsOnCreate(dummy, entityManager);

      // inspect fields
      Object value = null;
      FieldDefinition definition = null;
      for (Field field : Data.class.getDeclaredFields()) {
        field.setAccessible(true);
        if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isAbstract(field.getModifiers())
            && (!"PCSTATEMANAGER".equals(field.getName().toUpperCase()) && !"PCDETACHEDSTATE".equals(field.getName().toUpperCase()))) {
          value = field.get(dummy);
          if (value != null) {
            LOG.trace("Default value " + value + " found for " + field.getName());
            definition = definitions.locateByFieldName(field.getName());
            if (definition != null) {
              definition.setDefaultValue(value.toString());
            }
          }
        }

      }
    } catch (Exception e) {
      LOG.warn("Cannot call default create method.", e);
    }
  }

  /**
   * Wrapper for the request to attach custSubGrp as a parameter
   * 
   * @author JeffZAMORA
   * 
   */
  public class TemplateParamWrapper extends HttpServletRequestWrapper {
    private String custSubGrp;

    public TemplateParamWrapper(HttpServletRequest request, String custSubGrp) {
      super(request);
      this.custSubGrp = custSubGrp;
    }

    @Override
    public String getParameter(String name) {
      if ("custSubGrp".equals(name)) {
        return this.custSubGrp;
      }
      return super.getParameter(name);
    }

  }

  /**
   * Gets the allowed dropdown values
   * 
   * @param entityManager
   * @param params
   * @return
   * @throws CmrException
   */
  private List<DropdownItemModel> getDropDownValues(DropDownService service, EntityManager entityManager, HttpServletRequest request,
      ParamContainer params) {
    try {
      DropdownModel model = service.getValuesStandAlone(entityManager, request, params);
      if (model != null && model.getItems() != null && !model.getItems().isEmpty()) {
        return model.getItems();
        // for (DropdownItemModel item : model.getItems()) {
        // if (!StringUtils.isEmpty(item.getId()) &&
        // !values.contains(item.getId())) {
        // values.add(item.getId());
        // }
        // }
      }
    } catch (Exception e) {
      LOG.error("Cannot load dropdown values .", e);
    }
    return null;
  }

  /**
   * Cleans the definitions
   * 
   * @param definition
   */
  private void cleanDefinition(FieldDefinition definition) {
    if (definition.getDefaultValue() != null && INTERNAL_IDENTIFIERS.contains(definition.getDefaultValue())) {
      definition.setDefaultValue(null);
    }
    if (definition.getValues() != null && definition.getValues().size() == 1 && INTERNAL_IDENTIFIERS.contains(definition.getValues().get(0))) {
      definition.setValues(new ArrayList<String>());
    }
  }

  /**
   * Clears the cache value
   */
  public static void refresh() {
    cache.clear();
  }
}
