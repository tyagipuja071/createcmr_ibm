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
 * @author Jeffrey Zamora
 * 
 */
public class CreatebyModelHandler implements RowHandler {

  private static final Logger LOG = Logger.getLogger(CreatebyModelHandler.class);

  @Override
  public RowResult validate(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    if (!row.isCreateByModel()) {
      return RowResult.passed();
    }
    RowResult result = new RowResult();
    if (StringUtils.isEmpty(row.getData().getModelCmrNo())) {
      result.addError("Model CMR No is not specified.");
    }

    MassCreateData data = row.getData();
    String cmrNo = data.getModelCmrNo();
    String cmrIssuingCntry = row.getParentFile().getCmrIssuingCntry();

    FindCMRResultModel resultModel = FindCMRUtil.findCMRs(cmrNo, cmrIssuingCntry);
    FindCMRRecordModel mainRecord = resultModel.getItems().size() > 0 ? resultModel.getItems().get(0) : null;
    if (mainRecord == null) {
      LOG.debug("There are no records found for this CMR");
      result.addError("There are no records found for this CMR.");
      return result;
    }
    GEOHandler geoHandler = RequestUtils.getGEOHandler(cmrIssuingCntry);
    Data dataTemp = new Data();
    Admin adminTemp = new Admin();
    RequestEntryModel mockEntryModel = new RequestEntryModel();
    mockEntryModel.setReqType("C");
    if (geoHandler != null) {
      geoHandler.convertFrom(null, resultModel, mockEntryModel, new ImportCMRModel());
    }
    mainRecord = resultModel.getItems().size() > 0 ? resultModel.getItems().get(0) : null;
    if (mainRecord == null) {
      LOG.debug("There are no valid records found for this CMR");
      result.addError("There are no valid records found for this CMR.");
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
    extractAddresses(resultModel, cmrNo, geoHandler, row);

    return result;
  }

  @Override
  public void transform(EntityManager entityManager, MassCreateFileRow row) throws Exception {
    if (!row.isCreateByModel()) {
      return;
    }
  }

  private void fillData(FindCMRRecordModel mainRecord, MassCreateFileRow row, MassCreateData data, Data dataTemp, Admin adminTemp) {
    if (StringUtils.isBlank(data.getCustNm1()) && !StringUtils.isBlank(adminTemp.getMainCustNm2())) {
      data.setCustNm1(adminTemp.getMainCustNm1());
    }
    if (StringUtils.isBlank(data.getCustNm2()) && !StringUtils.isBlank(adminTemp.getMainCustNm2())) {
      data.setCustNm2(adminTemp.getMainCustNm2());
    }
    if (StringUtils.isBlank(data.getAbbrevNm()) && !StringUtils.isBlank(dataTemp.getAbbrevNm())) {
      data.setAbbrevNm(dataTemp.getAbbrevNm());
      row.addUpdateCol("ABBREV_NM");
      row.mapRawValue("ABBREV_NM", data.getAbbrevNm());
    }
    if (StringUtils.isBlank(data.getAffiliate()) && !StringUtils.isBlank(dataTemp.getAffiliate())) {
      data.setAffiliate(dataTemp.getAffiliate());
    }
    if (StringUtils.isBlank(data.getBpRelType()) && !StringUtils.isBlank(dataTemp.getBpRelType())) {
      data.setBpRelType(dataTemp.getBpRelType());
    }
    if (StringUtils.isBlank(data.getCapInd()) && !StringUtils.isBlank(dataTemp.getCapInd())) {
      data.setCapInd(dataTemp.getCapInd());
    }
    if (StringUtils.isBlank(data.getClientTier()) && !StringUtils.isBlank(dataTemp.getClientTier())) {
      data.setClientTier(dataTemp.getClientTier());
    }
    if (StringUtils.isBlank(data.getCmrIssuingCntry()) && !StringUtils.isBlank(dataTemp.getCmrIssuingCntry())) {
      data.setCmrIssuingCntry(dataTemp.getCmrIssuingCntry());
    }
    if (StringUtils.isBlank(data.getCmrOwner()) && !StringUtils.isBlank(dataTemp.getCmrOwner())) {
      data.setCmrOwner(dataTemp.getCmrOwner());
    }
    if (StringUtils.isBlank(data.getCompany()) && !StringUtils.isBlank(dataTemp.getCompany())) {
      data.setCompany(dataTemp.getCompany());
    }
    if (StringUtils.isBlank(data.getCustClass()) && !StringUtils.isBlank(dataTemp.getCustClass())) {
      data.setCustClass(dataTemp.getCustClass());
    }
    if (StringUtils.isBlank(data.getEnterprise()) && !StringUtils.isBlank(dataTemp.getEnterprise())) {
      data.setEnterprise(dataTemp.getEnterprise());
    }
    if (StringUtils.isBlank(data.getCustPrefLang()) && !StringUtils.isBlank(dataTemp.getCustPrefLang())) {
      data.setCustPrefLang(dataTemp.getCustPrefLang());
    }
    if (StringUtils.isBlank(data.getInacCd()) && !StringUtils.isBlank(dataTemp.getInacCd())) {
      data.setInacCd(dataTemp.getInacCd());
      row.addUpdateCol("INAC_CD");
      row.mapRawValue("INAC_CD", data.getInacCd());
    }
    if (StringUtils.isBlank(data.getInacType()) && !StringUtils.isBlank(dataTemp.getInacType())) {
      data.setInacType(dataTemp.getInacType());
      row.addUpdateCol("INAC_TYPE");
      row.mapRawValue("INAC_TYPE", "I".equals(data.getInacType()) ? "I | INAC" : "N | NAC");
    }
    if (StringUtils.isBlank(data.getIsuCd()) && !StringUtils.isBlank(dataTemp.getIsuCd())) {
      data.setIsuCd(dataTemp.getIsuCd());
      row.addUpdateCol("ISU_CD");
      row.mapRawValue("ISU_CD", data.getIsuCd() + " | " + mainRecord.getIsuDescription());
    }
    if (StringUtils.isBlank(data.getTaxCd1()) && !StringUtils.isBlank(dataTemp.getTaxCd1())) {
      data.setTaxCd1(dataTemp.getTaxCd1());
    }
    if (StringUtils.isBlank(data.getTaxCd2()) && !StringUtils.isBlank(dataTemp.getTaxCd2())) {
      data.setTaxCd2(dataTemp.getTaxCd2());
    }
    if (StringUtils.isBlank(data.getTaxCd3()) && !StringUtils.isBlank(dataTemp.getTaxCd3())) {
      data.setTaxCd3(dataTemp.getTaxCd3());
    }
    if (StringUtils.isBlank(data.getSearchTerm()) && !StringUtils.isBlank(dataTemp.getSearchTerm())) {
      data.setSearchTerm(dataTemp.getSearchTerm());
      row.addUpdateCol("SEARCH_TERM");
      row.mapRawValue("SEARCH_TERM", data.getSearchTerm());
    }
    if (StringUtils.isBlank(data.getMemLvl()) && !StringUtils.isBlank(dataTemp.getMemLvl())) {
      data.setMemLvl(dataTemp.getMemLvl());
    }
    if (StringUtils.isBlank(data.getSensitiveFlag()) && !StringUtils.isBlank(dataTemp.getSensitiveFlag())) {
      data.setSensitiveFlag(dataTemp.getSensitiveFlag());
    }
    if (StringUtils.isBlank(data.getIsicCd()) && !StringUtils.isBlank(dataTemp.getIsicCd())) {
      data.setIsicCd(dataTemp.getIsicCd());
    }
    if (StringUtils.isBlank(data.getSitePartyId()) && !StringUtils.isBlank(dataTemp.getSitePartyId())) {
      data.setSitePartyId(dataTemp.getSitePartyId());
    }
    if (StringUtils.isBlank(data.getSubIndustryCd()) && !StringUtils.isBlank(dataTemp.getSubIndustryCd())) {
      data.setSubIndustryCd(dataTemp.getSubIndustryCd());
    }
    if (StringUtils.isBlank(data.getVat()) && !StringUtils.isBlank(dataTemp.getVat())) {
      data.setVat(dataTemp.getVat());
    }
    if (StringUtils.isBlank(data.getCovId()) && !StringUtils.isBlank(dataTemp.getCovId())) {
      data.setCovId(dataTemp.getCovId());
    }
    if (StringUtils.isBlank(data.getBgId()) && !StringUtils.isBlank(dataTemp.getBgId())) {
      data.setBgId(dataTemp.getBgId());
    }
    if (StringUtils.isBlank(data.getGeoLocCd()) && !StringUtils.isBlank(dataTemp.getGeoLocationCd())) {
      data.setGeoLocCd(dataTemp.getGeoLocationCd());
    }
    if (StringUtils.isBlank(data.getDunsNo()) && !StringUtils.isBlank(dataTemp.getDunsNo())) {
      data.setDunsNo(dataTemp.getDunsNo());
    }
    if (StringUtils.isBlank(data.getRestrictInd()) && !StringUtils.isBlank(dataTemp.getRestrictInd())) {
      data.setRestrictInd(dataTemp.getRestrictInd());
    }
    if (StringUtils.isBlank(data.getRestrictTo()) && !StringUtils.isBlank(dataTemp.getRestrictTo())) {
      data.setRestrictTo(dataTemp.getRestrictTo());
    }
    if (StringUtils.isBlank(data.getOemInd()) && !StringUtils.isBlank(dataTemp.getOemInd())) {
      data.setOemInd(dataTemp.getOemInd());
    }
    if (StringUtils.isBlank(data.getBpAcctTyp()) && !StringUtils.isBlank(dataTemp.getBpAcctTyp())) {
      data.setBpAcctTyp(dataTemp.getBpAcctTyp());
    }
    if (StringUtils.isBlank(data.getMktgDept()) && !StringUtils.isBlank(dataTemp.getMktgDept())) {
      data.setMktgDept(dataTemp.getMktgDept());
    }
    if (StringUtils.isBlank(data.getMtkgArDept()) && !StringUtils.isBlank(dataTemp.getMtkgArDept())) {
      data.setMtkgArDept(dataTemp.getMtkgArDept());
    }
    if (StringUtils.isBlank(data.getPccMktgDept()) && !StringUtils.isBlank(dataTemp.getPccMktgDept())) {
      data.setPccMktgDept(dataTemp.getPccMktgDept());
    }
    if (StringUtils.isBlank(data.getPccArDept()) && !StringUtils.isBlank(dataTemp.getPccArDept())) {
      data.setPccArDept(dataTemp.getPccArDept());
    }
    if (StringUtils.isBlank(data.getSvcArOffice()) && !StringUtils.isBlank(dataTemp.getSvcArOffice())) {
      data.setSvcArOffice(dataTemp.getSvcArOffice());
    }
    if (StringUtils.isBlank(data.getOutCityLimit()) && !StringUtils.isBlank(dataTemp.getOutCityLimit())) {
      data.setOutCityLimit(dataTemp.getOutCityLimit());
    }
    if (StringUtils.isBlank(data.getCsoSite()) && !StringUtils.isBlank(dataTemp.getCsoSite())) {
      data.setCsoSite(dataTemp.getCsoSite());
    }
    if (StringUtils.isBlank(data.getFedSiteInd()) && !StringUtils.isBlank(dataTemp.getFedSiteInd())) {
      data.setFedSiteInd(dataTemp.getFedSiteInd());
    }
    if (StringUtils.isBlank(data.getSizeCd()) && !StringUtils.isBlank(dataTemp.getSizeCd())) {
      data.setSizeCd(dataTemp.getSizeCd());
    }
    if (StringUtils.isBlank(data.getSvcTerritoryZone()) && !StringUtils.isBlank(dataTemp.getSvcTerritoryZone())) {
      data.setSvcTerritoryZone(dataTemp.getSvcTerritoryZone());
    }
    if (StringUtils.isBlank(data.getMiscBillCd()) && !StringUtils.isBlank(dataTemp.getMiscBillCd())) {
      data.setMiscBillCd(dataTemp.getMiscBillCd());
    }
    if (StringUtils.isBlank(data.getBpName()) && !StringUtils.isBlank(dataTemp.getBpName())) {
      data.setBpName(dataTemp.getBpName());
    }
    if (StringUtils.isBlank(data.getIccTaxClass()) && !StringUtils.isBlank(dataTemp.getIccTaxClass())) {
      data.setIccTaxClass(dataTemp.getIccTaxClass());
    }
    if (StringUtils.isBlank(data.getIccTaxExemptStatus()) && !StringUtils.isBlank(dataTemp.getIccTaxExemptStatus())) {
      data.setIccTaxExemptStatus(dataTemp.getIccTaxExemptStatus());
    }
    if (StringUtils.isBlank(data.getNonIbmCompanyInd()) && !StringUtils.isBlank(dataTemp.getNonIbmCompanyInd())) {
      data.setNonIbmCompanyInd(dataTemp.getNonIbmCompanyInd());
    }
    if (StringUtils.isBlank(data.getDiv()) && !StringUtils.isBlank(dataTemp.getDiv())) {
      data.setDiv(dataTemp.getDiv());
    }
    if (StringUtils.isBlank(data.getDept()) && !StringUtils.isBlank(dataTemp.getDept())) {
      data.setDept(dataTemp.getDept());
    }
    if (StringUtils.isBlank(data.getFunc()) && !StringUtils.isBlank(dataTemp.getFunc())) {
      data.setFunc(dataTemp.getFunc());
    }
    if (StringUtils.isBlank(data.getUser()) && !StringUtils.isBlank(dataTemp.getUser())) {
      data.setUser(dataTemp.getUser());
    }
    if (StringUtils.isBlank(data.getLoc()) && !StringUtils.isBlank(dataTemp.getLoc())) {
      data.setLoc(dataTemp.getLoc());
    }
    if (StringUtils.isBlank(data.getOrdBlk()) && !StringUtils.isBlank(dataTemp.getOrdBlk())) {
      data.setOrdBlk(dataTemp.getOrdBlk());
    }
    if (StringUtils.isBlank(data.getBgRuleId()) && !StringUtils.isBlank(dataTemp.getBgRuleId())) {
      data.setBgRuleId(dataTemp.getBgRuleId());
    }
    if (StringUtils.isBlank(data.getCovDesc()) && !StringUtils.isBlank(dataTemp.getCovDesc())) {
      data.setCovDesc(dataTemp.getCovDesc());
    }
    if (StringUtils.isBlank(data.getBgDesc()) && !StringUtils.isBlank(dataTemp.getBgDesc())) {
      data.setBgDesc(dataTemp.getBgDesc());
    }
    if (StringUtils.isBlank(data.getGeoLocDesc()) && !StringUtils.isBlank(dataTemp.getGeoLocDesc())) {
      data.setGeoLocDesc(dataTemp.getGeoLocDesc());
    }
    if (StringUtils.isBlank(data.getGbgId()) && !StringUtils.isBlank(dataTemp.getGbgId())) {
      data.setGbgId(dataTemp.getGbgId());
    }
    if (StringUtils.isBlank(data.getGbgDesc()) && !StringUtils.isBlank(dataTemp.getGbgDesc())) {
      data.setGbgDesc(dataTemp.getGbgDesc());
    }
  }

  private void extractAddresses(FindCMRResultModel result, String cmrNo, GEOHandler converter, MassCreateFileRow row) throws Exception {
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
        addr.setVirtual(true);
        row.addAddresses(Collections.singletonList(addr));
        LOG.debug("Adding " + cmr.getCmrAddrTypeCode() + " record");

        LOG.debug("Processing address type " + addr.getId().getAddrType());

        addr = getAddr(row, cmr.getCmrAddrTypeCode());

        if (StringUtils.isBlank(addr.getCity1()) && !StringUtils.isBlank(cmr.getCmrCity())) {
          addr.setCity1(cmr.getCmrCity());
          updatedFields.add("CITY1");
        }
        if (StringUtils.isBlank(addr.getStateProv()) && !StringUtils.isBlank(cmr.getCmrState())) {
          addr.setStateProv(cmr.getCmrState());
          updatedFields.add("STATE_PROV");
        }
        if (StringUtils.isBlank(addr.getPostCd()) && !StringUtils.isBlank(cmr.getCmrPostalCode())) {
          addr.setPostCd(cmr.getCmrPostalCode());
          if (row.getRawValue(addr.getId().getAddrType() + "-CITY1") != null
              && !StringUtils.isEmpty(row.getRawValue(addr.getId().getAddrType() + "-CITY1").toString())) {
            row.mapRawValue(addr.getId().getAddrType() + "-POST_CD", addr.getPostCd());
            row.addUpdateCol(addr.getId().getAddrType() + "-POST_CD");
          }
          updatedFields.add("POST_CD");
        }
        if (StringUtils.isBlank(addr.getLandCntry()) && !StringUtils.isBlank(cmr.getCmrCountryLanded())) {
          addr.setLandCntry(cmr.getCmrCountryLanded());
          updatedFields.add("LAND_CNTRY");
        }
        if (StringUtils.isBlank(addr.getCounty()) && !StringUtils.isBlank(cmr.getCmrCounty())) {
          addr.setCounty(cmr.getCmrCounty());
          updatedFields.add("COUNTY");
        }
        if (StringUtils.isBlank(addr.getAddrTxt()) && !StringUtils.isBlank(cmr.getCmrStreetAddress())) {
          addr.setAddrTxt(cmr.getCmrStreetAddress());
          updatedFields.add("ADDR_TXT");
        }
        if (StringUtils.isBlank(addr.getDept()) && !StringUtils.isBlank(cmr.getCmrDept())) {
          addr.setDept(cmr.getCmrDept());
          updatedFields.add("DEPT");
        }

        Addr addrTemp = new Addr();
        AddrPK pk = new AddrPK();
        pk.setAddrType(addr.getId().getAddrType());
        addrTemp.setId(pk);
        if (converter != null) {
          converter.setAddressValuesOnImport(addrTemp, null, cmr, cmrNo);
          if (updatedFields.contains("CITY1") && !StringUtils.isBlank(addrTemp.getCity1())) {
            addr.setCity1(addrTemp.getCity1());
          }
          if (updatedFields.contains("STATE_PROV") && !StringUtils.isBlank(addrTemp.getStateProv())) {
            addr.setStateProv(addrTemp.getStateProv());
          }
          if (updatedFields.contains("POST_CD") && !StringUtils.isBlank(addrTemp.getPostCd())) {
            addr.setPostCd(addrTemp.getPostCd());
          }
          if (updatedFields.contains("ADDR_TXT") && !StringUtils.isBlank(addrTemp.getAddrTxt())) {
            addr.setAddrTxt(addrTemp.getAddrTxt());
          }
          if (updatedFields.contains("DEPT") && !StringUtils.isBlank(addrTemp.getDept())) {
            addr.setDept(addrTemp.getDept());
          }
          if (StringUtils.isBlank(addr.getDivn()) && !StringUtils.isBlank(addrTemp.getDivn())) {
            addr.setDivn(addrTemp.getDivn());
          }
          if (StringUtils.isBlank(addr.getAddrTxt2()) && !StringUtils.isBlank(addrTemp.getAddrTxt2())) {
            addr.setAddrTxt2(addrTemp.getAddrTxt2());
          }
        }

      }

    }
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
