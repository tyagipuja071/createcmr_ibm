/**
 * 
 */
package com.ibm.cio.cmr.request.service.automation;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.automation.AutomationElement;
import com.ibm.cio.cmr.request.automation.AutomationElementRegistry;
import com.ibm.cio.cmr.request.automation.util.AutomationElementContainer;
import com.ibm.cio.cmr.request.entity.AutoConfigDefn;
import com.ibm.cio.cmr.request.entity.AutoConfigDefnPK;
import com.ibm.cio.cmr.request.entity.AutoEngineConfig;
import com.ibm.cio.cmr.request.entity.AutoEngineConfigPK;
import com.ibm.cio.cmr.request.entity.AutoEngineMapping;
import com.ibm.cio.cmr.request.entity.AutoEngineMappingPK;
import com.ibm.cio.cmr.request.entity.ScenarioExceptions;
import com.ibm.cio.cmr.request.entity.ScenarioExceptionsPK;
import com.ibm.cio.cmr.request.entity.SuppCntry;
import com.ibm.cio.cmr.request.entity.SuppCntryPK;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.automation.AutoConfigCntryModel;
import com.ibm.cio.cmr.request.model.automation.AutoConfigElemModel;
import com.ibm.cio.cmr.request.model.automation.AutoConfigMapModel;
import com.ibm.cio.cmr.request.model.automation.AutoConfigModel;
import com.ibm.cio.cmr.request.model.automation.AutoElemModel;
import com.ibm.cio.cmr.request.model.automation.AutoExceptionEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author JeffZAMORA
 * 
 */
@Component
public class AutoConfigService extends BaseSimpleService<Map<String, Object>> {

  private static final Logger LOG = Logger.getLogger(AutoConfigService.class);

  public static final String ACTION_GET_CONFIGS = "GET_CONFIGS";
  public static final String ACTION_GET_ELEMENTS = "GET_ELEMENTS";
  public static final String ACTION_SAVE_CONFIG_DEFN = "SAVE_CONFIG_DEFN";
  public static final String ACTION_SAVE_CONFIG_ELEMENTS = "SAVE_CONFIG_ELEMENTS";
  public static final String ACTION_MAP_COUNTRIES = "MAP_COUNTRIES";
  public static final String ACTION_SAVE_COUNTRIES = "SAVE_COUNTRIES";
  public static final String ACTION_DELETE_CONFIG = "DELETE_CONFIG";

  public static final String ACTION_SAVE_EXCEPTION = "SAVE_EXCEPTION";

  public static final String OUT_CONFIG_LIST = "CONFIG_LIST";
  public static final String OUT_ELEMENT_LIST = "ELEMENT_LIST";
  public static final String OUT_PROCESS_RESULT = "PROCESS_RESULT";

  @Override
  protected Map<String, Object> doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    Map<String, Object> outMap = new HashMap<String, Object>();

