/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.entity.CustScenarios;
import com.ibm.cio.cmr.request.entity.CustScenariosPK;
import com.ibm.cio.cmr.request.entity.CustSubType;
import com.ibm.cio.cmr.request.entity.CustSubTypePK;
import com.ibm.cio.cmr.request.entity.CustType;
import com.ibm.cio.cmr.request.entity.CustTypePK;
import com.ibm.cio.cmr.request.model.ParamContainer;
import com.ibm.cio.cmr.request.model.ProcessResultModel;
import com.ibm.cio.cmr.request.model.code.ScenarioField;
import com.ibm.cio.cmr.request.model.code.ScenariosModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseSimpleService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class ScenariosService extends BaseSimpleService<ProcessResultModel> {

  private static final Logger LOG = Logger.getLogger(ScenariosService.class);

  @Override
  protected ProcessResultModel doProcess(EntityManager entityManager, HttpServletRequest request, ParamContainer params) throws Exception {
    ProcessResultModel result = new ProcessResultModel();
    String action = (String) params.getParam("action");
    String json = (String) params.getParam("json");

    AppUser user = AppUser.getUser(request);

    LOG.debug("Processing action " + action);
    ObjectMapper mapper = new ObjectMapper();
    ScenariosModel model = mapper.readValue(json, ScenariosModel.class);
    try {
      switch (action) {
      case ("ADD_TYPE"):
        processAddType(entityManager, model, user);
        break;
      case ("SAVE_SCENARIO"):
        processSaveScenario(entityManager, model, user);
        break;
      case ("DELETE_SCENARIO"):
        processDeleteScenario(entityManager, model, user);
        break;
      case ("PROPAGATE"):
        processPropagateField(entityManager, model, user);
        break;
      }
      result.setSuccess(true);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setMessage("An error occurred while processing action " + action);
    }
    return result;
  }

  private void processSaveScenario(EntityManager entityManager, ScenariosModel model, AppUser user) {
    List<ScenarioField> fields = model.getFields();
    Collections.sort(fields);

    deleteCurrentRecords(entityManager, model, user);

    String locCd = model.getIssuingCntry();
    String custTyp = model.getCustSubTypeVal(); // use subtype only

    List<String> values = null;

    CustScenarios scenario = null;
    CustScenariosPK pk = null;

    for (ScenarioField field : fields) {
      values = field.getValues();
      if (values == null || values.isEmpty()) {
        // only one record to be created
        pk = new CustScenariosPK();
        pk.setAddrTyp(field.getAddrTyp() == null ? "" : field.getAddrTyp());
        pk.setCustSubTyp("");
        pk.setCustTyp(custTyp);
        pk.setFieldId(field.getFieldId());
        pk.setLocCd(locCd);
        pk.setSeqNo(1);
        scenario = new CustScenarios();
        scenario.setId(pk);
        scenario.setFieldName(field.getFieldName());
        scenario.setLockedIndc(field.getLockedIndc());
        scenario.setParTabId(field.getTabId());
        scenario.setReqInd(field.getReqInd());
        scenario.setRetainValInd(field.isRetainValInd() ? "Y" : "N");
        scenario.setValue("");
        entityManager.persist(scenario);
        entityManager.flush();
      } else if (!values.isEmpty()) {
        int seq = 1;
        for (String value : values) {
          pk = new CustScenariosPK();
          pk.setAddrTyp(field.getAddrTyp() == null ? "" : field.getAddrTyp());
          pk.setCustSubTyp("");
          pk.setCustTyp(custTyp);
          pk.setFieldId(field.getFieldId());
          pk.setLocCd(locCd);
          pk.setSeqNo(seq);
          scenario = new CustScenarios();
          scenario.setId(pk);
          scenario.setFieldName(field.getFieldName());
          scenario.setLockedIndc(field.getLockedIndc());
          scenario.setParTabId(field.getTabId());
          scenario.setReqInd(field.getReqInd());
          scenario.setRetainValInd(field.isRetainValInd() ? "Y" : "N");
          scenario.setValue(value);
          entityManager.persist(scenario);
          seq++;
        }
        entityManager.flush();
      }
    }

    SystemUtil.logSystemAdminAction(entityManager, user, "CUST_SCENARIOS", "U", model.getCustSubTypeVal(), model.getIssuingCntry(), "", "");

  }

  private void processDeleteScenario(EntityManager entityManager, ScenariosModel model, AppUser user) {

    deleteCurrentRecords(entityManager, model, user);

    CustSubTypePK subtypePk = new CustSubTypePK();
    subtypePk.setIssuingCntry(model.getIssuingCntry());
    subtypePk.setCustTypVal(model.getCustTypeVal());
    subtypePk.setCustSubTypVal(model.getCustSubTypeVal());
    CustSubType subtype = entityManager.find(CustSubType.class, subtypePk);
    if (subtype != null) {
      LOG.debug("Removing subtype " + model.getCustSubTypeVal() + " from CUST_SUB_TYPE under " + model.getIssuingCntry());
      entityManager.remove(subtype);
      entityManager.flush();
    }

    SystemUtil.logSystemAdminAction(entityManager, user, "CUST_SCENARIOS", "D", model.getCustSubTypeVal(), model.getIssuingCntry(), "", "");

  }

  private void processPropagateField(EntityManager entityManager, ScenariosModel model, AppUser user) {

    String issuingCntry = model.getIssuingCntry();
    String subType = model.getCustSubTypeVal();

    ScenarioField field = !model.getFields().isEmpty() ? model.getFields().get(0) : null;
    if (field != null) {
      String fieldId = field.getFieldId();
      String addrType = field.getAddrTyp();

      String sql = ExternalizedQuery.getSql("SCENARIOS.PROPAGATE");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("LOC_CD", issuingCntry);
      query.setParameter("TYPE", subType);
      query.setParameter("FIELD_ID", fieldId);
      query.setParameter("ADDR_TYPE", StringUtils.isEmpty(addrType) ? "" : addrType);

      List<String> results = query.getResults(String.class);
      CustScenarios scenario = null;
      CustScenariosPK pk = null;
      if (results != null) {
        for (String trgType : results) {

          pk = new CustScenariosPK();
          pk.setAddrTyp(StringUtils.isEmpty(addrType) ? "" : addrType);
          pk.setCustSubTyp("");
          pk.setCustTyp(trgType);
          pk.setFieldId(fieldId);
          pk.setLocCd(issuingCntry);
          pk.setSeqNo(1);

          scenario = new CustScenarios();
          scenario.setId(pk);
          scenario.setFieldName(field.getFieldName());
          scenario.setLockedIndc(field.getLockedIndc());
          scenario.setParTabId(field.getTabId());
          scenario.setReqInd(field.getReqInd());
          scenario.setRetainValInd(field.isRetainValInd() ? "Y" : "N");
          scenario.setValue(field.getValues().isEmpty() ? "" : field.getValues().get(0));

          LOG.debug("Propagating Field " + fieldId + " to scenario " + trgType + " under " + issuingCntry);
          entityManager.persist(scenario);
          entityManager.flush();
        }
      }
      SystemUtil.logSystemAdminAction(entityManager, user, "CUST_SCENARIOS", "U", model.getCustSubTypeVal(), model.getIssuingCntry(), "", fieldId);
    }

  }

  private void deleteCurrentRecords(EntityManager entityManager, ScenariosModel model, AppUser user) {
    String sql = "delete from CREQCMR.CUST_SCENARIOS where LOC_CD = :CNTRY and CUST_TYP = :CODE";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("CNTRY", model.getIssuingCntry());
    query.setParameter("CODE", model.getCustSubTypeVal());
    LOG.debug("Deleting scenario " + model.getCustSubTypeVal() + " under " + model.getIssuingCntry());
    query.executeSql();
  }

  private void processAddType(EntityManager entityManager, ScenariosModel model, AppUser user) {
    switch (model.getAddMode()) {
    case "T":
      CustType type = new CustType();
      CustTypePK typePk = new CustTypePK();
      typePk.setIssuingCntry(model.getIssuingCntry());
      typePk.setCustTypVal(model.getCode());
      type.setId(typePk);
      type.setCustTypDesc(model.getDesc());
      type.setCreateById(user.getIntranetId());
      type.setCreateTs(SystemUtil.getCurrentTimestamp());
      type.setGeoCd("");
      type.setUpdateById(user.getIntranetId());
      type.setUpdateTs(SystemUtil.getCurrentTimestamp());
      LOG.debug("Creating CUST_TYPE " + model.getCode() + " - " + model.getDesc());
      entityManager.persist(type);
      entityManager.flush();
      SystemUtil.logSystemAdminAction(entityManager, user, "CUST_TYPE", "I", model.getCode(), model.getIssuingCntry(), "", model.getCode());
      break;
    case "S":
      CustSubType subtype = new CustSubType();
      CustSubTypePK subtypePk = new CustSubTypePK();
      subtypePk.setIssuingCntry(model.getIssuingCntry());
      subtypePk.setCustTypVal(model.getCustTypeVal());
      subtypePk.setCustSubTypVal(model.getCode());
      subtype.setId(subtypePk);
      subtype.setCustSubTypDesc(model.getDesc());
      subtype.setCreateById(user.getIntranetId());
      subtype.setCreateTs(SystemUtil.getCurrentTimestamp());
      subtype.setGeoCd("");
      subtype.setUpdateById(user.getIntranetId());
      subtype.setUpdateTs(SystemUtil.getCurrentTimestamp());
      LOG.debug("Creating CUST_SUB_TYPE " + model.getCode() + " - " + model.getDesc());
      entityManager.persist(subtype);
      entityManager.flush();
      SystemUtil
          .logSystemAdminAction(entityManager, user, "CUST_SUB_TYPE", "I", model.getCustTypeVal(), model.getIssuingCntry(), "", model.getCode());
      break;
    }
  }

  @Override
  protected boolean isTransactional() {
    return true;
  }

}
