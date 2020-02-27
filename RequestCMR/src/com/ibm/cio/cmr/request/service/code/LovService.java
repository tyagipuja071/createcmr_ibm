package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.Lov;
import com.ibm.cio.cmr.request.entity.LovPK;
import com.ibm.cio.cmr.request.model.code.LovModel;
import com.ibm.cio.cmr.request.model.code.LovValues;
import com.ibm.cio.cmr.request.model.code.LovValuesModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Rochelle Salazar
 * 
 */
@Component
public class LovService extends BaseService<LovModel, Lov> {

  private LovValuesModel valuesModel;

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(LovService.class);
  }

  @Override
  protected void performTransaction(LovModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    if (this.valuesModel != null) {
      // String sql = "delete from CREQCMR.LOV where FIELD_ID = :FIELD_ID and
      // CMR_ISSUING_CNTRY = :CMR_ISSUING_CNTRY";
      // PreparedQuery query = new PreparedQuery(entityManager, sql);
      // query.setParameter("FIELD_ID", this.valuesModel.getFieldId());
      // query.setParameter("CMR_ISSUING_CNTRY",
      // this.valuesModel.getCmrIssuingCntry());
      // log.debug("Deleting LOV for " + valuesModel.getFieldId() + " - " +
      // valuesModel.getCmrIssuingCntry());
      // query.executeSql();

      if (!this.valuesModel.isDelete()) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Integer> countMap = new HashMap<String, Integer>();
        for (String stringValue : valuesModel.getValues()) {
          try {
            LovValues value = mapper.readValue(stringValue, LovValues.class);
            if (!"DUMMY".equals(value.getCd())) {

              if (!countMap.containsKey(value.getStatus())) {
                countMap.put(value.getStatus(), 0);
              }

              countMap.put(value.getStatus(), countMap.get(value.getStatus()) + 1);

              LovPK pk = new LovPK();
              pk.setFieldId(valuesModel.getFieldId());
              pk.setCmrIssuingCntry(valuesModel.getCmrIssuingCntry());
              pk.setCd("-BLANK-".equals(value.getCd()) ? "" : value.getCd());
              Lov lov = new Lov();
              lov.setId(pk);
              lov.setCmt(valuesModel.getCmt());
              lov.setDefaultInd(value.isDefaultInd() ? "Y" : "N");
              lov.setDispOrder(value.getDispOrder());
              lov.setDispType(valuesModel.getDispType());
              lov.setTxt(value.getTxt());

              switch (value.getStatus()) {
              case "N":
                entityManager.persist(lov);
                break;
              case "D":
                lov = entityManager.find(Lov.class, pk);
                if (lov != null) {
                  entityManager.remove(lov);
                }
                break;
              case "M":
                entityManager.merge(lov);
                break;
              }
            }

          } catch (Exception e) {
            log.warn("Cannot convert to value object: " + stringValue);
          }
        }
        entityManager.flush();
        for (String key : countMap.keySet()) {
          System.err.println(key + " - " + countMap.get(key));
        }
      }

      SystemUtil.logSystemAdminAction(entityManager, AppUser.getUser(request), "LOV", "U", this.valuesModel.getFieldId(),
          this.valuesModel.getCmrIssuingCntry(), "", "");
    }
  }

  @Override
  protected List<LovModel> doSearch(LovModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.LOV");
    String fieldId = request.getParameter("fieldId");
    if (StringUtils.isBlank(fieldId)) {
      fieldId = "xxx"; // to retrieve nothing
    }
    String cmrIssuingCntry = request.getParameter("cmrIssuingCntry");
    if (StringUtils.isEmpty(cmrIssuingCntry)) {
      cmrIssuingCntry = "";
    }
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("FIELD_ID", fieldId);
    q.setParameter("CNTRY", "%" + cmrIssuingCntry + "%");
    q.setForReadOnly(true);
    List<LovModel> list = new ArrayList<>();
    List<Lov> record = q.getResults(Lov.class);

    LovModel lovModel = null;
    for (Lov lov : record) {
      lovModel = new LovModel();
      lovModel.setFieldId(lov.getId().getFieldId());
      lovModel.setCmrIssuingCntry(lov.getId().getCmrIssuingCntry());
      lovModel.setCd(lov.getId().getCd());
      lovModel.setCmt(lov.getCmt());
      lovModel.setDefaultInd(lov.getDefaultInd());
      lovModel.setDispOrder(lov.getDispOrder());
      lovModel.setTxt(lov.getTxt());
      if (lov.getDispType().equalsIgnoreCase("C")) {
        lovModel.setDispType("Code");
      } else if (lov.getDispType().equalsIgnoreCase("T")) {
        lovModel.setDispType("Text");
      } else if (lov.getDispType().equalsIgnoreCase("B")) {
        lovModel.setDispType("Both");
      } else {
        lovModel.setDispType(lov.getDispType());
      }
      list.add(lovModel);
    }
    return list;
  }

  @Override
  protected Lov getCurrentRecord(LovModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected Lov createFromModel(LovModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  public LovValuesModel getValuesModel() {
    return valuesModel;
  }

  public void setValuesModel(LovValuesModel valuesModel) {
    this.valuesModel = valuesModel;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
