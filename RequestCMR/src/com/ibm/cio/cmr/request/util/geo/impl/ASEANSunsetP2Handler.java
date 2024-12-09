package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.ui.PageManager;

public class ASEANSunsetP2Handler extends ASEANSunsetHandler {
  private static final Logger LOG = Logger.getLogger(ASEANSunsetP2Handler.class);

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    LOG.debug("Issuing Country: " + reqEntry.getCmrIssuingCntry());
    String processingType = PageManager.getProcessingType(reqEntry.getCmrIssuingCntry(), "U");
    LOG.info("ASEANSunsetP2Handler processing type: " + processingType);

    if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) {
      LOG.info("DR - IERP Logic");
      convertFromIerp(entityManager, source, reqEntry, searchModel);
    } else {
      LOG.info("Phase 1 - MQ Logic");
      super.convertFrom(entityManager, source, reqEntry, searchModel);
    }
  }

  private void convertFromIerp(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    List<FindCMRRecordModel> converted = new ArrayList<>();
    List<FindCMRRecordModel> records = source.getItems();

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      for (FindCMRRecordModel record : records) {
        if ("ZS01".equals(record.getCmrAddrTypeCode())) {
          record.setCmrAddrSeq(GRP1_SOLD_TO_FIXED_SEQ);
          converted.add(record);
        }
      }
      source.setItems(converted);
    } else if (CmrConstants.REQ_TYPE_UPDATE.equals(reqEntry.getReqType())) {
      for (FindCMRRecordModel record : records) {
        String addrType = getMappedAddressType(record.getCmrIssuedBy(), record.getCmrAddrTypeCode(), record.getCmrAddrSeq());
        record.setCmrAddrTypeCode(addrType);
        converted.add(record);
      }
      source.setItems(converted);
    }
  }

  @Override
  public void doAfterConvert(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel,
      List<FindCMRRecordModel> converted) {
    LOG.debug("Issuing Country: " + reqEntry.getCmrIssuingCntry());
    String processingType = PageManager.getProcessingType(reqEntry.getCmrIssuingCntry(), "U");
    LOG.info("ASEANSunsetP2Handler processing type: " + processingType);

    if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) {
      LOG.info("DR - IERP Logic");
      // Override this method to prevent the parent logic from being called
    } else {
      LOG.info("Phase 1 - MQ Logic");
      super.doAfterConvert(entityManager, source, reqEntry, searchModel, converted);
    }
  }

  @Override
  protected void handleRDcRecordValues(FindCMRRecordModel record) {
    LOG.debug("Issuing Country: " + record.getCmrIssuedBy());
    String processingType = PageManager.getProcessingType(record.getCmrIssuedBy(), "U");
    LOG.info("ASEANSunsetP2Handler processing type: " + processingType);

    if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) {
      LOG.info("DR - IERP Logic");
      // Override this method to prevent the parent logic from being called
    } else {
      LOG.info("Phase 1 - MQ Logic");
      super.handleRDcRecordValues(record);
    }
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {

    // Override this method to prevent the parent logic from being called
    LOG.debug("Issuing Country: " + currentRecord.getCmrIssuedBy());
    String processingType = PageManager.getProcessingType(currentRecord.getCmrIssuedBy(), "U");
    LOG.info("ASEANSunsetP2Handler processing type: " + processingType);

    if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) {
      LOG.info("DR - IERP Logic");
      if (currentRecord != null) {
        address.setCustNm1(currentRecord.getCmrName1Plain());
        address.setCustNm2(currentRecord.getCmrName2Plain());
        address.setAddrTxt(currentRecord.getCmrName3());
        address.setAddrTxt2(currentRecord.getCmrStreetAddress());
        address.setDept(currentRecord.getCmrName4());
        address.setCity1(currentRecord.getCmrCity());
        address.setStateProv(currentRecord.getCmrState());
      }
    } else {
      LOG.info("Phase 1 - MQ Logic");
      super.setAddressValuesOnImport(address, admin, currentRecord, cmrNo);
    }

  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    LOG.debug("Issuing Country: " + data.getCmrIssuingCntry());
    String processingType = PageManager.getProcessingType(data.getCmrIssuingCntry(), "U");
    LOG.info("ASEANSunsetP2Handler processing type: " + processingType);
    if (CmrConstants.PROCESSING_TYPE_IERP.equals(processingType)) {
      LOG.info("DR - IERP Logic");
      boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
      if (!prospectCmrChosen) {
        // data.setApCustClusterId(this.currentRecord.get(WtaasQueryKeys.Data.ClusterNo));
        // data.setRepTeamMemberNo(this.currentRecord.get(WtaasQueryKeys.Data.SalesmanNo));
        // data.setCollectionCd(this.currentRecord.get(WtaasQueryKeys.Data.IBMCode));
        // // knvv_ext.ACCT_RECV_BO
        // data.setIsbuCd(this.currentRecord.get(WtaasQueryKeys.Data.SellDept));
        data.setClientTier(mainRecord.getCmrTier());
        // data.setMrcCd(this.currentRecord.get(WtaasQueryKeys.Data.MrktRespCode));
        // data.setTerritoryCd(this.currentRecord.get(WtaasQueryKeys.Data.SellBrnchOff));
        data.setAbbrevLocn(mainRecord.getCmrDataLine());
        // data.setIsuCd(mainRecord.getCmrIsu());
        // data.setBusnType(this.currentRecord.get(WtaasQueryKeys.Data.SellBrnchOff));
        // data.setGovType(this.currentRecord.get(WtaasQueryKeys.Data.GovCustInd));
        // data.setMiscBillCd(this.currentRecord.get(WtaasQueryKeys.Data.RegionCode));
      } else {
        data.setCmrNo("");
      }

      super.autoSetAbbrevLocnNMOnImport(admin, data, results, mainRecord);
      data.setIsuCd(mainRecord.getCmrIsu());

      if (mainRecord.getCmrCountryLandedDesc().isEmpty() && !prospectCmrChosen) {
        data.setAbbrevLocn(mainRecord.getCmrDataLine());
      }

      // fix Defect 1732232: Abbreviated Location issue
      if (!StringUtils.isBlank(data.getAbbrevLocn()) && data.getAbbrevLocn().length() > 9) {
        data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 9));
      }
    } else {
      LOG.info("Phase 1 - MQ Logic");
      super.setDataValuesOnImport(admin, data, results, mainRecord);
    }

  }

  @Override
  public List<String> getDataFieldsForUpdate(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("ABBREV_NM", "CUST_PREF_LANG", "ISIC_CD", "SUB_INDUSTRY_CD", "INAC_CD", "ABBREV_LOCN", "CUST_CLASS", "ISU_CD",
        "CLIENT_TIER", "MRC_CD", "BUSN_TYP", "TERRITORY_CD", "COLLECTION_CD", "MISC_BILL_CD", "COV_DESC", "COV_ID", "GBG_DESC", "GBG_ID", "BG_DESC",
        "BG_ID", "BG_RULE_ID", "GEO_LOC_DESC", "GEO_LOCATION_CD", "DUNS_NO"));
    return fields;
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "LAND_CNTRY", "ADDR_TXT", "ADDR_TXT_2", "DEPT", "CITY1", "STATE_PROV", "POST_CD"));
    return fields;
  }

}
