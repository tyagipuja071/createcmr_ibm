/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * Main {@link GEOHandler} implementation for Canada
 * 
 * @author JeffZAMORA
 *
 */
public class CanadaHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(CanadaHandler.class);
  private static final String[] USABLE_ADDRESSES = new String[] { "ZS01", "ZI01", "ZP01" };

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    List<FindCMRRecordModel> converted = new ArrayList<>();
    List<FindCMRRecordModel> records = source.getItems();
    for (FindCMRRecordModel record : records) {
      // if address is usable, process and add to converted
      if (Arrays.asList(USABLE_ADDRESSES).contains(record.getCmrAddrTypeCode())) {
        if ("ZS01".equals(record.getCmrAddrTypeCode()) || "ZI01".equals(record.getCmrAddrTypeCode())) {
          // set the address type to Install At for CreateCMR
          record.setCmrAddrTypeCode("ZS01");
        } else if ("ZP01".equals(record.getCmrAddrTypeCode())) {
          // set the address type to Invoice To for CreateCMR
          record.setCmrAddrTypeCode("ZI01");
        }

        converted.add(record);
      }
    }

    Collections.sort(converted);
    source.setItems(converted);
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
  }

  @Override
  public int getName1Length() {
    return 30;
  }

  @Override
  public int getName2Length() {
    return 30;
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    return false;
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    setAddressRelatedData(entityManager, admin, data, null);
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    if (!"ZS01".equals(addr.getId().getAddrType())) {
      // only update for main address
      return;
    }
    DataPK pk = new DataPK();
    pk.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, pk);
    AdminPK apk = new AdminPK();
    apk.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, apk);
    setAddressRelatedData(entityManager, admin, data, addr);
  }

  @Override
  public boolean customerNamesOnAddress() {
    return false;
  }

  @Override
  public boolean useSeqNoFromImport() {
    return true;
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return false;
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    return null;
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    return false;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }

  /* new functions for Canada only */

  /**
   * Sets the DATA field values that depend on the main address like Location
   * No, etc
   * 
   * @param entityManager
   * @param admin
   * @param data
   * @param zs01
   */
  private void setAddressRelatedData(EntityManager entityManager, Admin admin, Data data, Addr zs01) {
    Addr mainAddr = zs01;
    List<String> caribNorthDistCntries = Arrays.asList("BS", "BB", "BM", "KY", "GY", "JM", "AW", "LC", "SR", "TT", "CW");
    if (mainAddr == null) {
      // reuse italy's ZS01
      String sql = ExternalizedQuery.getSql("ITALY.GETINSTALLING");
      PreparedQuery query = new PreparedQuery(entityManager, sql);
      query.setParameter("REQ_ID", data.getId().getReqId());
      mainAddr = query.getSingleResult(Addr.class);
    }
    if (mainAddr == null) {
      return;
    }

    // set preferred language to F for Quebec
    if ("QC".equals(mainAddr.getStateProv())) {
      data.setCustPrefLang("F");
    }

    // set location number based on state/prov
    if ("CA".equals(mainAddr.getLandCntry())) {
      if ("AB".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("01999");
      } else if ("BC".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("02999");
      } else if ("MB".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("03999");
      } else if ("NB".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("04999");
      } else if ("NL".equals(mainAddr.getStateProv()) || "NF".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("05999");
      } else if ("NT".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("06999");
      } else if ("NS".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("07999");
      } else if ("ON".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("08999");
      } else if ("PE".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("09999");
      } else if ("QC".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("10999");
      } else if ("SK".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("11999");
      } else if ("YT".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("12999");
      } else if ("NU".equals(mainAddr.getStateProv())) {
        data.setLocationNumber("13999");
      }
    } else if (caribNorthDistCntries.contains(mainAddr.getStateProv())) {
      data.setLocationNumber("99000");
    } else {
      data.setLocationNumber("99999");
    }

    // set CS Branch to first 3 digits of postal code
    if (mainAddr.getPostCd() != null && mainAddr.getPostCd().length() >= 3) {
      data.setSalesTeamCd(mainAddr.getPostCd().substring(0, 3));
    }
    if (mainAddr.getCity1() != null) {
      data.setAbbrevLocn(mainAddr.getCity1().length() > 12 ? mainAddr.getCity1().substring(0, 12) : mainAddr.getCity1());
    }

    // set abbreviated name
    String name = admin.getMainCustNm1();
    if (name != null) {
      data.setAbbrevNm(name.length() > 12 ? name.substring(0, 12).toUpperCase() : name);
    }

    entityManager.merge(data);
  }
}
