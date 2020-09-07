/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
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

/**
 * Handler for MCO Central, East, West Africa
 * 
 * @author Eduard Bernardo
 * 
 */
public class MCOCewaHandler extends MCOHandler {

  protected static final Logger LOG = Logger.getLogger(MCOCewaHandler.class);

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    super.setDataValuesOnImport(admin, data, results, mainRecord);

    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      if (legacyObjects != null && legacyObjects.getCustomer() != null) {
        data.setCrosSubTyp(legacyObjects.getCustomer().getCustType());
      }
    }

  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    super.setAddressValuesOnImport(address, admin, currentRecord, cmrNo);

    String country = currentRecord.getCmrIssuedBy();
    String processingType = PageManager.getProcessingType(country, "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (currentRecord.getCmrAddrSeq() != null && CmrConstants.REQ_TYPE_CREATE.equals(admin.getReqType())
          && "ZS01".equalsIgnoreCase(address.getId().getAddrType())) {
        String seq = address.getId().getAddrSeq();
        seq = StringUtils.leftPad(seq, 5, '0');
        address.getId().setAddrSeq(seq);
      }
      address.setIerpSitePrtyId(currentRecord.getCmrSitePartyID());
      if (!("ZS01".equals(address.getId().getAddrType()) || "ZD01".equals(address.getId().getAddrType()))) {
        address.setCustPhone("");
      }
    }
  }

  @Override
  public List<String> getMandtAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZS01", "ZP01", "ZI01", "ZD01", "ZS02");
  }

  @Override
  public List<String> getAdditionalAddrTypeForLDSeqGen(String cmrIssuingCntry) {
    return Arrays.asList("ZD01", "ZI01");
  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    data.setRepTeamMemberNo("DUMMY1");
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    super.doBeforeAddrSave(entityManager, addr, cmrIssuingCntry);

    if (addr != null) {
      if (!("ZS01".equals(addr.getId().getAddrType()) || "ZD01".equals(addr.getId().getAddrType()))) {
        addr.setCustPhone("");
      }

      if (!("ZS01".equals(addr.getId().getAddrType()) || "ZP01".equals(addr.getId().getAddrType()))) {
        addr.setPoBox("");
      }
    }
  }

  @Override
  public List<String> getDataFieldsForUpdateCheckLegacy(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("SALES_BO_CD", "REP_TEAM_MEMBER_NO", "MODE_OF_PAYMENT", "VAT", "ISIC_CD", "EMBARGO_CD", "MAILING_COND", "ABBREV_NM",
        "LOCN_NO", "CLIENT_TIER", "ENGINEERING_BO", "ENTERPRISE", "CUST_PREF_LANG", "INAC_CD", "ISU_CD", "COLLECTION_CD", "SPECIAL_TAX_CD",
        "SEARCH_TERM", "SUB_INDUSTRY_CD", "ABBREV_LOCN", "PPSCEID", "IBM_DEPT_COST_CENTER"));
    return fields;
  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(
        Arrays.asList("CUST_NM1", "CUST_NM2", "LAND_CNTRY", "CUST_NM4", "ADDR_TXT", "ADDR_TXT_2", "CITY1", "CUST_PHONE", "POST_CD", "PO_BOX"));
    return fields;
  }

  @Override
  public void handleSOFConvertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, FindCMRRecordModel mainRecord,
      List<FindCMRRecordModel> converted, ImportCMRModel searchModel) throws Exception {

    String processingType = PageManager.getProcessingType(mainRecord.getCmrIssuedBy(), "U");
    if (CmrConstants.PROCESSING_TYPE_LEGACY_DIRECT.equals(processingType)) {
      if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
        mapCreateReqAddrLD(mainRecord, converted);
      } else if (CmrConstants.REQ_TYPE_UPDATE.equals(reqEntry.getReqType())) {
        mapUpdateReqAddrLD(source, converted);
      }
    } else {
      super.handleSOFConvertFrom(entityManager, source, reqEntry, mainRecord, converted, searchModel);
    }
  }

  private void mapCreateReqAddrLD(FindCMRRecordModel mainRecord, List<FindCMRRecordModel> converted) {
    // only add zs01 equivalent for create by model
    FindCMRRecordModel record = mainRecord;

    if (!StringUtils.isEmpty(record.getCmrName3())) {
      record.setCmrStreetAddressCont(record.getCmrName3());
    }

    if (!StringUtils.isEmpty(record.getCmrName4())) {
      record.setCmrName4(record.getCmrName4());
    }

    if (!StringUtils.isBlank(record.getCmrPOBox())) {
      record.setCmrPOBox(record.getCmrPOBox());
    }
    if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
      record.setCmrAddrSeq("00001");
    } else {
      record.setCmrAddrSeq(StringUtils.leftPad(record.getCmrAddrSeq(), 5, '0'));
    }

    record.setCmrName2Plain(record.getCmrName2Plain());

    if (StringUtils.isEmpty(record.getCmrCustPhone())) {
      record.setCmrCustPhone(this.currentImportValues.get("BillingPhone"));
    }
    converted.add(record);
  }

  private void mapUpdateReqAddrLD(FindCMRResultModel source, List<FindCMRRecordModel> converted) throws Exception {
    if (source.getItems() != null) {

      String addrType = null;
      String seqNo = null;
      List<String> sofUses = null;
      FindCMRRecordModel addr = null;

      // map RDc - SOF - CreateCMR by sequence no
      for (FindCMRRecordModel record : source.getItems()) {
        seqNo = record.getCmrAddrSeq();
        if (!StringUtils.isBlank(seqNo) && StringUtils.isNumeric(seqNo)) {
          sofUses = this.legacyObjects.getUsesBySequenceNo(seqNo);
          for (String sofUse : sofUses) {
            addrType = getAddressTypeByUse(sofUse);
            if (!StringUtils.isEmpty(addrType)) {
              addr = cloneAddress(record, addrType);
              LOG.trace("Adding address type " + addrType + " for sequence " + seqNo);

              // name3 in rdc = Address Con't on SOF
              addr.setCmrStreetAddressCont(record.getCmrName3());
              addr.setCmrName3(record.getCmrName3());
              addr.setCmrName2Plain(record.getCmrName2Plain());
              addr.setCmrSitePartyID(record.getCmrSitePartyID());

              if (!StringUtils.isBlank(record.getCmrPOBox())) {
                addr.setCmrPOBox(record.getCmrPOBox());
              }

              if (StringUtils.isEmpty(record.getCmrAddrSeq())) {
                addr.setCmrAddrSeq("00001");
              }
              converted.add(addr);
            }
          }
        }
      }
    }
  }

  @Override
  protected String getAddressTypeByUse(String addressUse) {
    switch (addressUse) {
    case "1":
      return "ZS01";
    case "2":
      return "ZP01";
    case "3":
      return "ZI01";
    case "4":
      return "ZD01";
    case "5":
      return "ZS02";
    }
    return null;
  }
}
