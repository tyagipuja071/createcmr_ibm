/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate.handler.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.MassCreateAddr;
import com.ibm.cio.cmr.request.entity.MassCreateAddrPK;
import com.ibm.cio.cmr.request.entity.MassCreateData;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.service.requestentry.ImportCMRService;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.masscreate.MassCreateFileRow;
import com.ibm.cmr.create.batch.util.FindCMRUtil;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowHandler;
import com.ibm.cmr.create.batch.util.masscreate.handler.RowResult;

/**
 * @author Joseph Ramos
 * 
 */
public class CACreatebyModelHandler implements RowHandler {

  private static final Logger LOG = Logger.getLogger(CACreatebyModelHandler.class);

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    if (!row.isCreateByModel()) {
      return RowResult.passed();
    }
    RowResult result = new RowResult();
    if (StringUtils.isEmpty(row.getData().getModelCmrNo())) {
      result.addError("Model CMR No is not specified. ");
    }

    MassCreateData data = row.getData();
    String cmrNo = data.getModelCmrNo();
    String cmrIssuingCntry = row.getParentFile().getCmrIssuingCntry();

    FindCMRResultModel resultModel = FindCMRUtil.findCMRs(cmrNo, cmrIssuingCntry);
    FindCMRRecordModel mainRecord = resultModel.getItems().size() > 0 ? resultModel.getItems().get(0) : null;
    if (mainRecord == null) {
      LOG.debug("There are no records found for this CMR");
      result.addError("There are no records found for this CMR. ");
      return result;
    }
    GEOHandler geoHandler = RequestUtils.getGEOHandler(cmrIssuingCntry);
    Data dataTemp = new Data();
    Admin adminTemp = new Admin();
    adminTemp.setReqType(CmrConstants.REQ_TYPE_CREATE);
    RequestEntryModel mockEntryModel = new RequestEntryModel();
    mockEntryModel.setReqType("C");
    if (geoHandler != null) {
      geoHandler.convertFrom(entityManager, resultModel, mockEntryModel, new ImportCMRModel());
    }
    mainRecord = resultModel.getItems().size() > 0 ? resultModel.getItems().get(0) : null;
    if (mainRecord == null) {
      LOG.debug("There are no valid records found for this CMR");
      result.addError("There are no valid records found for this CMR. ");
      return result;
    }

    LOG.debug("Getting current data values..");
    ImportCMRService importCmr = new ImportCMRService();
    importCmr.loadRecordToData(geoHandler, resultModel, mainRecord, adminTemp, dataTemp);

    if (geoHandler != null) {
      geoHandler.setAdminValuesOnImport(adminTemp, mainRecord);
    }
    fillData(mainRecord, row, data, dataTemp, adminTemp);

    LOG.debug("Getting current address values..");
    extractAddresses(resultModel, cmrNo, geoHandler, row, adminTemp);

