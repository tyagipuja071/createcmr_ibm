/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.RestrictToCode;
import com.ibm.cio.cmr.request.model.code.RestrictModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class RestrictService extends BaseService<RestrictModel, RestrictToCode> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(RestrictService.class);
  }

  @Override
  protected void performTransaction(RestrictModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    /*
     * if ("MASS_DELETE".equals(model.getMassAction())) { List<KeyContainer>
     * keys = extractKeys(model); String fieldId = null; String cntry = null;
     * String seqNo = null; FieldInfo info = null; String sql =
     * "select * from CREQCMR.FIELD_INFO where FIELD_ID = :ID and CMR_ISSUING_CNTRY = :CNTRY and SEQ_NO = :SEQ_NO"
     * ; PreparedQuery query = null; for (KeyContainer key : keys) { fieldId =
     * key.getKey("fieldId"); cntry = key.getKey("cmrIssuingCntry"); seqNo =
     * key.getKey("seqNo"); if ("1".equals(seqNo)) { // sequence 1 cannot be
     * deleted throw new CmrException(24); } query = new
     * PreparedQuery(entityManager, sql); query.setParameter("ID", fieldId);
     * query.setParameter("CNTRY", cntry); query.setParameter("SEQ_NO",
     * Integer.parseInt(seqNo)); info = query.getSingleResult(FieldInfo.class);
     * if (info != null) { deleteEntity(info, entityManager); } } }
     */
  }

  @Override
  protected List<RestrictModel> doSearch(RestrictModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("SYSTEM.RSTLIST");
    String rstcode = request.getParameter("restrictToCd");
    String abbnm = request.getParameter("nRestrictToAbbrevNm");
    String rstnm = request.getParameter("nRestrictToNm");

    if (StringUtils.isBlank(rstcode)) {
      rstcode = ""; // to retrieve all land
    }
    if (StringUtils.isBlank(abbnm)) {
      abbnm = ""; // to retrieve all land
    }
    if (StringUtils.isBlank(rstnm)) {
      rstnm = ""; // to retrieve all land
    }

    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("RSTCODE", "%" + rstcode.toUpperCase() + "%");
    q.setParameter("ABBREVNM", "%" + abbnm.toUpperCase() + "%");
    q.setParameter("RSTNM", "%" + rstnm.toUpperCase() + "%");
    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    q.setForReadOnly(true);
    List<RestrictToCode> rstList = q.getResults(RestrictToCode.class);
    List<RestrictModel> list = new ArrayList<>();
    RestrictModel rstModel = null;
    for (RestrictToCode rst : rstList) {
      rstModel = new RestrictModel();
      rstModel.setRestrictToCd(rst.getId().getRestrictToCd());
      rstModel.setMandt(rst.getId().getMandt());
      rstModel.setnRestrictToAbbrevNm(rst.getnRestrictToAbbrevNm());
      rstModel.setnRestrictToNm(rst.getnRestrictToNm());
      rstModel.setnKatr10(rst.getnKatr10());
      rstModel.setnLoevm(rst.getnLoevm());
      rstModel.setCreatedBy(rst.getCreateBy());
      rstModel.setCreateDt(formatter.format(rst.getCreateDate()));
      rstModel.setUpdatedBy(rst.getUpdateBy());
      rstModel.setUpdateDt(formatter.format(rst.getUpdateDate()));
      rstModel.setUpdateType(rst.getUpdateType());
      list.add(rstModel);
    }
    return list;
  }

  @Override
  protected RestrictToCode getCurrentRecord(RestrictModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected RestrictToCode createFromModel(RestrictModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
