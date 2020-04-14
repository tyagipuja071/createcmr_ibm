package com.ibm.cio.cmr.request.service.code;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.RepTeam;
import com.ibm.cio.cmr.request.entity.RepTeamPK;
import com.ibm.cio.cmr.request.model.KeyContainer;
import com.ibm.cio.cmr.request.model.code.RepTeamModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;

/**
 * 
 * @author Anuja Srivastava
 *
 */
@Component
public class RepTeamService extends BaseService<RepTeamModel, RepTeam> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(RepTeamService.class);
  }

  @Override
  protected void performTransaction(RepTeamModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {

    if (model != null) {
      if (StringUtils.isBlank(model.getAction())) {
        throw new Exception("No action defined");
      }
      if ("REMOVE_MAPPINGS".equals(model.getAction())) {
        List<KeyContainer> keys = extractKeys(model);
        RepTeam selectedRt = null;
        String issuingCntry = null;
        String repTeamCd = null;
        String repTeamMemberNo = null;
        for (KeyContainer key : keys) {
          issuingCntry = key.getKey("issuingCntry");
          repTeamCd = key.getKey("repTeamCd");
          repTeamMemberNo = key.getKey("repTeamMemberNo");
          RepTeamPK rtPk = new RepTeamPK();
          rtPk.setIssuingCntry(StringUtils.isNotBlank(issuingCntry) ? issuingCntry : "");
          rtPk.setRepTeamCd(StringUtils.isNotBlank(repTeamCd) ? repTeamCd : "");
          rtPk.setRepTeamMemberNo(StringUtils.isNotBlank(repTeamMemberNo) ? repTeamMemberNo : "");
          selectedRt = entityManager.find(RepTeam.class, rtPk);
          if (selectedRt != null) {
            entityManager.remove(selectedRt);
          }

        }
      }
    }
  }

  @Override
  protected List<RepTeamModel> doSearch(RepTeamModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.REP_TEAM");
    String cmrIssuingCntry = request.getParameter("issuingCntry");
    if (StringUtils.isEmpty(cmrIssuingCntry)) {
      cmrIssuingCntry = "";
    }
    PreparedQuery q = new PreparedQuery(entityManager, sql);
    q.setParameter("CNTRY", "%" + cmrIssuingCntry + "%");
    q.setForReadOnly(true);
    List<RepTeamModel> list = new ArrayList<>();
    List<RepTeam> record = q.getResults(RepTeam.class);
    RepTeamModel rtModel = null;
    for (RepTeam rt : record) {
      rtModel = new RepTeamModel();
      rtModel.setIssuingCntry(rt.getId().getIssuingCntry());
      rtModel.setRepTeamCd(rt.getId().getRepTeamCd());
      rtModel.setRepTeamMemberNo(rt.getId().getRepTeamMemberNo());
      rtModel.setRepTeamMemberName(rt.getRepTeamMemberName());
      list.add(rtModel);
    }
    return list;
  }

  @Override
  protected RepTeam getCurrentRecord(RepTeamModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected RepTeam createFromModel(RepTeamModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