    return result;
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    if (!row.isCreateByModel()) {
      return;
    }
  }

  private void fillData(FindCMRRecordModel mainRecord, MassCreateFileRow row, MassCreateData data, Data dataTemp, Admin adminTemp) {
    if (StringUtils.isBlank(data.getCustNm1()) && StringUtils.isNotBlank(adminTemp.getMainCustNm1())) {
      data.setCustNm1(adminTemp.getMainCustNm1()); // Customer Name
    }
    if (StringUtils.isBlank(data.getCustNm2()) && StringUtils.isNotBlank(adminTemp.getMainCustNm2())) {
      data.setCustNm2(adminTemp.getMainCustNm2()); // Customer Name Cont
    }
    /*
     * if (StringUtils.isBlank(data.getOemInd()) &&
     * StringUtils.isNotBlank(dataTemp.getOemInd())) {
     * data.setOemInd(dataTemp.getOemInd()); }
     */
    if (StringUtils.isBlank(data.getCustPrefLang()) && StringUtils.isNotBlank(dataTemp.getCustPrefLang())) {
      data.setCustPrefLang(dataTemp.getCustPrefLang()); // Preferred Lang
    }
    if (StringUtils.isBlank(data.getSubIndustryCd()) && StringUtils.isNotBlank(dataTemp.getSubIndustryCd())) {
      data.setSubIndustryCd(dataTemp.getSubIndustryCd()); // Sub Industry
    }
    if (StringUtils.isBlank(data.getSensitiveFlag()) && StringUtils.isNotBlank(dataTemp.getSensitiveFlag())) {
      data.setSensitiveFlag(dataTemp.getSensitiveFlag()); // Sensitive Flag
    }
    if (StringUtils.isBlank(data.getIsicCd()) && StringUtils.isNotBlank(dataTemp.getIsicCd())) {
      data.setIsicCd(dataTemp.getIsicCd()); // ISIC
    }
    if (StringUtils.isBlank(data.getVat()) && StringUtils.isNotBlank(dataTemp.getVat())) {
      data.setVat(dataTemp.getVat()); // GST/HST
    }
    if (StringUtils.isBlank(data.getTaxCd3()) && StringUtils.isNotBlank(dataTemp.getTaxCd3())) {
      data.setTaxCd3(dataTemp.getTaxCd3()); // QST
    }
    if (StringUtils.isBlank(data.getTaxExemp()) && StringUtils.isNotBlank(dataTemp.getVatExempt())) {
      data.setTaxExemp(dataTemp.getVatExempt()); // PST Exempt
    }
    if (StringUtils.isBlank(data.getTaxPayerCustCd()) && StringUtils.isNotBlank(dataTemp.getTaxPayerCustCd())) {
      data.setTaxPayerCustCd(dataTemp.getTaxPayerCustCd()); // PST LicNo
    }
    if (StringUtils.isBlank(data.getSectorCd()) && StringUtils.isNotBlank(dataTemp.getSectorCd())) {
      data.setSectorCd(dataTemp.getSectorCd()); // Auth Exemption Type
    }
    if (StringUtils.isBlank(data.getLeasingCompanyIndc()) && StringUtils.isNotBlank(dataTemp.getLeasingCompanyIndc())) {
      data.setLeasingCompanyIndc(dataTemp.getLeasingCompanyIndc()); // LeasingIndicator
    }
    if (StringUtils.isBlank(data.getContactName1()) && StringUtils.isNotBlank(dataTemp.getContactName1())) {
      data.setContactName1(dataTemp.getContactName1()); // PurchaseOrderNo
    }
    if (StringUtils.isBlank(data.getMiscBillCd()) && StringUtils.isNotBlank(dataTemp.getMiscBillCd())) {
      data.setMiscBillCd(dataTemp.getMiscBillCd()); // LatePaymentChargeInd
    }
    if (StringUtils.isBlank(data.getTaxCd1()) && StringUtils.isNotBlank(dataTemp.getTaxCd1())) {
      data.setTaxCd1(dataTemp.getTaxCd1()); // Est FunctionCode
    }
    if (StringUtils.isBlank(data.getPpsceid())) { // PPSCEID
      if (StringUtils.isNotBlank(dataTemp.getPpsceid())) {
        data.setPpsceid(dataTemp.getPpsceid());
      } else if (StringUtils.isNotBlank(mainRecord.getCmrPpsceid())) {
        data.setPpsceid(mainRecord.getCmrPpsceid());
      }
    }
    if (StringUtils.isBlank(data.getTaxCd2()) && StringUtils.isNotBlank(dataTemp.getTaxCd2())) {
      data.setTaxCd2(dataTemp.getTaxCd2()); // VAD Number
    }
    if (StringUtils.isBlank(data.getSalesBusOffCd()) && StringUtils.isNotBlank(dataTemp.getSalesBusOffCd())) {
      data.setSalesBusOffCd(dataTemp.getSalesBusOffCd()); // SBO
    }
    if (StringUtils.isBlank(data.getInstallBranchOff()) && StringUtils.isNotBlank(dataTemp.getInstallBranchOff())) {
      data.setInstallBranchOff(dataTemp.getInstallBranchOff()); // IBO
    }
    if (StringUtils.isBlank(data.getSalesTeamCd()) && StringUtils.isNotBlank(dataTemp.getSalesTeamCd())) {
      data.setSalesTeamCd(dataTemp.getSalesTeamCd()); // CS Branch
    }
    if (StringUtils.isBlank(data.getAdminDeptCd()) && StringUtils.isNotBlank(dataTemp.getAdminDeptCd())) {
      data.setAdminDeptCd(dataTemp.getAdminDeptCd()); // AR-FAAR
    }
    if (StringUtils.isBlank(data.getCreditCd()) && StringUtils.isNotBlank(dataTemp.getCreditCd())) {
      data.setCreditCd(dataTemp.getCreditCd()); // Credit Code
    }
    if (StringUtils.isBlank(data.getCollectorNo()) && StringUtils.isNotBlank(dataTemp.getCollectorNameNo())) {
      data.setCollectorNo(dataTemp.getCollectorNameNo()); // SW Billing Freq
    }
    if (StringUtils.isBlank(data.getLocationNumber()) && StringUtils.isNotBlank(dataTemp.getLocationNumber())) {
      data.setLocationNumber(dataTemp.getLocationNumber()); // Prov Code
    }
    if (StringUtils.isBlank(data.getCusInvoiceCopies()) && StringUtils.isNotBlank(dataTemp.getCusInvoiceCopies())) {
      data.setCusInvoiceCopies(dataTemp.getCusInvoiceCopies()); // NumofInvoices
    }
    if (StringUtils.isBlank(data.getIsuCd()) && StringUtils.isNotBlank(dataTemp.getIsuCd())) {
      data.setIsuCd(dataTemp.getIsuCd()); // ISU
    }
    if (StringUtils.isBlank(data.getClientTier()) && StringUtils.isNotBlank(dataTemp.getClientTier())) {
      data.setClientTier(dataTemp.getClientTier()); // Client Tier
    }
    if (StringUtils.isBlank(data.getInacCd()) && StringUtils.isNotBlank(dataTemp.getInacCd())) {
      data.setInacCd(dataTemp.getInacCd()); // INAC
    }
    if (StringUtils.isBlank(data.getInacType()) && StringUtils.isNotBlank(dataTemp.getInacType())) {
      data.setInacType(dataTemp.getInacType()); // INAC Type
    }
    if (StringUtils.isBlank(data.getDunsNo())) { // Duns No
      if (StringUtils.isNotBlank(dataTemp.getDunsNo())) {
        data.setDunsNo(dataTemp.getDunsNo());
      } else {
        data.setDunsNo(mainRecord.getCmrDuns());
      }
    }

    if (StringUtils.isBlank(data.getCmrIssuingCntry()) && StringUtils.isNotBlank(dataTemp.getCmrIssuingCntry())) {
      data.setCmrIssuingCntry(dataTemp.getCmrIssuingCntry());
    }
    if (StringUtils.isBlank(data.getCmrOwner()) && !StringUtils.isNotBlank(dataTemp.getCmrOwner())) {
      data.setCmrOwner(dataTemp.getCmrOwner());
    }
    if (StringUtils.isBlank(data.getBgId()) && StringUtils.isNotBlank(dataTemp.getBgId())) {
      data.setBgId(dataTemp.getBgId());
    }
    if (StringUtils.isBlank(data.getBgDesc()) && StringUtils.isNotBlank(dataTemp.getBgDesc())) {
      data.setBgDesc(dataTemp.getBgDesc());
    }
    if (StringUtils.isBlank(data.getGbgId()) && StringUtils.isNotBlank(dataTemp.getGbgId())) {
      data.setGbgId(dataTemp.getGbgId());
    }
    if (StringUtils.isBlank(data.getGbgDesc()) && StringUtils.isNotBlank(dataTemp.getGbgDesc())) {
      data.setGbgDesc(dataTemp.getGbgDesc());
    }
    if (StringUtils.isBlank(data.getBgRuleId()) && StringUtils.isNotBlank(dataTemp.getBgRuleId())) {
      data.setBgRuleId(dataTemp.getBgRuleId());
    }
    if (StringUtils.isBlank(data.getCovId()) && StringUtils.isNotBlank(dataTemp.getCovId())) {
      data.setCovId(dataTemp.getCovId());
    }
    if (StringUtils.isBlank(data.getCovDesc()) && StringUtils.isNotBlank(dataTemp.getCovDesc())) {
      data.setCovDesc(dataTemp.getCovDesc());
    }
    if (StringUtils.isBlank(data.getGeoLocCd()) && StringUtils.isNotBlank(dataTemp.getGeoLocationCd())) {
      data.setGeoLocCd(dataTemp.getGeoLocationCd());
    }
    if (StringUtils.isBlank(data.getGeoLocDesc()) && StringUtils.isNotBlank(dataTemp.getGeoLocDesc())) {
      data.setGeoLocDesc(dataTemp.getGeoLocDesc());
    }
    if (StringUtils.isBlank(data.getOrdBlk()) && StringUtils.isNotBlank(dataTemp.getOrdBlk())) {
      data.setOrdBlk(dataTemp.getOrdBlk());
    }
    if (StringUtils.isBlank(data.getIccTaxExemptStatus()) && StringUtils.isBlank(dataTemp.getIccTaxExemptStatus())) {
      data.setIccTaxExemptStatus(dataTemp.getIccTaxExemptStatus());
    }
    if (StringUtils.isBlank(data.getSizeCd()) && StringUtils.isNotBlank(dataTemp.getSizeCd())) {
      data.setSizeCd(dataTemp.getSizeCd());
    }
  }

  private void extractAddresses(FindCMRResultModel result, String cmrNo, GEOHandler converter, MassCreateFileRow row, Admin adminTemp)
      throws Exception {
    List<FindCMRRecordModel> cmrs = result.getItems();
    MassCreateAddr addr = null;
    MassCreateAddrPK addrPk = null;
    List<String> updatedFields = null;
    for (FindCMRRecordModel cmr : cmrs) {
      updatedFields = new ArrayList<String>();
      if (getAddr(row, cmr.getCmrAddrTypeCode()) == null) {
        addr = new MassCreateAddr();
        addrPk = new MassCreateAddrPK();
        addrPk.setAddrType(cmr.getCmrAddrTypeCode());
        addrPk.setIterationId(row.getData().getId().getIterationId());
        addrPk.setParReqId(row.getData().getId().getParReqId());
        addrPk.setSeqNo(row.getData().getId().getSeqNo());
        addr.setId(addrPk);
        // addr.setVirtual(true);
        addr.setSapNo(cmr.getCmrAddrSeq());
        row.addAddresses(Collections.singletonList(addr));
        LOG.debug("Adding " + cmr.getCmrAddrTypeCode() + " record");

        LOG.debug("Processing address type " + addr.getId().getAddrType());
        addr = getAddr(row, cmr.getCmrAddrTypeCode());

        if (StringUtils.isBlank(addr.getLandCntry()) && StringUtils.isNotBlank(cmr.getCmrCountryLanded())) {
          addr.setLandCntry(cmr.getCmrCountryLanded());
          updatedFields.add("LAND_CNTRY");
        }
        if (StringUtils.isBlank(addr.getAddrTxt()) && StringUtils.isNotBlank(cmr.getCmrStreetAddress())) {
          addr.setAddrTxt(cmr.getCmrStreetAddress());
          updatedFields.add("ADDR_TXT");
        }
        if (StringUtils.isBlank(addr.getAddrTxt2()) && StringUtils.isNotBlank(cmr.getCmrName4())) {
          addr.setAddrTxt2(cmr.getCmrName4());
          updatedFields.add("ADDR_TXT2");
        }
        if (StringUtils.isBlank(addr.getDept()) && StringUtils.isNotBlank(cmr.getCmrName3())) {
          addr.setDept(cmr.getCmrName3());
          updatedFields.add("DEPT");
        }
        if (StringUtils.isBlank(addr.getCity1()) && StringUtils.isNotBlank(cmr.getCmrCity())) {
          addr.setCity1(cmr.getCmrCity());
          updatedFields.add("CITY1");
        }
        if (StringUtils.isBlank(addr.getStateProv()) && StringUtils.isNotBlank(cmr.getCmrState())) {
          addr.setStateProv(cmr.getCmrState());
          updatedFields.add("STATE_PROV");
        }
        if (StringUtils.isBlank(addr.getCity2()) && StringUtils.isNotBlank(cmr.getCmrCity2())) {
          addr.setCity2(cmr.getCmrCity2());
          updatedFields.add("CITY2");
        }
        if (StringUtils.isBlank(addr.getPostCd()) && StringUtils.isNotBlank(cmr.getCmrPostalCode())) {
          addr.setPostCd(cmr.getCmrPostalCode());
          updatedFields.add("POST_CD");
        }
        if (StringUtils.isBlank(addr.getCustPhone()) && StringUtils.isNotBlank(cmr.getCmrCustPhone())) {
          addr.setCustPhone(cmr.getCmrCustPhone());
          updatedFields.add("CUST_PHONE");
        }
        if (StringUtils.isBlank(addr.getPoBox()) && StringUtils.isNotBlank(cmr.getCmrPOBox())) {
          addr.setPoBox(cmr.getCmrPOBox());
          updatedFields.add("PO_BOX");
        }
        if (StringUtils.isBlank(addr.getPoBoxCity()) && StringUtils.isNotBlank(cmr.getCmrPOBoxCity())) {
          addr.setPoBoxCity(cmr.getCmrPOBoxCity());
          updatedFields.add("PO_BOX_CITY");
        }

        Addr addrTemp = new Addr();
        AddrPK pk = new AddrPK();
        pk.setAddrType(addr.getId().getAddrType());
        addrTemp.setId(pk);
        if (converter != null) {
          converter.setAddressValuesOnImport(addrTemp, adminTemp, cmr, cmrNo);
          if (updatedFields.contains("LAND_CNTRY") && StringUtils.isNotBlank(addrTemp.getLandCntry())) {
            addr.setLandCntry(addrTemp.getLandCntry());
          }
          if (updatedFields.contains("ADDR_TXT") && StringUtils.isNotBlank(addrTemp.getAddrTxt())) {
            addr.setAddrTxt(addrTemp.getAddrTxt());
          }
          if (updatedFields.contains("ADDR_TXT2") && StringUtils.isNotBlank(addrTemp.getAddrTxt2())) {
            addr.setAddrTxt2(addrTemp.getAddrTxt2());
          }
          if (updatedFields.contains("DEPT") && StringUtils.isNotBlank(addrTemp.getDept())) {
            addr.setDept(addrTemp.getDept());
          }
          if (updatedFields.contains("CITY1") && StringUtils.isNotBlank(addrTemp.getCity1())) {
            addr.setCity1(addrTemp.getCity1());
          }
          if (updatedFields.contains("STATE_PROV") && StringUtils.isNotBlank(addrTemp.getStateProv())) {
            addr.setStateProv(addrTemp.getStateProv());
          }
          if (updatedFields.contains("CITY2") && StringUtils.isNotBlank(addrTemp.getCity2())) {
            addr.setCity2(addrTemp.getCity2());
          }
          if (updatedFields.contains("POST_CD") && StringUtils.isNotBlank(addrTemp.getPostCd())) {
            addr.setPostCd(addrTemp.getPostCd());
          }
          if (updatedFields.contains("CUST_PHONE") && StringUtils.isNotBlank(addrTemp.getCustPhone())) {
            addr.setCustPhone(addrTemp.getCustPhone());
          }
          if (updatedFields.contains("PO_BOX") && StringUtils.isNotBlank(addrTemp.getPoBox())) {
            addr.setPoBox(addrTemp.getPoBox());
          }
          if (updatedFields.contains("PO_BOX_CITY") && StringUtils.isNotBlank(addrTemp.getPoBoxCity())) {
            addr.setPoBoxCity(addrTemp.getPoBoxCity());
          }
        }
      }
    } // for
  }

  private MassCreateAddr getAddr(MassCreateFileRow row, String type) {
    for (MassCreateAddr addr : row.getAddresses()) {
      if (type.equals(addr.getId().getAddrType())) {
        return addr;
      }
    }
    return null;
  }

  @Override
  public boolean isCritical() {
    return false;
  }

}
