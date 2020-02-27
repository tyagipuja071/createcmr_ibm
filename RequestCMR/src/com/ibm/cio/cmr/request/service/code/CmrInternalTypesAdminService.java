/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.CmrInternalTypes;
import com.ibm.cio.cmr.request.entity.Users;
import com.ibm.cio.cmr.request.model.code.CmrInternalTypesModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * @author max
 * 
 */
@Component
public class CmrInternalTypesAdminService extends BaseService<CmrInternalTypesModel, Users> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(CmrInternalTypesAdminService.class);
  }

  @Override
  protected void performTransaction(CmrInternalTypesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<CmrInternalTypesModel> doSearch(CmrInternalTypesModel model, EntityManager entityManager, HttpServletRequest request)
      throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.CMR_INTERNAL_TYPESLIST");
    SimpleDateFormat formatter = new SimpleDateFormat(SystemConfiguration.getValue("DATE_FORMAT"));
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setForReadOnly(true);
    List<CmrInternalTypes> users = q.getResults(CmrInternalTypes.class);
    List<CmrInternalTypesModel> list = new ArrayList<>();
    CmrInternalTypesModel userModel = null;
    for (CmrInternalTypes user : users) {
      userModel = new CmrInternalTypesModel();
      userModel.setCondition(user.getCondition());
      userModel.setCreateBy(user.getCreateBy());

      userModel.setCreateTs(user.getCreateTs());
      userModel.setCreateTsString(formatter.format(user.getCreateTs()));
      userModel.setInternalTyp(user.getId().getInternalTyp());
      userModel.setInternalTypDesc(user.getInternalTypDesc());
      userModel.setPriority(user.getPriority());
      userModel.setStatus(user.getStatus());
      userModel.setUpdateBy(user.getUpdateBy());
      userModel.setUpdateTs(user.getUpdateTs());
      userModel.setUpdateTsString(formatter.format(user.getUpdateTs()));
      userModel.setReqTyp(user.getReqTyp());
      userModel.setSepValInd(user.getSepValInd());
      userModel.setCmrIssuingCntry(user.getId().getCmrIssuingCntry());

      list.add(userModel);
    }
    return list;
  }

  @Override
  protected Users getCurrentRecord(CmrInternalTypesModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    return null;
  }

  @Override
  protected Users createFromModel(CmrInternalTypesModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }
}