    String action = (String) params.getParam("action");
    if (StringUtils.isBlank(action)) {
      return outMap;
    }
    switch (action) {
    case ACTION_GET_CONFIGS:
      getConfigurations(entityManager, outMap);
      break;
    case ACTION_GET_ELEMENTS:
      getElements(entityManager, outMap);
      break;
    case ACTION_SAVE_CONFIG_DEFN:
      saveConfigDefinition(entityManager, request, outMap);
      break;
    case ACTION_SAVE_CONFIG_ELEMENTS:
      AutoConfigElemModel model = (AutoConfigElemModel) params.getParam("model");
      saveConfigElements(entityManager, model, request, outMap);
      break;
    case ACTION_MAP_COUNTRIES:
      AutoConfigMapModel mapModel = (AutoConfigMapModel) params.getParam("model");
      saveCountryMapping(entityManager, mapModel, request, outMap);
      break;
    case ACTION_SAVE_COUNTRIES:
      AutoConfigCntryModel cntryModel = (AutoConfigCntryModel) params.getParam("model");
      saveCountryConfig(entityManager, cntryModel, request, outMap);
      break;
    case ACTION_SAVE_EXCEPTION:
      AutoExceptionEntryModel excModel = (AutoExceptionEntryModel) params.getParam("model");
      saveException(entityManager, excModel, request, outMap);
      break;
    case ACTION_DELETE_CONFIG:
      deleteConfiguration(entityManager, request, outMap);
      break;
    }
    return outMap;
  }

  /**
   * Gets the list of current {@link AutoConfigDefn} records
   * 
   * @param entityManager
   * @param outMap
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   * @throws IllegalAccessException
   */
  private void getConfigurations(EntityManager entityManager, Map<String, Object> outMap)
      throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    String sql = ExternalizedQuery.getSql("AUTOMATION.CONFIG.GET_CONFIGS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setForReadOnly(true);
    List<AutoConfigModel> returnList = new ArrayList<AutoConfigModel>();
    AutoConfigModel model = null;

    List<AutoConfigDefn> configs = query.getResults(AutoConfigDefn.class);
    for (AutoConfigDefn def : configs) {
      model = new AutoConfigModel();
      PropertyUtils.copyProperties(model, def);
      model.setConfigId(def.getId().getConfigId());
      returnList.add(model);
    }

    outMap.put(OUT_CONFIG_LIST, returnList);
  }

  /**
   * Gets the list of defined {@link AutomationElement} items in the
   * {@link AutomationElementRegistry}
   * 
   * @param entityManager
   * @param outMap
   * @throws Exception
   */
  private void getElements(EntityManager entityManager, Map<String, Object> outMap) throws Exception {
    // List<AutomationElementContainer> elems =
    // AutomationElementContainer.getDefinedElements();
    List<AutomationElementContainer> elems = new ArrayList<AutomationElementContainer>();
    String sql = ExternalizedQuery.getSql("AUTO_ENGINE.ELEMENTS");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    List<Object[]> results = query.getResults();
    AutomationElementContainer elem = null;
    if (results != null) {
      for (Object[] result : results) {
        // 0 - Process Code
        // 1 - Process Desc
        // 2 - Process Type
        // 3 - No import indc
        elem = new AutomationElementContainer();
        elem.setProcessCd((String) result[0]);
        elem.setProcessDesc((String) result[1]);
        elem.setProcessType((String) result[2]);
        elem.setNonImportable("Y".equals(result[3]));
        elems.add(elem);
      }
    }
    outMap.put(OUT_ELEMENT_LIST, elems);
  }

  /**
   * Saves the updated definition for the configuration
   * 
   * @param entityManager
   * @param request
   * @param outMap
   */
  private void saveConfigDefinition(EntityManager entityManager, HttpServletRequest request, Map<String, Object> outMap) {
    String configId = request.getParameter("configId");
    String configDefn = request.getParameter("configDefn");
    String shortDesc = request.getParameter("shortDesc");
    String copyFrom = request.getParameter("copyFrom");
    boolean updateMode = "Y".equals(request.getParameter("updateMode"));
    ProcessResultModel result = new ProcessResultModel();
    if (StringUtils.isBlank(configId) || StringUtils.isBlank(configDefn)) {
      result.setSuccess(false);
      result.setMessage("Configuration ID and Definition are required");
    } else {
      try {
        AppUser user = AppUser.getUser(request);
        if (!updateMode) {
          LOG.debug("Creating configuration " + configId);
          AutoConfigDefnPK pk = new AutoConfigDefnPK();
          pk.setConfigId(configId);
          AutoConfigDefn definition = new AutoConfigDefn();
          definition.setId(pk);
          definition.setConfigDefn(configDefn);
          definition.setShortDesc(shortDesc);
          definition.setLastUpdtBy(user.getIntranetId());
          definition.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
          definition.setCreateBy(user.getIntranetId());
          definition.setCreateTs(SystemUtil.getCurrentTimestamp());
          entityManager.persist(definition);

          if (!StringUtils.isBlank(copyFrom)) {
            LOG.debug("Assigning automation elements to new configuration from " + copyFrom);
            String sql = ExternalizedQuery.getSql("QUERY.GET_AUTO_ELEMENTS");
            PreparedQuery query = new PreparedQuery(entityManager, sql);
            query.setParameter("ID", copyFrom);
            query.setForReadOnly(true);
            List<AutoEngineConfig> configs = query.getResults(AutoEngineConfig.class);
            if (configs != null) {
              for (AutoEngineConfig config : configs) {
                AutoEngineConfigPK newPk = new AutoEngineConfigPK();
                newPk.setConfigId(configId);
                newPk.setElementId(config.getId().getElementId());
                AutoEngineConfig newConfig = new AutoEngineConfig();
                PropertyUtils.copyProperties(newConfig, config);
                newConfig.setId(newPk);
                newConfig.setLastUpdtBy(user.getIntranetId());
                newConfig.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
                newConfig.setCreateBy(user.getIntranetId());
                newConfig.setCreateTs(SystemUtil.getCurrentTimestamp());

                LOG.debug("Adding element " + newConfig.getProcessCd() + " under " + configId);
                entityManager.persist(newConfig);
              }
            }
          }
          entityManager.flush();
          result.setSuccess(true);
        } else {
          String sql = ExternalizedQuery.getSql("RECORD.GET_AUTO_ENGINE_CONFIG");
          PreparedQuery query = new PreparedQuery(entityManager, sql);
          query.setParameter("ID", configId);
          AutoConfigDefn definition = query.getSingleResult(AutoConfigDefn.class);
          if (definition == null) {
            result.setSuccess(false);
            result.setMessage("Configuration for ID " + configId + " not found");
          } else {
            LOG.debug("Updating configuration " + configId);
            definition.setConfigDefn(configDefn);
            definition.setShortDesc(shortDesc);
            definition.setLastUpdtBy(user.getIntranetId());
            definition.setLastUpdtTs(SystemUtil.getCurrentTimestamp());

            entityManager.merge(definition);
            entityManager.flush();
            result.setSuccess(true);
          }
        }
      } catch (Exception e) {
        LOG.error("Error in saving configuration", e);
        result.setSuccess(false);
        result.setMessage("An unexpected error occurred while saving. Please try again.");
      }
    }
    outMap.put(OUT_PROCESS_RESULT, result);
  }

  private void deleteConfiguration(EntityManager entityManager, HttpServletRequest request, Map<String, Object> outMap) {
    String configId = request.getParameter("configId");
    ProcessResultModel result = new ProcessResultModel();
    if (StringUtils.isBlank(configId)) {
      result.setSuccess(false);
      result.setMessage("Configuration ID not specified.");
    } else {
      LOG.debug("Checking mapped countries..");
      String sql = "select * from CREQCMR.AUTO_ENGINE_MAPPING where CONFIG_ID = :CONFIG_ID";
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setForReadOnly(true);
      query.setParameter("CONFIG_ID", configId);
      if (query.exists()) {
        result.setSuccess(false);
        result.setMessage(
            "Countries are currently mapped to the engine configuration. Remove mapped countries first before deleting the configuration.");
      } else {
        LOG.debug("Removing element config for " + configId);
        sql = "delete from CREQCMR.AUTO_ENGINE_CONFIG where CONFIG_ID = :CONFIG_ID";
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("CONFIG_ID", configId);
        query.executeSql();

        LOG.debug("Removing country mapping for " + configId);
        sql = "delete from CREQCMR.AUTO_ENGINE_MAPPING where CONFIG_ID = :CONFIG_ID";
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("CONFIG_ID", configId);
        query.executeSql();

        LOG.debug("Removing definition for " + configId);
        sql = "delete from CREQCMR.AUTO_CONFIG_DEFN where CONFIG_ID = :CONFIG_ID";
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("CONFIG_ID", configId);
        query.executeSql();

        entityManager.flush();

        result.setSuccess(true);
      }
    }

    outMap.put(OUT_PROCESS_RESULT, result);

  }

  /**
   * Saves all the elements defined under the configuration
   * 
   * @param entityManager
   * @param request
   * @param outMap
   */
  private void saveConfigElements(EntityManager entityManager, AutoConfigElemModel model, HttpServletRequest request, Map<String, Object> outMap) {
    AppUser user = AppUser.getUser(request);
    ProcessResultModel result = new ProcessResultModel();

    try {
      // delete first ALL elements
      LOG.debug("Deleting all elements under config " + model.getConfigId());
      String sql = "delete from CREQCMR.AUTO_ENGINE_CONFIG where CONFIG_ID = :CONFIG_ID";
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("CONFIG_ID", model.getConfigId());
      query.executeSql();
      entityManager.flush();

      // recreate all records based on configuration
      int orderAndId = 1;
      for (AutoElemModel elem : model.getElements()) {
        AutoEngineConfigPK pk = new AutoEngineConfigPK();
        pk.setConfigId(model.getConfigId());
        pk.setElementId(orderAndId);

        LOG.trace("Creating element config for " + elem.getProcessCd() + " under " + model.getConfigId() + " with Order " + orderAndId);
        AutoEngineConfig config = new AutoEngineConfig();
        config.setId(pk);
        config.setProcessCd(elem.getProcessCd());
        config.setExecOrd(orderAndId);
        config.setRequestTyp(elem.getRequestTyp());
        config.setActionOnError(elem.getActionOnError());
        config.setOverrideDataIndc(elem.isOverrideDataIndc() ? "Y" : null);
        config.setStopOnErrorIndc(elem.isStopOnErrorIndc() ? "Y" : null);
        config.setStatus(elem.isStatus() ? "1" : "0");

        config.setCreateBy(user.getIntranetId());
        config.setCreateTs(SystemUtil.getCurrentTimestamp());
        config.setLastUpdtBy(user.getIntranetId());
        config.setLastUpdtTs(SystemUtil.getCurrentTimestamp());

        entityManager.persist(config);

        orderAndId++;
      }
      entityManager.flush();

      result.setSuccess(true);
    } catch (Exception e) {
      LOG.error("Error in saving element configurations.", e);
      result.setSuccess(false);
      result.setMessage("An error occurred while saving the configuration.");
    }

    outMap.put(OUT_PROCESS_RESULT, result);
  }

  /**
   * Saves country mappings
   * 
   * @param entityManager
   * @param model
   * @param request
   * @param outMap
   */
  private void saveCountryMapping(EntityManager entityManager, AutoConfigMapModel model, HttpServletRequest request, Map<String, Object> outMap) {
    AppUser user = AppUser.getUser(request);
    ProcessResultModel result = new ProcessResultModel();

    LOG.debug("Saving country mapping for " + model.getConfigId());
    try {

      boolean delete = "D".equals(model.getDirective());
      boolean update = "U".equals(model.getDirective());
      for (String country : model.getCountries()) {
        AutoEngineMapping mapping = null;
        AutoEngineMappingPK pk = new AutoEngineMappingPK();
        pk.setCmrIssuingCntry(country);
        pk.setConfigId(model.getConfigId());
        if (delete) {
          mapping = entityManager.find(AutoEngineMapping.class, pk);
          if (mapping != null) {
            LOG.debug("Removing country " + country + " from config " + model.getConfigId());
            AutoEngineMapping merged = entityManager.merge(mapping);
            if (merged != null) {
              entityManager.remove(merged);
            }
          }
          if (country.length() == 3) {
            SuppCntryPK suppPk = new SuppCntryPK();
            suppPk.setCntryCd(country);
            SuppCntry cntry = entityManager.find(SuppCntry.class, suppPk);
            if (cntry != null) {
              LOG.debug("Setting automation to DISABLED for " + country);
              cntry.setAutoEngineIndc("N");
              entityManager.merge(cntry);
            }
          }
        } else if (update) {
          mapping = entityManager.find(AutoEngineMapping.class, pk);
          if (mapping != null) {
            LOG.debug("Updating country " + country + " from config " + model.getConfigId());
            mapping.setProcessOnCompletion(model.getProcessOnCompletion());
            entityManager.merge(mapping);
          }
        } else {
          mapping = new AutoEngineMapping();
          mapping.setId(pk);
          mapping.setProcessOnCompletion(model.getProcessOnCompletion());
          mapping.setStatus("1");
          mapping.setCreateBy(user.getIntranetId());
          mapping.setCreateTs(SystemUtil.getCurrentTimestamp());
          mapping.setLastUpdtBy(user.getIntranetId());
          mapping.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
          LOG.debug("Adding country " + country + " to config " + model.getConfigId());
          entityManager.persist(mapping);
        }
      }
      entityManager.flush();
      result.setSuccess(true);
    } catch (Exception e) {
      LOG.error("Error in saving element configurations.", e);
      result.setSuccess(false);
      result.setMessage("An error occurred while saving the configuration.");
    }
    outMap.put(OUT_PROCESS_RESULT, result);

  }

  /**
   * UPdates country configurations
   * 
   * @param entityManager
   * @param model
   * @param request
   * @param outMap
   */
  private void saveCountryConfig(EntityManager entityManager, AutoConfigCntryModel model, HttpServletRequest request, Map<String, Object> outMap) {
    ProcessResultModel result = new ProcessResultModel();
    try {
      if (model.isRemoveCountry()) {
        for (String country : model.getCountries()) {
          LOG.debug("Removing country " + country + " from automation engine mappings..");
          String sql = "delete from CREQCMR.AUTO_ENGINE_MAPPING where CMR_ISSUING_CNTRY = :CNTRY";
          PreparedQuery query = new PreparedQuery(entityManager, sql);
          query.setParameter("CNTRY", country);
          query.executeSql();

          if (country.length() == 3) {
            SuppCntryPK suppPk = new SuppCntryPK();
            suppPk.setCntryCd(country);
            SuppCntry cntry = entityManager.find(SuppCntry.class, suppPk);
            if (cntry != null) {
              LOG.debug("Setting automation to DISABLED for " + country);
              cntry.setAutoEngineIndc("N");
              entityManager.merge(cntry);
            }
          }

          entityManager.flush();
        }
      } else {
        AppUser user = AppUser.getUser(request);
        if (model.isSaveEnablement()) {
          for (String country : model.getCountries()) {
            SuppCntryPK suppPk = new SuppCntryPK();
            suppPk.setCntryCd(country.length() > 3 ? country.substring(0, 3) : country);
            SuppCntry cntry = entityManager.find(SuppCntry.class, suppPk);
            if (cntry != null) {
              LOG.debug("Saving auto engine enablement as " + model.getAutoEngineIndc() + " for " + country);
              cntry.setAutoEngineIndc(StringUtils.isBlank(model.getAutoEngineIndc()) ? null : model.getAutoEngineIndc());
              entityManager.merge(cntry);
            }
          }
          entityManager.flush();
        }
        if (model.isSaveProcessOnCompletion()) {
          for (String country : model.getCountries()) {
            String sql = "select * from CREQCMR.AUTO_ENGINE_MAPPING where CMR_ISSUING_CNTRY = :CNTRY";
            PreparedQuery query = new PreparedQuery(entityManager, sql);
            query.setParameter("CNTRY", country);
            query.setForReadOnly(true);
            AutoEngineMapping mapping = query.getSingleResult(AutoEngineMapping.class);
            if (mapping != null) {
              LOG.debug("Saving auto engine process on completion as " + model.getProcessOnCompletion() + " for " + country);
              mapping.setProcessOnCompletion(model.getProcessOnCompletion());
              mapping.setLastUpdtBy(user.getIntranetId());
              mapping.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
              entityManager.merge(mapping);
            }
          }
          entityManager.flush();
        }
        if (model.isSaveConfig()) {
          for (String country : model.getCountries()) {

            AutoEngineMappingPK mapPk = new AutoEngineMappingPK();
            mapPk.setCmrIssuingCntry(country);
            mapPk.setConfigId(model.getConfigId());
            AutoEngineMapping mapping = entityManager.find(AutoEngineMapping.class, mapPk);
            if (mapping == null) {
              // the mapping is the same, no need
              // delete + insert

              LOG.debug("Removing all configurations for country " + country);
              String sql = "delete from CREQCMR.AUTO_ENGINE_MAPPING where CMR_ISSUING_CNTRY = :CNTRY";
              PreparedQuery query = new PreparedQuery(entityManager, sql);
              query.setParameter("CNTRY", country);
              query.executeSql();
              entityManager.flush();

              mapPk = new AutoEngineMappingPK();
              mapPk.setCmrIssuingCntry(country);
              mapPk.setConfigId(model.getConfigId());
              mapping = new AutoEngineMapping();
              mapping.setId(mapPk);
              mapping.setProcessOnCompletion(model.getProcessOnCompletion());
              mapping.setStatus("1");
              mapping.setCreateBy(user.getIntranetId());
              mapping.setCreateTs(SystemUtil.getCurrentTimestamp());
              mapping.setLastUpdtBy(user.getIntranetId());
              mapping.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
              LOG.debug("Adding mapping for country " + country + " ang engine ID " + model.getConfigId());
              entityManager.persist(mapping);
            } else {
              LOG.debug("Configuration ID is the same as current. No need to update");
            }

          }
          entityManager.flush();
        }
      }
      result.setSuccess(true);
    } catch (Exception e) {
      LOG.error("Error in saving element configurations.", e);
      result.setSuccess(false);
      result.setMessage("An error occurred while saving the configuration.");
    }
    outMap.put(OUT_PROCESS_RESULT, result);
  }

  private void saveException(EntityManager entityManager, AutoExceptionEntryModel model, HttpServletRequest request, Map<String, Object> outMap) {
    AppUser user = AppUser.getUser(request);
    ProcessResultModel result = new ProcessResultModel();

    LOG.debug("Saving country mapping for " + model.getCmrIssuingCntry());
    try {
      ScenarioExceptions exc = null;
      ScenarioExceptionsPK pk = null;
      switch (model.getStatus()) {
      case "D":
        // delete
        pk = new ScenarioExceptionsPK();
        pk.setCmrIssuingCntry(model.getCmrIssuingCntry());
        pk.setCustTyp(model.getCustTyp());
        pk.setCustSubTyp(model.getCustSubTyp());
        pk.setSubregionCd(model.getRegion() != null ? model.getRegion() : model.getCmrIssuingCntry());
        exc = entityManager.find(ScenarioExceptions.class, pk);
        if (exc != null) {
          LOG.debug("Removing exception for " + model.getCmrIssuingCntry() + " - " + model.getCustTyp() + "/" + model.getCustSubTyp());
          ScenarioExceptions merged = entityManager.merge(exc);
          if (merged != null) {
            entityManager.remove(merged);
          }
        }
        break;
      case "M":
        pk = new ScenarioExceptionsPK();
        pk.setCmrIssuingCntry(model.getCmrIssuingCntry());
        pk.setCustTyp(model.getCustTyp());
        pk.setCustSubTyp(model.getCustSubTyp());
        pk.setSubregionCd(model.getRegion() != null ? model.getRegion() : model.getCmrIssuingCntry());
        exc = entityManager.find(ScenarioExceptions.class, pk);
        if (exc != null) {
          LOG.debug("Updating exception for " + model.getCmrIssuingCntry() + " - " + model.getCustTyp() + "/" + model.getCustSubTyp());
          PropertyUtils.copyProperties(exc, model);
          exc.setDupCheckAddrTypes(model.cleanDupAddressChecks());
          exc.setSkipCheckAddrTypes(model.cleanSkipAddressChecks());
          exc.setLastUpdtBy(user.getIntranetId());
          exc.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
          entityManager.merge(exc);
        }
        break;
      case "N":
        pk = new ScenarioExceptionsPK();
        pk.setCmrIssuingCntry(model.getCmrIssuingCntry());
        pk.setCustTyp(model.getCustTyp());
        pk.setCustSubTyp(model.getCustSubTyp());
        pk.setSubregionCd(model.getRegion() != null ? model.getRegion() : model.getCmrIssuingCntry());
        exc = new ScenarioExceptions();
        exc.setId(pk);

        LOG.debug("Adding exception for " + model.getCmrIssuingCntry() + " - " + model.getCustTyp() + "/" + model.getCustSubTyp());
        PropertyUtils.copyProperties(exc, model);
        exc.setDupCheckAddrTypes(model.cleanDupAddressChecks());
        exc.setSkipCheckAddrTypes(model.cleanSkipAddressChecks());
        exc.setCreateBy(user.getIntranetId());
        exc.setCreateTs(SystemUtil.getCurrentTimestamp());
        exc.setLastUpdtBy(user.getIntranetId());
        exc.setLastUpdtTs(SystemUtil.getCurrentTimestamp());
        entityManager.merge(exc);
        break;
      }
      entityManager.flush();
      result.setSuccess(true);
    } catch (Exception e) {
      LOG.error("Error in saving element configurations.", e);
      result.setSuccess(false);
      result.setMessage("An error occurred while saving the exception.");
    }
    outMap.put(OUT_PROCESS_RESULT, result);

  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

}
