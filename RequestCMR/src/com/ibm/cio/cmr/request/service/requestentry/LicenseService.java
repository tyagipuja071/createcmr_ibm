package com.ibm.cio.cmr.request.service.requestentry;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Knvl;
import com.ibm.cio.cmr.request.entity.Licenses;
import com.ibm.cio.cmr.request.entity.LicensesPK;
import com.ibm.cio.cmr.request.model.requestentry.LicenseModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.BaseService;
import com.ibm.cio.cmr.request.user.AppUser;
import com.ibm.cio.cmr.request.util.SystemUtil;

@Component
public class LicenseService extends BaseService<LicenseModel, Licenses> {
  public static final String EXISTING_LICENSE_INDC = "Y";
  public static final String NEW_LICENSE_INDC = "N";

  @Override
  protected Logger initLogger() {
    return Logger.getLogger(LicenseService.class);
  }

  @Override
  protected void performTransaction(LicenseModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    String action = model.getAction();
    AppUser user = AppUser.getUser(request);
    String requesterId = user.getIntranetId();

    if ("ADD_LICENSE".equalsIgnoreCase(action)) {
      createLicenseFromModel(entityManager, model, requesterId);
    } else if ("REMOVE_LICENSE".equalsIgnoreCase(action)) {
      String licenseNumberToRemove = StringUtils.isNotBlank(model.getLicenseNum()) ? model.getLicenseNum() : "";

      // We are only allowed to remove newly added license CURRENT_INDC = "N"
      if (StringUtils.isNotBlank(licenseNumberToRemove) && NEW_LICENSE_INDC.equals(model.getCurrentIndc())) {
        Licenses currentRec = getCurrentRecord(model, entityManager, request);
        deleteEntity(currentRec, entityManager);
      }
    }
  }

  @Override
  protected List<LicenseModel> doSearch(LicenseModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    List<Licenses> allLicenses = getAllLicenses(entityManager, model.getReqId());
    return copyLicensesToModel(allLicenses);
  }

  @Override
  protected Licenses getCurrentRecord(LicenseModel model, EntityManager entityManager, HttpServletRequest request) throws Exception {
    List<Licenses> licenses = getLicensesByIndcLicenseNum(entityManager, model.getReqId(), model.getCurrentIndc(), model.getLicenseNum());

    if (licenses != null && !licenses.isEmpty()) {
      return licenses.get(0);
    }
    return null;
  }

  @Override
  protected Licenses createFromModel(LicenseModel model, EntityManager entityManager, HttpServletRequest request) throws CmrException {
    return null;
  }

  public List<LicenseModel> getNewLicenses(long reqId, EntityManager entityManager) {
    List<Licenses> newLicenses = getLicensesByIndc(entityManager, reqId, NEW_LICENSE_INDC);
    return copyLicensesToModel(newLicenses);
  }

  private List<LicenseModel> copyLicensesToModel(List<Licenses> licenses) {
    List<LicenseModel> licenseModelList = new ArrayList<>();

    if (licenses != null && !licenses.isEmpty()) {
      for (Licenses lic : licenses) {
        LicenseModel lm = new LicenseModel();
        copyValuesFromEntity(lic, lm);
        licenseModelList.add(lm);
      }
    }
    return licenseModelList;
  }

  public void deleteAllLicense(List<Licenses> licenseList, EntityManager entityManager) {
    if (licenseList != null && licenseList.size() > 0) {
      for (int i = 0; i < licenseList.size(); i++) {
        Licenses license = licenseList.get(i);
        Licenses merged = entityManager.merge(license);
        if (merged != null) {
          entityManager.remove(merged);
        }
        entityManager.flush();
      }
    }
  }

  public void createLicenseFromKnvl(EntityManager entityManager, Knvl knvl, long reqId, String requesterId) {
    Licenses license = new Licenses();
    LicensesPK licensePK = new LicensesPK();

    licensePK.setLicenseNum(knvl.getId().getLicnr());
    licensePK.setReqId(reqId);

    license.setValidFrom(knvl.getDatab());
    license.setValidTo(knvl.getDatbi());
    license.setDepCntry(knvl.getId().getAland());
    license.setTaxCat(knvl.getId().getTatyp());
    license.setTaxExemptConf("X".equalsIgnoreCase(knvl.getBelic()) ? "X" : "N");
    license.setCurrentIndc(EXISTING_LICENSE_INDC);
    license.setCreateBy(requesterId);
    license.setCreateTs(SystemUtil.getCurrentTimestamp());
    license.setLastUpdtBy(requesterId);
    license.setLastUpdtTs(SystemUtil.getCurrentTimestamp());

    license.setId(licensePK);

    createEntity(license, entityManager);
  }

  public void createLicenseFromModel(EntityManager entityManager, LicenseModel model, String requesterId) {
    Licenses license = new Licenses();
    LicensesPK licensePK = new LicensesPK();

    licensePK.setLicenseNum(model.getLicenseNum());
    licensePK.setReqId(model.getReqId());

    license.setValidFrom(model.getValidFrom());
    license.setValidTo(model.getValidTo());
    license.setDepCntry("IE");
    license.setTaxCat("ZWST");
    license.setTaxExemptConf("X");
    license.setCurrentIndc(NEW_LICENSE_INDC);
    license.setCreateBy(requesterId);
    license.setCreateTs(SystemUtil.getCurrentTimestamp());
    license.setLastUpdtBy(requesterId);
    license.setLastUpdtTs(SystemUtil.getCurrentTimestamp());

    license.setId(licensePK);

    createEntity(license, entityManager);
  }

  public List<Knvl> getKnvlByKunnr(EntityManager entityManager, String kunnr) {
    String sql = ExternalizedQuery.getSql("GET.KNVL.RECORD");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KUNNR", kunnr);
    return query.getResults(Knvl.class);
  }

  public List<Licenses> getAllLicenses(EntityManager entityManager, Long reqId) {
    PreparedQuery licensesQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("GET.LICENSES_BY_REQ_ID"));
    licensesQuery.setParameter("REQ_ID", reqId);
    List<Licenses> licensesList = licensesQuery.getResults(Licenses.class);
    return licensesList;
  }

  public List<Licenses> getLicensesByIndc(EntityManager entityManager, Long reqId, String indc) {
    PreparedQuery licensesQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("GET.LICENSES_BY_REQ_ID_INDC"));
    licensesQuery.setParameter("REQ_ID", reqId);
    licensesQuery.setParameter("INDC", indc);
    List<Licenses> licensesList = licensesQuery.getResults(Licenses.class);
    return licensesList;
  }

  public List<Licenses> getLicensesByIndcLicenseNum(EntityManager entityManager, Long reqId, String indc, String licenseNumber) {
    PreparedQuery licensesQuery = new PreparedQuery(entityManager, ExternalizedQuery.getSql("GET.LICENSES_BY_REQ_ID_INDC_LICENSE_NUM"));
    licensesQuery.setParameter("REQ_ID", reqId);
    licensesQuery.setParameter("INDC", indc);
    licensesQuery.setParameter("LICENSE_NUM", licenseNumber);
    List<Licenses> licensesList = licensesQuery.getResults(Licenses.class);
    return licensesList;
  }

}
