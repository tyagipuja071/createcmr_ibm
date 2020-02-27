/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.FieldInfo;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.code.FieldInfoModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author Jeffrey Zamora
 * 
 */
@Component
public class FieldInfoService extends BaseService<FieldInfoModel, FieldInfo> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(FieldInfoService.class);
  }

  @Override
  protected void performTransaction(FieldInfoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    if ("MASS_DELETE".equals(model.getMassAction())) {
      List<KeyContainer> keys = extractKeys(model);
      String fieldId = null;
      String cntry = null;
      String seqNo = null;
      FieldInfo info = null;
      String sql = "select * from CREQCMR.FIELD_INFO where FIELD_ID = :ID and CMR_ISSUING_CNTRY = :CNTRY and SEQ_NO = :SEQ_NO";
      PreparedQuery query = null;
      for (KeyContainer key : keys) {
        fieldId = key.getKey("fieldId");
        cntry = key.getKey("cmrIssuingCntry");
        seqNo = key.getKey("seqNo");
        if ("1".equals(seqNo)) {
          // sequence 1 cannot be deleted
          throw new CmrException(24);
        }
        query = new PreparedQuery(entityManager, sql);
        query.setParameter("ID", fieldId);
        query.setParameter("CNTRY", cntry);
        query.setParameter("SEQ_NO", Integer.parseInt(seqNo));
        info = query.getSingleResult(FieldInfo.class);
        if (info != null) {
          deleteEntity(info, entityManager);
        }
      }
      SystemUtil.logSystemAdminAction(entityManager, AppUser.getUser(request), "FIELD_INFO", "D", model.getFieldId(), model.getCmrIssuingCntry(), "",
          "");

    }
  }

  @Override
  protected List<FieldInfoModel> doSearch(FieldInfoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    String sql = ExternalizedQuery.getSql("SYSTEM.FIELDINFOLIST");
    String fieldId = request.getParameter("fieldId");
    if (StringUtils.isBlank(fieldId)) {
      fieldId = "xxx"; // to retrieve nothing
    }
    String cntry = request.getParameter("cmrIssuingCntry");
    if (StringUtils.isBlank(cntry)) {
      cntry = ""; // to retrieve nothing
    }
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("FIELD_ID", fieldId);
    q.setParameter("CNTRY", "%" + cntry + "%");
    q.setForReadOnly(true);
    List<FieldInfo> fields = q.getResults(FieldInfo.class);
    List<FieldInfoModel> list = new ArrayList<>();
    FieldInfoModel fieldInfoModel = null;
    for (FieldInfo fieldinfo : fields) {
      fieldInfoModel = new FieldInfoModel();
      fieldInfoModel.setFieldId(fieldinfo.getId().getFieldId());
      fieldInfoModel.setCmrIssuingCntry(fieldinfo.getId().getCmrIssuingCntry());
      fieldInfoModel.setSeqNo(fieldinfo.getId().getSeqNo());
      fieldInfoModel.setType(fieldinfo.getType());
      fieldInfoModel.setChoice(fieldinfo.getChoice());
      fieldInfoModel.setMinLength(fieldinfo.getMinLength());
      fieldInfoModel.setMaxLength(fieldinfo.getMaxLength());
      fieldInfoModel.setValidation(fieldinfo.getValidation());
      fieldInfoModel.setRequired(fieldinfo.getRequired());
      fieldInfoModel.setDependsOn(fieldinfo.getDependsOn());
      fieldInfoModel.setDependsSetting(fieldinfo.getDependsSetting());
      fieldInfoModel.setCondReqInd(fieldinfo.getCondReqInd());
      fieldInfoModel.setCmt(fieldinfo.getCmt());
      fieldInfoModel.setValDependsOn(fieldinfo.getValDependsOn());

      list.add(fieldInfoModel);
    }
    return list;
  }

  @Override
  protected FieldInfo getCurrentRecord(FieldInfoModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected FieldInfo createFromModel(FieldInfoModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
