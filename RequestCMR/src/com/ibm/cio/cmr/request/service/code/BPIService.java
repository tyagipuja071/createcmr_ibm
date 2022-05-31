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
import com.ibm.cio.cmr.request.entity.BPCodeInt;
import com.ibm.cio.cmr.request.model.code.BPIntModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class BPIService extends BaseService<BPIntModel, BPCodeInt> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(BPIService.class);
  }

  @Override
  protected void performTransaction(BPIntModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
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
  protected List<BPIntModel> doSearch(BPIntModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_TIME_FORMAT"));
    String sql = ExternalizedQuery.getSql("SYSTEM.BPILIST");
    String bpcode = request.getParameter("bpCode");
    String abbnm = request.getParameter("nBpAbbrevNm");
    String fullnm = request.getParameter("nBpFullNm");

    if (StringUtils.isBlank(bpcode)) {
      bpcode = ""; // to retrieve all land
    }
    if (StringUtils.isBlank(abbnm)) {
      abbnm = ""; // to retrieve all land
    }
    if (StringUtils.isBlank(fullnm)) {
      fullnm = ""; // to retrieve all land
    }

    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("BPCODE", "%" + bpcode.toUpperCase() + "%");
    q.setParameter("ABBREVNM", "%" + abbnm.toUpperCase() + "%");
    q.setParameter("FULLNM", "%" + fullnm.toUpperCase() + "%");
    q.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    q.setForReadOnly(true);
    List<BPCodeInt> bpiList = q.getResults(BPCodeInt.class);
    List<BPIntModel> list = new ArrayList<>();
    BPIntModel bpIntModel = null;
    for (BPCodeInt bpi : bpiList) {
      bpIntModel = new BPIntModel();
      bpIntModel.setBpCode(bpi.getId().getBpCode());
      bpIntModel.setMandt(bpi.getId().getMandt());
      bpIntModel.setnBpAbbrevNm(bpi.getnBpAbbrevNm());
      bpIntModel.setnBpFullNm(bpi.getnBpFullNm());
      bpIntModel.setnKatr10(bpi.getnKatr10());
      bpIntModel.setnLoevm(bpi.getnLoevm());
      bpIntModel.setCreatedBy(bpi.getCreateBy());
      bpIntModel.setCreateDt(formatter.format(bpi.getCreateDate()));
      bpIntModel.setUpdatedBy(bpi.getUpdateBy());
      bpIntModel.setUpdateDt(formatter.format(bpi.getUpdateDate()));
      list.add(bpIntModel);
    }

    return list;
  }

  @Override
  protected BPCodeInt getCurrentRecord(BPIntModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected BPCodeInt createFromModel(BPIntModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
