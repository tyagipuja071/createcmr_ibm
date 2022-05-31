/**
 * 
 */
package com.ibm.cio.cmr.request.service.code;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.entity.USCMRScc;
import com.ibm.cio.cmr.request.entity.USCMRSccPK;
import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.code.SCCModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

/**
 * @author sonali Jain
 * 
 */
@Component
public class SCCMaintainService extends BaseService<SCCModel, USCMRScc> {

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(SCCMaintainService.class);
  }

  @Override
  protected void performTransaction(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
  }

  @Override
  protected List<SCCModel> doSearch(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    USCMRScc field = getCurrentRecord(model, entityManager, request);
    SCCModel newModel = new SCCModel();
    copyValuesFromEntity(field, newModel);
    newModel.setState(BaseModel.STATE_EXISTING);
    return Collections.singletonList(newModel);
  }

  @Override
  protected USCMRScc getCurrentRecord(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String sql = ExternalizedQuery.getSql("SYSTEM.GETSCC2");
    PreparedQuery q = new PreparedQuery(entityManager, sql);

    DecimalFormat df = new DecimalFormat("#");
    // if (!StringUtils.isBlank(Float.toString(model.getcZip()))) {
    // BigDecimal transform = new BigDecimal(model.getcZip());
    // String zip = transform.toPlainString();
    // }
    q.setParameter("STATE", model.getnSt() != null ? model.getnSt().trim().toUpperCase() : "x");
    q.setParameter("CITY", model.getnCity() != null ? model.getnCity().trim().toUpperCase() : "x");
    q.setParameter("COUNTY", model.getnCnty() != null ? model.getnCnty().trim().toUpperCase() : "x");
    q.setParameter("ZIP", df.format(model.getcZip()).trim());
    q.setParameter("SCCID", model.getSccId() != 0 ? model.getSccId() : "0");
    return q.getSingleResult(USCMRScc.class);
  }

  @Override
  protected USCMRScc createFromModel(SCCModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    USCMRScc scc = new USCMRScc();
    USCMRSccPK pk = new USCMRSccPK();
    pk.setSccId(getNextSccId(entityManager));
    // pk.setcCnty(model.getcCnty());
    // pk.setcCity(model.getcCity());
    // pk.setcSt(model.getcSt());
    scc.setId(pk);
    scc.setnCnty(model.getnCnty().toUpperCase());
    scc.setnCity(model.getnCity().toUpperCase());
    scc.setnSt(StringUtils.isBlank(model.getnSt()) ? "''" : model.getnSt().toUpperCase());
    scc.setcZip(model.getcZip());
    scc.setcCnty(
        Float.valueOf(model.getcCnty()) == null ? getNextCounty(entityManager, model.getnSt(), model.getnCity().toUpperCase()) : model.getcCnty());
    scc.setcSt(Float.valueOf(model.getcSt()) == null || model.getcSt() == 0 ? getStateID(entityManager, model.getnSt()) : model.getcSt());
    scc.setcCity(
        Float.valueOf(model.getcCity()) == null ? getNextCityID(entityManager, model.getnSt(), model.getnCnty().toUpperCase()) : model.getcCity());
    scc.setnLand(StringUtils.isBlank(model.getnLand()) ? "" : model.getnLand());
    // SimpleDateFormat formatter = CmrConstants.DATE_FORMAT();
    Timestamp ts = SystemUtil.getCurrentTimestamp();
    scc.setCreateDate(ts);

    AppUser user = AppUser.getUser(request);
    scc.setCreateBy(user.getIntranetId());

    return scc;
  }

  private long getNextSccId(EntityManager entityManager) {
    String sql = ExternalizedQuery.getSql("SCC.GET_MAX_ID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    String code = query.getSingleResult(String.class);
    if (StringUtils.isBlank(code) || Long.valueOf(code) == 0) {
      return 1;
    } else {
      return Long.valueOf(code) + 1;
    }
  }

  private float getNextCityID(EntityManager entityManager, String state, String county) {
    String sql = ExternalizedQuery.getSql("SCC.GET_MAX_CITY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("STATE", state);
    query.setParameter("COUNTY", county);
    String code = query.getSingleResult(String.class);
    if (!StringUtils.isEmpty(code)) {
      return Float.parseFloat(code) + 1;
    }
    return 1;

  }

  private float getStateID(EntityManager entityManager, String stateCode) {
    if (stateCode.equals("''")) {
      return 99;
    }
    String sql = ExternalizedQuery.getSql("SCC.GET_STATE_KEY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("STATE", stateCode);
    String code = query.getSingleResult(String.class);
    if (!StringUtils.isEmpty(code)) {
      return Float.parseFloat(code);
    }
    return 0;

  }

  private float getNextCounty(EntityManager entityManager, String stateCode, String city) {
    String sql = ExternalizedQuery.getSql("SCC.GET_MAX_CNTY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("STATE", stateCode);
    query.setParameter("CITY", city);
    String code = query.getSingleResult(String.class);
    if (!StringUtils.isEmpty(code)) {
      return Float.parseFloat(code) + 1;
    }
    return 0;

  }

  @Override
  protected void doBeforeInsert(USCMRScc entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    entity.setUpdateBy(AppUser.getUser(request).getIntranetId());
    entity.setUpdateDate(SystemUtil.getCurrentTimestamp());
  }

  @Override
  protected void doBeforeUpdate(USCMRScc entity, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    super.doBeforeInsert(entity, entityManager, request);
    entity.setnCity(entity.getnCity().toUpperCase());
    entity.setnCnty(entity.getnCnty().toUpperCase());
    entity.setnSt(entity.getnSt().toUpperCase());
    entity.setUpdateBy(AppUser.getUser(request).getIntranetId());
    entity.setUpdateDate(SystemUtil.getCurrentTimestamp());
  }

  @Override
  protected boolean isSystemAdminService() {
    return true;
  }

}
