package com.ibm.cio.cmr.request.service.code;

import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.RepTeam;
import com.ibm.cio.cmr.request.entity.RepTeamPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.RepTeamMaintModel;
import com.ibm.cio.cmr.request.model.code.RepTeamModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * 
 * @author Anuja Srivastava
 *
 */
@Controller
public class RepTeamMaintService extends BaseService<RepTeamModel, RepTeam> {

  RepTeamMaintModel maintModel;

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(RepTeamMaintService.class);
  }

  @Override
  protected void performTransaction(RepTeamModel repTeamModel, EntityManager entityManager, HttpServletRequest request) throws Exception {
    if (this.maintModel != null) {
      if (StringUtils.isBlank(this.maintModel.getMassAction())) {
        throw new Exception("Action Not Defined");
      }

      if (StringUtils.isBlank(this.maintModel.getIssuingCntry())) {
        throw new Exception("Issuing Country not defined.");
      }

      if (this.maintModel.getMassAction().equals("MASS_SAVE")) {
        ObjectMapper mapper = new ObjectMapper();
        if (this.maintModel.getItems().size() > 0) {
          this.deleteExisting(entityManager, this.maintModel.getIssuingCntry());
          RepTeamModel model = new RepTeamModel();
          for (String stringValue : this.maintModel.getItems()) {
            try {
              model = mapper.readValue(stringValue, RepTeamModel.class);
              if ("DUMMY".equals(model.getRepTeamCd()) && "DUMMY".equals(model.getRepTeamMemberNo())
                  && "DUMMY".equals(model.getRepTeamMemberName())) {
                continue;
              }
              AppUser user = AppUser.getUser(request);
              RepTeam rt = new RepTeam();
              RepTeamPK pk = new RepTeamPK();
              pk.setIssuingCntry(this.maintModel.getIssuingCntry());
              pk.setRepTeamCd(model.getRepTeamCd());
              pk.setRepTeamMemberNo(model.getRepTeamMemberNo());
              rt.setRepTeamMemberName(model.getRepTeamMemberName());
              rt.setId(pk);
              rt.setCreateById(user.getIntranetId());
              rt.setCreateTs(SystemUtil.getCurrentTimestamp());
              entityManager.persist(rt);
            } catch (Exception e) {
              log.warn("Cannot convert to maint object: " + stringValue);
            }
          }
        }

      } else if ("DELETE".equals(this.maintModel.getMassAction())) {
        deleteExisting(entityManager, this.maintModel.getIssuingCntry());
      }
    }

  }

  private void deleteExisting(EntityManager entityManager, String issuingCntry) {
    String sql = "delete from CREQCMR.REP_TEAM where ISSUING_CNTRY = :ISSUING_CNTRY";
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", issuingCntry);
    log.debug("Deleting Existing REP_TEAM Mappings for issuing country - " + issuingCntry);
    query.executeSql();
  }

  @Override
  protected void doBeforeInsert(RepTeam entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setCreateById(user.getIntranetId());
    entity.setCreateTs(this.currentTimestamp);
    entity.setUpdtById(user.getIntranetId());
    entity.setUpdtTs(this.currentTimestamp);
  }

  @Override
  protected void doBeforeUpdate(RepTeam entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeUpdate(entity, entityManager, request);
    AppUser user = AppUser.getUser(request);
    entity.setUpdtById(user.getIntranetId());
    entity.setUpdtTs(this.currentTimestamp);
  }

  @Override
  protected List<RepTeamModel> doSearch(RepTeamModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    RepTeam rt = getCurrentRecord(model, entityManager, request);
    RepTeamModel newModel = new RepTeamModel();
    if (rt != null) {
      copyValuesFromEntity(rt, newModel);
      newModel.setState(BaseModel.STATE_EXISTING);
    }
    return Collections.singletonList(newModel);

  }

  @Override
  protected RepTeam getCurrentRecord(RepTeamModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String issuingCntry = model.getIssuingCntry();
    if (StringUtils.isNotBlank(issuingCntry) && issuingCntry.length() == 3) {
      String sql = ExternalizedQuery.getSql("SYSTEM.REP_TEAM");
      PreparedQuery q = new PreparedQuery(entityManager, sql);
      q.setParameter("CNTRY", issuingCntry);
      return q.getSingleResult(RepTeam.class);
    }
    return null;
  }

  @Override
  protected RepTeam createFromModel(RepTeamModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    RepTeam rt = new RepTeam();
    RepTeamPK pk = new RepTeamPK();
    pk.setIssuingCntry(model.getIssuingCntry());
    pk.setRepTeamCd(model.getRepTeamCd());
    pk.setRepTeamMemberNo(model.getRepTeamMemberNo());
    rt.setId(pk);
    rt.setRepTeamMemberName(model.getRepTeamMemberName());
    return rt;
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

  public RepTeamMaintModel getMaintModel() {
    return maintModel;
  }

  public void setMaintModel(RepTeamMaintModel maintModel) {
    this.maintModel = maintModel;
  }
}
