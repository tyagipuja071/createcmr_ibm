package com.ibm.cio.cmr.request.util.geo.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.digester.Digester;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.util.AutomationUtil;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.controller.DropdownListController;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.entity.DataRdc;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.model.window.UpdatedDataModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.service.window.RequestSummaryService;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.ConfigUtil;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.MessageUtil;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.wtaas.WtaasAddress;
import com.ibm.cio.cmr.request.util.wtaas.WtaasQueryKeys;
import com.ibm.cio.cmr.request.util.wtaas.WtaasRecord;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.WtaasClient;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;
import com.ibm.cmr.services.client.wtaas.WtaasQueryRequest;
import com.ibm.cmr.services.client.wtaas.WtaasQueryResponse;

/**
 * Import Converter for AP
 * 
 */
public abstract class APHandler extends GEOHandler {

  private static final Logger LOG = Logger.getLogger(APHandler.class);
  private static final String[] AP_SKIP_ON_SUMMARY_UPDATE_FIELDS = { "SearchTerm", "TransportZone" };
  protected WtaasRecord currentRecord;
  private static List<IndiaProvCdStateCityMapping> provCdArCdMappings = new ArrayList<IndiaProvCdStateCityMapping>();

  @SuppressWarnings("unchecked")
  public APHandler() {

    if (APHandler.provCdArCdMappings.isEmpty()) {
      Digester digester = new Digester();

      digester.setValidating(false);

      digester.addObjectCreate("mappings", ArrayList.class);
      digester.addObjectCreate("mappings/mapping", IndiaProvCdStateCityMapping.class);

      digester.addBeanPropertySetter("mappings/mapping/city", "city");
      digester.addBeanPropertySetter("mappings/mapping/state", "state");
      digester.addBeanPropertySetter("mappings/mapping/provinceCd", "provinceCd");
      digester.addBeanPropertySetter("mappings/mapping/arCode", "arCode");

      digester.addSetNext("mappings/mapping", "add");

      try {
        InputStream is = ConfigUtil.getResourceStream("india_prov_state_city.xml");
        APHandler.provCdArCdMappings = (ArrayList<IndiaProvCdStateCityMapping>) digester.parse(is);

      } catch (Exception e) {
        LOG.error("Error occured while digesting xml.", e);
      }
    }

  }

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    LOG.debug("Converting search results to WTAAS equivalent..");
    FindCMRRecordModel mainRecord = source.getItems() != null && !source.getItems().isEmpty() ? source.getItems().get(0) : null;
    if (mainRecord != null) {
      boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
      if (!prospectCmrChosen) {
        this.currentRecord = retrieveWTAASValues(mainRecord);
        if (this.currentRecord != null) {

          List<WtaasAddress> wtaasAddresses = this.currentRecord.uniqueAddresses();

          if (wtaasAddresses != null && !wtaasAddresses.isEmpty()) {

            String zs01AddressUse = getZS01AddressUse(mainRecord.getCmrIssuedBy());
            FindCMRRecordModel record = null;
            List<FindCMRRecordModel> converted = new ArrayList<FindCMRRecordModel>();
            // import only ZS01 from RDC for Create request
            if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
              LOG.debug(" - Main address, importing from FindCMR main record.");
              // will import ZS01 from RDc directly
              record = mainRecord;
              if (StringUtils.isEmpty(mainRecord.getCmrAddrSeq())) {
                record.setCmrAddrSeq("00001");
              } else {
                record.setCmrAddrSeq(StringUtils.leftPad(mainRecord.getCmrAddrSeq(), 5, '0'));
              }
              handleRDcRecordValues(record);
              converted.add(record);
            } else {
              // only create the actual number of addresses in wtaas
              for (WtaasAddress wtaasAddress : wtaasAddresses) {
                LOG.debug("WTAAS Addr No: " + wtaasAddress.getAddressNo() + " Use: " + wtaasAddress.getAddressUse());
                if (!wtaasAddress.getAddressUse().contains(zs01AddressUse)) {
                  LOG.debug(" - Additional address, checking RDC equivalent..");
                  record = locateRdcRecord(source, wtaasAddress);

                  if (record != null) {
                    LOG.debug(" - RDC equivalent found with type = " + record.getCmrAddrTypeCode() + " seq = " + record.getCmrAddrSeq()
                        + ", using directly");
                    handleRDcRecordValues(record);
                  } else {
                    // create a copy of the main record, to preserve data fields
                    LOG.debug(" - not found. parsing WTAAS data..");
                    record = new FindCMRRecordModel();
                    PropertyUtils.copyProperties(record, mainRecord);

                    // clear the address values
                    record.setCmrName1Plain(null);
                    record.setCmrName2Plain(null);
                    record.setCmrName3(null);
                    record.setCmrName4(null);
                    record.setCmrDept(null);
                    record.setCmrCity(null);
                    record.setCmrState(null);
                    record.setCmrCountryLanded(null);
                    record.setCmrStreetAddress(null);
                    record.setCmrStreetAddressCont(null);
                    record.setCmrSapNumber(null);

                    // handle here the different mappings
                    handleWTAASAddressImport(entityManager, record.getCmrIssuedBy(), mainRecord, record, wtaasAddress);
                    String supportedAddrType = getAddrTypeForWTAASAddrUse(record.getCmrIssuedBy(), wtaasAddress.getAddressUse());
                    if (supportedAddrType != null)
                      record.setCmrAddrTypeCode(supportedAddrType);
                  }
                  record.setCmrAddrSeq(wtaasAddress.getAddressNo());

                  if ("MAIL".equals(record.getCmrAddrTypeCode())) {
                    continue;
                  }

                  if (shouldAddWTAASAddess(record.getCmrIssuedBy(), wtaasAddress)) {
                    converted.add(record);
                  }
                } else {
                  LOG.debug(" - Main address, importing from FindCMR main record.");
                  // will import ZS01 from RDc directly
                  mainRecord.setCmrAddrSeq(wtaasAddress.getAddressNo());
                  handleRDcRecordValues(mainRecord);
                  converted.add(mainRecord);
                }
              }
            }

            doAfterConvert(entityManager, source, reqEntry, searchModel, converted);
            Collections.sort(converted);
            source.setItems(converted);
          }
        }
      } else {
        FindCMRRecordModel record = null;
        List<FindCMRRecordModel> converted = new ArrayList<FindCMRRecordModel>();
        LOG.debug(" - Main address, importing from FindCMR main record.");
        // will import ZS01 from RDc directly
        record = mainRecord;
        if (StringUtils.isEmpty(mainRecord.getCmrAddrSeq())) {
          record.setCmrAddrSeq("00001");
        } else {
          record.setCmrAddrSeq(StringUtils.leftPad(mainRecord.getCmrAddrSeq(), 5, '0'));
        }
        handleRDcRecordValues(record);
        converted.add(record);
        doAfterConvert(entityManager, source, reqEntry, searchModel, converted);
        Collections.sort(converted);
        source.setItems(converted);
      }

    }
  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    boolean prospectCmrChosen = mainRecord != null && CmrConstants.PROSPECT_ORDER_BLOCK.equals(mainRecord.getCmrOrderBlock());
    if (!prospectCmrChosen) {
      data.setApCustClusterId(this.currentRecord.get(WtaasQueryKeys.Data.ClusterNo));
      data.setRepTeamMemberNo(this.currentRecord.get(WtaasQueryKeys.Data.SalesmanNo));
      data.setCollectionCd(this.currentRecord.get(WtaasQueryKeys.Data.IBMCode));
      data.setIsbuCd(this.currentRecord.get(WtaasQueryKeys.Data.SellDept));
      data.setClientTier(this.currentRecord.get(WtaasQueryKeys.Data.GB_SegCode));
      data.setMrcCd(this.currentRecord.get(WtaasQueryKeys.Data.MrktRespCode));
      data.setTerritoryCd(this.currentRecord.get(WtaasQueryKeys.Data.SellBrnchOff));
      data.setAbbrevLocn(this.currentRecord.get(WtaasQueryKeys.Data.AbbrLoc));
      // data.setIsuCd(mainRecord.getCmrIsu());
      data.setBusnType(this.currentRecord.get(WtaasQueryKeys.Data.SellBrnchOff));
      data.setGovType(this.currentRecord.get(WtaasQueryKeys.Data.GovCustInd));
      data.setMiscBillCd(this.currentRecord.get(WtaasQueryKeys.Data.RegionCode));
    } else {
      data.setCmrNo("");
    }

    autoSetAbbrevLocnNMOnImport(admin, data, results, mainRecord);
    data.setIsuCd(mainRecord.getCmrIsu());

    if (!prospectCmrChosen) {
      System.out.println("Value of Province Code is >>> " + this.currentRecord.get(WtaasQueryKeys.Data.SellBrnchOff));
      System.out.println("Value of MRC Cd  is >>> " + this.currentRecord.get(WtaasQueryKeys.Data.MrktRespCode));
      System.out.println("Value of Abbr Loc is >>> " + this.currentRecord.get(WtaasQueryKeys.Data.AbbrLoc));
      System.out.println("Value of Coll Cd  is >>> " + this.currentRecord.get(WtaasQueryKeys.Data.IBMCode));
      System.out.println("Value of Cluster Code is >>> " + this.currentRecord.get(WtaasQueryKeys.Data.ClusterNo));
      System.out.println("Value of GB Seg Code is >>> " + this.currentRecord.get(WtaasQueryKeys.Data.GB_SegCode));
      System.out.println("Value of Sales Rep Code is >>> " + this.currentRecord.get(WtaasQueryKeys.Data.SalesmanNo));
      System.out.println("Value of Gov Cus Indc >>> " + this.currentRecord.get(WtaasQueryKeys.Data.GovCustInd));
      System.out.println("value of ISBU Cd is >>> " + this.currentRecord.get(WtaasQueryKeys.Data.SellDept));
      System.out.println("value of Region Code is >>> " + this.currentRecord.get(WtaasQueryKeys.Data.RegionCode));
    }

    if (mainRecord.getCmrCountryLandedDesc().isEmpty() && !prospectCmrChosen)
      data.setAbbrevLocn(this.currentRecord.get(WtaasQueryKeys.Data.AbbrLoc));
    // fix Defect 1732232: Abbreviated Location issue
    if (!StringUtils.isBlank(data.getAbbrevLocn()) && data.getAbbrevLocn().length() > 9)
      data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 9));

    LOG.debug("SalesmanNo: " + data.getRepTeamMemberNo());
    LOG.debug("AbbrLoc: " + data.getAbbrevLocn());
    LOG.debug("IBMCode: " + data.getCollectionCd());
    LOG.debug("Cluster: " + data.getApCustClusterId());
    LOG.debug("ISBU Cd/ SellDept: " + data.getIsbuCd());
    LOG.debug("GbSeg Code / Client Tier: " + data.getClientTier());
    LOG.debug("MrktRespCode: " + data.getMrcCd());
    LOG.debug("Province Code/SellBrnchOff : " + data.getTerritoryCd());

  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType())) {
      admin.setOldCustNm1(currentRecord.getCmrName1Plain());
      admin.setOldCustNm2(currentRecord.getCmrName2Plain());
    }
  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    if (currentRecord != null) {
      // String[] names = splitName(currentRecord.getCmrName1Plain(),
      // currentRecord.getCmrName2Plain(), 30, 30);
      address.setCustNm1(currentRecord.getCmrName1Plain());
      address.setCustNm2(currentRecord.getCmrName2Plain());
      address.setAddrTxt(currentRecord.getCmrStreetAddress());
      address.setAddrTxt2(currentRecord.getCmrStreetAddressCont());
      address.setDept(currentRecord.getCmrDept());
      if ("D".equals(address.getImportInd())) {
        address.setAddrTxt(currentRecord.getCmrStreet());
      }

      // data cleanup
      if (!StringUtils.isEmpty(address.getPostCd()) && address.getPostCd().contains(",")) {
        address.setPostCd(address.getPostCd().replaceAll(",", ""));
      }
      if (!StringUtils.isEmpty(address.getAddrTxt()) && address.getAddrTxt().trim().endsWith(",")) {
        address.setAddrTxt(address.getAddrTxt().trim().substring(0, address.getAddrTxt().trim().length() - 1));
      }
      if (!StringUtils.isEmpty(address.getAddrTxt2()) && address.getAddrTxt2().trim().endsWith(",")) {
        address.setAddrTxt2(address.getAddrTxt2().trim().substring(0, address.getAddrTxt2().trim().length() - 1));
      }
      if (!StringUtils.isEmpty(address.getCity1()) && address.getCity1().contains(",")) {
        address.setCity1(address.getCity1().replaceAll(",", ""));
      }
      // setAddressSeqNo(address, currentRecord);

    }
  }

  // private void setAddressSeqNo(Addr address, FindCMRRecordModel
  // currentRecord) {
  // if (currentRecord.getCmrIssuedBy() != null &&
  // currentRecord.getCmrIssuedBy().equals(SystemLocation.AUSTRALIA)
  // || currentRecord.getCmrCountryLanded() != null &&
  // currentRecord.getCmrCountryLanded().equals(SystemLocation.AUSTRALIA_COUNTRY))
  // {
  // address.getId().setAddrSeq("B");
  // } else if (currentRecord.getCmrIssuedBy() != null
  // && (currentRecord.getCmrIssuedBy().equals(SystemLocation.NEW_ZEALAND) ||
  // currentRecord.getCmrIssuedBy().equals(SystemLocation.HONG_KONG) ||
  // currentRecord
  // .getCmrIssuedBy().equals(SystemLocation.MACAO))
  // || currentRecord.getCmrCountryLanded() != null
  // &&
  // (currentRecord.getCmrCountryLanded().equals(SystemLocation.NEW_ZEALAND_COUNTRY)
  // ||
  // currentRecord.getCmrCountryLanded().equals(SystemLocation.HONG_KONG_COUNTRY)
  // || currentRecord.getCmrCountryLanded().equals(
  // SystemLocation.MACAO_COUNTRY))) {
  // address.getId().setAddrSeq("3");
  // } else {
  // address.getId().setAddrSeq("1");
  // }
  // }

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
  public void addSummaryUpdatedFields(RequestSummaryService service, String type, String cmrCountry, Data newData, DataRdc oldData,
      List<UpdatedDataModel> results) {
    UpdatedDataModel update = null;

    if (RequestSummaryService.TYPE_CUSTOMER.equals(type) && !equals(oldData.getAbbrevLocn(), newData.getAbbrevLocn())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "AbbrevLocation", "-"));
      update.setNewData(newData.getAbbrevLocn());
      update.setOldData(oldData.getAbbrevLocn());
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getRepTeamMemberNo(), newData.getRepTeamMemberNo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "SalRepNameNo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getRepTeamMemberNo(), "SalRepNameNo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getRepTeamMemberNo(), "SalRepNameNo", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getCollectionCd(), newData.getCollectionCd())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "CollectionCd", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getCollectionCd(), "CollectionCd", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getCollectionCd(), "CollectionCd", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getEngineeringBo(), newData.getEngineeringBo())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "EngineeringBo", "-"));
      update.setNewData(service.getCodeAndDescription(newData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      update.setOldData(service.getCodeAndDescription(oldData.getEngineeringBo(), "EngineeringBo", cmrCountry));
      results.add(update);
    }
    if (RequestSummaryService.TYPE_IBM.equals(type) && !equals(oldData.getApCustClusterId(), newData.getApCustClusterId())) {
      update = new UpdatedDataModel();
      update.setDataField(PageManager.getLabel(cmrCountry, "Cluster", "-"));
      update.setNewData(newData.getApCustClusterId());
      update.setOldData(oldData.getApCustClusterId());
      results.add(update);
    }
  }

  public String getCodeAndDescription(String code, String fieldId, String cntry) {
    String desc = DropdownListController.getDescription(fieldId, code, cntry);
    if (!StringUtils.isEmpty(desc)) {
      return desc;
    }
    return code;
  }

  public DataRdc getAPClusterDataRdc(long reqId) {
    String sql = ExternalizedQuery.getSql("SUMMARY.OLDDATA");
    EntityManager entityManager = JpaManager.getEntityManager();
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setForReadOnly(true);
    List<DataRdc> records = query.getResults(DataRdc.class);
    if (records != null && records.size() > 0) {
      for (DataRdc oldData : records) {
        return oldData;
      }
    }
    return null;
  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
    request.setSORTL(data.getApCustClusterId());
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
    if (cmrIssuingCntry.equalsIgnoreCase(SystemLocation.HONG_KONG) || cmrIssuingCntry.equalsIgnoreCase(SystemLocation.MACAO)) {
      if (data.getMrcCd() != null && data.getApCustClusterId() != null && data.getCustSubGrp() != null && data.getClientTier() != null) {
        List<String> codes = Arrays.asList("03756", "03757", "03758", "03759", "04249", "05485", "05486");
        if ("3".equalsIgnoreCase(data.getMrcCd())
            && ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "00568".equalsIgnoreCase(data.getApCustClusterId()))) {
          data.setIsuCd("32");
        } else if ("3".equalsIgnoreCase(data.getMrcCd())
            && (codes.contains(data.getApCustClusterId()) || "BUSPR".equalsIgnoreCase(data.getCustSubGrp()))) {
          data.setIsuCd("34");
        } else if ("2".equalsIgnoreCase(data.getMrcCd())
            && ("00000".equalsIgnoreCase(data.getApCustClusterId()) && "Z".equalsIgnoreCase(data.getClientTier())
                || "INTER".equalsIgnoreCase(data.getCustSubGrp()) || "DUMMY".equalsIgnoreCase(data.getCustSubGrp()))) {
          data.setIsuCd("21");

        } else if (null == data.getIsuCd() || "".equals(data.getIsuCd())) {
          data.setIsuCd("21");
        }
      }
    }

    String sql = ExternalizedQuery.getSql("DNB.GET_CURR_SOLD_TO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    query.setForReadOnly(true);
    Addr soldTo = query.getSingleResult(Addr.class);
    if (soldTo != null) {
      setAbbrevLocNMBeforeAddrSave(entityManager, soldTo, data.getCmrIssuingCntry());
    }
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    setAbbrevLocNMBeforeAddrSave(entityManager, addr, cmrIssuingCntry);
    // call only for zs01
    setProvNameCdFrmCityState(entityManager, addr);
  }

  private void setProvNameCdFrmCityState(EntityManager entityManager, Addr addr) {
    DataPK dataPK = new DataPK();
    dataPK.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, dataPK);
    boolean matchFound = false;
    if (!APHandler.provCdArCdMappings.isEmpty()) {
      // first check if any city matches
      for (IndiaProvCdStateCityMapping mapping : provCdArCdMappings) {
        String mappingCity = AutomationUtil.getCleanString(mapping.getCity());
        String requestCity = StringUtils.isNotEmpty(addr.getCity1()) ? addr.getCity1().toUpperCase() : "";
        if (mappingCity.contains(requestCity)) {
          data.setBusnType(mapping.getProvinceCd());
          data.setCollectionCd(mapping.getArCode());
          matchFound = true;
          break;
        }
      }

      // then check if any state matches
      if (!matchFound) {
        for (IndiaProvCdStateCityMapping mapping : provCdArCdMappings) {
          String mappingState = AutomationUtil.getCleanString(mapping.getState());
          String requestState = getStateDesc(entityManager, addr.getStateProv());
          if (mappingState.contains(requestState)) {
            data.setBusnType(mapping.getProvinceCd());
            data.setCollectionCd(mapping.getArCode());
            break;
          }
        }
      }
      entityManager.persist(data);
    }
  }

  private static String getStateDesc(EntityManager em, String state) {
    String sql = ExternalizedQuery.getSql("AUTO.GET_STATE_DESCRIP");
    PreparedQuery query = new PreparedQuery(em, sql);
    query.setParameter("STATE_PROV_CD", state);
    String stateDesc = query.getSingleResult(String.class);
    if (StringUtils.isNotBlank(stateDesc)) {
      state = stateDesc.toUpperCase();
    }
    return state;
  }

  private void setAbbrevLocNMBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) {
    DataPK dataPK = new DataPK();
    dataPK.setReqId(addr.getId().getReqId());
    Data data = entityManager.find(Data.class, dataPK);

    AdminPK adminPK = new AdminPK();
    adminPK.setReqId(addr.getId().getReqId());
    Admin admin = entityManager.find(Admin.class, adminPK);
    // Need to check if ZS01
    if (addr.getId().getAddrType().equals("ZS01")) {
      // set Abb Location
      switch (cmrIssuingCntry) {
      case SystemLocation.INDIA:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("IN")) {
            data.setAbbrevLocn("India");
          }
          if (!addr.getLandCntry().equalsIgnoreCase("IN")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("IGF".equalsIgnoreCase(data.getCustSubGrp()) || "XIGF".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "IGF");
        } else if ("INTER".equalsIgnoreCase(data.getCustSubGrp()) || "XINT".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "IBM INDIA PVT LTD");
        } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "BLUEMIX");
        } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "MARKETPLACE");
        } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "SOFTLAYER USE ONLY");
        } else if ("AQSTN".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Acquisition Use Only");
        } else if ("ESOSW".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA Use Only");
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.SRI_LANKA:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("LK")) {
            data.setAbbrevLocn("Sri Lanka");
          }
          if (!addr.getLandCntry().equalsIgnoreCase("LK")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("INTER".equalsIgnoreCase(data.getCustSubGrp()) || "XINT".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "IBM INDIA PVT LTD");
        } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "BLUEMIX");
        } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Marketplace Use Only");
        } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Softlayer Use Only");
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.BANGLADESH:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("BD")) {
            data.setAbbrevLocn("Bangladesh");
          }
          if (!addr.getLandCntry().equalsIgnoreCase("BD")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("INTER".equalsIgnoreCase(data.getCustSubGrp()) || "XINT".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "IBM INDIA PVT LTD");
        } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "BLUEMIX");
        } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Marketplace Use Only");
        } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Softlayer Use Only");
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.NEPAL:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("NP")) {
            data.setAbbrevLocn("Nepal");
          }
          if (!addr.getLandCntry().equalsIgnoreCase("NP")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        setAbbrevNM(data, addr.getCustNm1());
        break;
      case SystemLocation.SINGAPORE:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("SG")
              || (!"SPOFF".equalsIgnoreCase(data.getCustSubGrp()) && !"CROSS".equalsIgnoreCase(data.getCustSubGrp()))) {
            data.setAbbrevLocn("Singapore");
          }
          if (!addr.getLandCntry().equalsIgnoreCase("SG") || "SPOFF".equalsIgnoreCase(data.getCustSubGrp())) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "IGF INTERNAL_" + addr.getCustNm1());
        } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "BLUEMIX_" + addr.getCustNm1());
        } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "MARKET PLACE_" + addr.getCustNm1());
        } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA_" + addr.getCustNm1());
        } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Softlayer_" + addr.getCustNm1());
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.MALAYSIA:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("MY")) {
            if (addr.getStateProv() != null) {
              data.setAbbrevLocn(addr.getStateProv());
            }
            if (addr.getCity1() != null) {
              data.setAbbrevLocn(addr.getCity1());
            }
          }
          if (!addr.getLandCntry().equalsIgnoreCase("MY")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "IGF INTERNAL_" + addr.getCustNm1());
        } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA_" + addr.getCustNm1());
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.BRUNEI:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("BN")) {
            data.setAbbrevLocn("Brunei");
          }
          if (!addr.getLandCntry().equalsIgnoreCase("BN")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "IGF INTERNAL_" + addr.getCustNm1());
        } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA_" + addr.getCustNm1());
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.VIETNAM:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("VN")) {
            if (addr.getStateProv() != null) {
              data.setAbbrevLocn(addr.getStateProv());
            }
            if (addr.getCity1() != null) {
              data.setAbbrevLocn(addr.getCity1());
            }
          }
          if (!addr.getLandCntry().equalsIgnoreCase("VN")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "IGF INTERNAL_" + addr.getCustNm1());
        } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA_" + addr.getCustNm1());
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.INDONESIA:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("ID")) {
            if (addr.getStateProv() != null) {
              data.setAbbrevLocn(addr.getStateProv());
            }
            if (addr.getCity1() != null) {
              data.setAbbrevLocn(addr.getCity1());
            }
          }
          if (!addr.getLandCntry().equalsIgnoreCase("ID")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "IGF INTERNAL_" + addr.getCustNm1());
        } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA_" + addr.getCustNm1());
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.PHILIPPINES:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("PH")) {
            if (addr.getStateProv() != null) {
              data.setAbbrevLocn(addr.getStateProv());
            }
            if (addr.getCity1() != null) {
              data.setAbbrevLocn(addr.getCity1());
            }
          }
          if (!addr.getLandCntry().equalsIgnoreCase("PH")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "IGF INTERNAL_" + addr.getCustNm1());
        } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA_" + addr.getCustNm1());
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.THAILAND:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("TH")) {
            if (addr.getStateProv() != null) {
              data.setAbbrevLocn(addr.getStateProv());
            }
            if (addr.getCity1() != null) {
              data.setAbbrevLocn(addr.getCity1());
            }
          }
          if (!addr.getLandCntry().equalsIgnoreCase("TH")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "IGF INTERNAL_" + addr.getCustNm1());
        } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA_" + addr.getCustNm1());
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.HONG_KONG:
        if (admin.getReqType().equals("C")) {
          data.setAbbrevLocn("00 HK");
        }
        if ("AQSTN".equalsIgnoreCase(data.getCustSubGrp()) || "XAQST".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Acquisition use only");
        } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA use only");
        } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Consumer only");
        } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Market Place Order");
        } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Softlayer use only");
        } else if ("BUSPR".equalsIgnoreCase(data.getCustSubGrp()) || "XBUSP".equalsIgnoreCase(data.getCustSubGrp())) {
          // do nothing - don't overwrite abbv name
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.MACAO:
        if (admin.getReqType().equals("C")) {
          data.setAbbrevLocn("00 HK");
        }
        if ("AQSTN".equalsIgnoreCase(data.getCustSubGrp()) || "XAQST".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Acquisition use only");
        } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA use only");
        } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Consumer only");
        } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Market Place Order");
        } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Softlayer use only");
        } else if ("BUSPR".equalsIgnoreCase(data.getCustSubGrp()) || "XBUSP".equalsIgnoreCase(data.getCustSubGrp())) {
          // do nothing - don't overwrite abbv name
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.MYANMAR:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("MM")) {
            data.setAbbrevLocn("Myanmar");
          }
          if (!addr.getLandCntry().equalsIgnoreCase("MM")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "DUMMY_" + addr.getCustNm1());
        } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA_" + addr.getCustNm1());
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.LAOS:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("LA")) {
            data.setAbbrevLocn("Laos");
          }
          if (!addr.getLandCntry().equalsIgnoreCase("LA")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "DUMMY_" + addr.getCustNm1());
        } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA_" + addr.getCustNm1());
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.CAMBODIA:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("KH")) {
            data.setAbbrevLocn("Cambodia");
          }
          if (!addr.getLandCntry().equalsIgnoreCase("KH")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "DUMMY_" + addr.getCustNm1());
        } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "ESA_" + addr.getCustNm1());
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.AUSTRALIA:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("AU")) {
            if (addr.getStateProv() != null) {
              data.setAbbrevLocn(addr.getStateProv());
            }
            if (addr.getCity1() != null) {
              data.setAbbrevLocn(addr.getCity1());
            }
          }
          if (!addr.getLandCntry().equalsIgnoreCase("AU")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Bluemix Use Only");
        } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Marketplace Use Only");
        } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Softlayer Use Only");
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      case SystemLocation.NEW_ZEALAND:
        if (admin.getReqType().equals("C")) {
          if (addr.getLandCntry().equalsIgnoreCase("NZ")) {
            if (addr.getStateProv() != null) {
              data.setAbbrevLocn(addr.getStateProv());
            }
            if (addr.getCity1() != null) {
              data.setAbbrevLocn(addr.getCity1());
            }
          }
          if (!addr.getLandCntry().equalsIgnoreCase("NZ")) {
            data.setAbbrevLocn(getLandCntryDesc(entityManager, addr.getLandCntry()));
          }
        }
        if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Bluemix Use Only");
        } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Marketplace Use Only");
        } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
          setAbbrevNM(data, "Softlayer Use Only");
        } else {
          setAbbrevNM(data, addr.getCustNm1());
        }
        break;
      }
      if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 12 && (cmrIssuingCntry.equals("616") || cmrIssuingCntry.equals("796"))) {
        data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 12));
      }
      if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 9 && !(cmrIssuingCntry.equals("616") || cmrIssuingCntry.equals("796"))) {
        data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 9));
      }
    }
    entityManager.merge(data);
    entityManager.flush();
  }

  private String getLandCntryDesc(EntityManager entityManager, String landCntryCd) {
    String landCntryDesc = null;
    String sql = ExternalizedQuery.getSql("ADDRESS.GETCNTRY");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("COUNTRY", landCntryCd);
    List<String> results = query.getResults(String.class);
    if (results != null && !results.isEmpty()) {
      landCntryDesc = results.get(0);
    }
    entityManager.flush();
    return landCntryDesc;
  }

  @Override
  public boolean customerNamesOnAddress() {
    return true;
  }

  @Override
  public boolean useSeqNoFromImport() {
    return true;
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    return Arrays.asList(AP_SKIP_ON_SUMMARY_UPDATE_FIELDS).contains(field);
  }

  /**
   * Checks absolute equality between the strings
   * 
   * @param val1
   * @param val2
   * @return
   */
  private boolean equals(String val1, String val2) {
    if (val1 == null && val2 != null) {
      return StringUtils.isEmpty(val2.trim());
    }
    if (val1 != null && val2 == null) {
      return StringUtils.isEmpty(val1.trim());
    }
    if (val1 == null && val2 == null) {
      return true;
    }
    return val1.trim().equals(val2.trim());
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) {
    setSector(admin, data);
    setMRC(admin, data);
    setISBU(admin, data);

  }

  private boolean expiredSearchTerm(EntityManager entityManager, String searchTerm, String cmrIssuingCntry) {
    if (!StringUtils.isEmpty(searchTerm)) {
      if (!existInList(entityManager, searchTerm, cmrIssuingCntry)) {
        return true;
      }
    }
    return false;
  }

  private boolean existInList(EntityManager entityManager, String searchTerm, String cmrIssuingCntry) {
    if (StringUtils.isEmpty(searchTerm)) {
      return true;
    }
    if (searchTerm.length() > 5) {
      searchTerm = searchTerm.substring(0, 5);
    }
    String sql = ExternalizedQuery.getSql("QUERY.CHECK.CLUSTER");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ISSUING_CNTRY", cmrIssuingCntry);
    query.setParameter("AP_CUST_CLUSTER_ID", searchTerm);
    List<String> result = query.getResults(String.class);
    if (result != null && !result.isEmpty()) {
      return true;
    }
    return false;
  }

  // private void createOtherAddrs(EntityManager entityManager, Admin admin,
  // Data data) {
  // Addr addr = getZS01Addr(entityManager, admin);
  // if (addr != null) {
  // HashMap<String, String> auMap =
  // getAddrNoAndCntryNo(data.getCmrIssuingCntry());
  //
  // createOthersaddr(auMap, addr, entityManager);
  // }
  // }

  // private HashMap<String, String> getAddrNoAndCntryNo(String cmrIssuingCntry)
  // {
  // HashMap<String, String> auMap = new HashMap<String, String>();
  // if (cmrIssuingCntry.equals(SystemLocation.AUSTRALIA)) {
  // auMap.put("1", "STAT");
  // auMap.put("A", "MAIL");
  // auMap.put("2", "ZP01");
  // auMap.put("3", "ZI01");
  // auMap.put("5", "ZF01");
  // auMap.put("6", "PUBS");
  // auMap.put("7", "PUBB");
  // // auMap.put("B", "ZS01");
  // auMap.put("D", "EDUC");
  // auMap.put("G", "CTYG");
  // auMap.put("H", "CTYH");
  // } else if (cmrIssuingCntry.equals(SystemLocation.NEW_ZEALAND)) {
  // auMap.put("1", "MAIL");
  // auMap.put("2", "ZP01");
  // // auMap.put("3", "ZS01");
  // auMap.put("4", "ZI01");
  // } else if (cmrIssuingCntry.equals(SystemLocation.HONG_KONG) ||
  // cmrIssuingCntry.equals(SystemLocation.MACAO)) {
  // auMap.put("1", "MAIL");
  // auMap.put("2", "ZP01");
  // } else {
  // auMap.put("2", "ZP01");
  // auMap.put("3", "ZI01");
  // auMap.put("4", "ZH01");
  //
  // }
  //
  // return auMap;
  // }
  //
  // private void createOthersaddr(HashMap<String, String> auMap, Addr addr,
  // EntityManager entityManager) {
  // Iterator<Entry<String, String>> iter = auMap.entrySet().iterator();
  // while (iter.hasNext()) {
  // Entry<?, ?> entry = iter.next();
  // String key = (String) entry.getKey();
  // String val = (String) entry.getValue();
  // Addr newAddr = new Addr();
  // AddrPK newAddrPk = new AddrPK();
  // AddrRdc rdc = new AddrRdc();
  // AddrPK rdcpk = new AddrPK();
  // try {
  // PropertyUtils.copyProperties(newAddr, addr);
  // PropertyUtils.copyProperties(newAddrPk, addr.getId());
  // newAddrPk.setAddrSeq(key);
  // newAddrPk.setAddrType(val);
  // newAddr.setId(newAddrPk);
  // PropertyUtils.copyProperties(rdc, addr);
  // PropertyUtils.copyProperties(rdcpk, addr.getId());
  // rdcpk.setAddrSeq(key);
  // rdcpk.setAddrType(val);
  // rdc.setId(rdcpk);
  // entityManager.persist(newAddr);
  // entityManager.persist(rdc);
  // entityManager.flush();
  // } catch (IllegalAccessException | InvocationTargetException |
  // NoSuchMethodException e) {
  // e.printStackTrace();
  // }
  // }
  // }
  //
  // private Addr getZS01Addr(EntityManager entityManager, Admin admin) {
  // Addr addr = null;
  // String sql = ExternalizedQuery.getSql("MQREQUEST.GETADDR");
  // PreparedQuery query = new PreparedQuery(entityManager, sql);
  // query.setParameter("REQ_ID", admin.getId().getReqId());
  // query.setParameter("ADDR_TYPE", "ZS01");
  // List<Addr> results = query.getResults(Addr.class);
  // if (results != null && !results.isEmpty()) {
  // addr = results.get(0);
  // }
  // return addr;
  // }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {
    List<String> fields = new ArrayList<>();
    fields.addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "STATE_PROV", "DEPT", "ADDR_TXT", "ADDR_TXT_2", "CITY1", "POST_CD", "LAND_CNTRY", "PO_BOX"));
    return fields;
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    switch (cmrIssiungCntry) {
    case SystemLocation.VIETNAM:
      return true;
    case SystemLocation.CAMBODIA:
      return true;
    /*
     * case SystemLocation.INDIA: return true;
     */
    case SystemLocation.SINGAPORE:
      return true;
    case SystemLocation.HONG_KONG:
      return true;
    case SystemLocation.MACAO:
      return true;
    case SystemLocation.MYANMAR:
      return true;
    case SystemLocation.LAOS:
      return true;
    }
    return false;
  }

  private void setAbbrevNM(Data data, String abbrevNM) {

    if (!StringUtils.isBlank(abbrevNM))
      if (abbrevNM.length() > 21)
        data.setAbbrevNm(abbrevNM.substring(0, 21));
      else
        data.setAbbrevNm(abbrevNM);
  }

  private void autoSetAbbrevLocnNMOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) {
    switch (data.getCmrIssuingCntry()) {
    case SystemLocation.INDIA:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("IN")) {
          data.setAbbrevLocn("India");
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("IN")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("IGF".equalsIgnoreCase(data.getCustSubGrp()) || "XIGF".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "IGF");
      } else if ("INTER".equalsIgnoreCase(data.getCustSubGrp()) || "XINT".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "IBM INDIA PVT LTD");
      } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "BLUEMIX");
      } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "MARKETPLACE");
      } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "SOFTLAYER USE ONLY");
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.SRI_LANKA:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("LK")) {
          data.setAbbrevLocn("Sri Lanka");
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("LK")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("INTER".equalsIgnoreCase(data.getCustSubGrp()) || "XINTER".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "IBM INDIA PVT LTD");
      } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "BLUEMIX");
      } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Marketplace Use Only");
      } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Softlayer Use Only");
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.BANGLADESH:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("BD")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("BD")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("INTER".equalsIgnoreCase(data.getCustSubGrp()) || "XINT".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "IBM INDIA PVT LTD");
      } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "BLUEMIX");
      } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Marketplace Use Only");
      } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Softlayer Use Only");
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.NEPAL:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("NP")) {
          data.setAbbrevLocn("Nepal");
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("NP")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      setAbbrevNM(data, mainRecord.getCmrName1Plain());
      break;
    case SystemLocation.SINGAPORE:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("SG")
            || (!"SPOFF".equalsIgnoreCase(data.getCustSubGrp()) && !"CROSS".equalsIgnoreCase(data.getCustSubGrp()))) {
          data.setAbbrevLocn("Singapore");
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("SG") || "SPOFF".equalsIgnoreCase(data.getCustSubGrp())) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "IGF INTERNAL_" + mainRecord.getCmrName1Plain());
      } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "BLUEMIX_" + mainRecord.getCmrName1Plain());
      } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "MARKET PLACE_" + mainRecord.getCmrName1Plain());
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA_" + mainRecord.getCmrName1Plain());
      } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Softlayer_" + mainRecord.getCmrName1Plain());
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.MALAYSIA:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("MY")) {
          if (mainRecord.getCmrStateDesc() != null) {
            data.setAbbrevLocn(mainRecord.getCmrStateDesc());
          }
          if (mainRecord.getCmrCity() != null) {
            data.setAbbrevLocn(mainRecord.getCmrCity());
          }
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("MY")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "IGF INTERNAL_" + mainRecord.getCmrName1Plain());
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA_" + mainRecord.getCmrName1Plain());
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.BRUNEI:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("BN")) {
          data.setAbbrevLocn("Brunei");
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("BN")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "IGF INTERNAL_" + mainRecord.getCmrName1Plain());
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA_" + mainRecord.getCmrName1Plain());
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.VIETNAM:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("VN")) {
          if (mainRecord.getCmrStateDesc() != null) {
            data.setAbbrevLocn(mainRecord.getCmrStateDesc());
          }
          if (mainRecord.getCmrCity() != null) {
            data.setAbbrevLocn(mainRecord.getCmrCity());
          }
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("VN")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "IGF INTERNAL_" + mainRecord.getCmrName1Plain());
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA_" + mainRecord.getCmrName1Plain());
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.INDONESIA:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("ID")) {
          if (mainRecord.getCmrStateDesc() != null) {
            data.setAbbrevLocn(mainRecord.getCmrStateDesc());
          }
          if (mainRecord.getCmrCity() != null) {
            data.setAbbrevLocn(mainRecord.getCmrCity());
          }
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("ID")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "IGF INTERNAL_" + mainRecord.getCmrName1Plain());
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA_" + mainRecord.getCmrName1Plain());
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.PHILIPPINES:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("PH")) {
          if (mainRecord.getCmrStateDesc() != null) {
            data.setAbbrevLocn(mainRecord.getCmrStateDesc());
          }
          if (mainRecord.getCmrCity() != null) {
            data.setAbbrevLocn(mainRecord.getCmrCity());
          }
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("PH")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "IGF INTERNAL_" + mainRecord.getCmrName1Plain());
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA_" + mainRecord.getCmrName1Plain());
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.THAILAND:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("TH")) {
          if (mainRecord.getCmrStateDesc() != null) {
            data.setAbbrevLocn(mainRecord.getCmrStateDesc());
          }
          if (mainRecord.getCmrCity() != null) {
            data.setAbbrevLocn(mainRecord.getCmrCity());
          }
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("TH")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "IGF INTERNAL_" + mainRecord.getCmrName1Plain());
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA_" + mainRecord.getCmrName1Plain());
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.HONG_KONG:
      if (admin.getReqType().equals("C")) {
        data.setAbbrevLocn("00 HK");
      }
      if ("AQSTN".equalsIgnoreCase(data.getCustSubGrp()) || "XAQST".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Acquisition use only");
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA use only");
      } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Consumer only");
      } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Market Place Order");
      } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Softlayer use only");
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.MACAO:
      if (admin.getReqType().equals("C")) {
        data.setAbbrevLocn("00 HK");
      }
      if ("AQSTN".equalsIgnoreCase(data.getCustSubGrp()) || "XAQST".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Acquisition use only");
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA use only");
      } else if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Consumer only");
      } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Market Place Order");
      } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Softlayer use only");
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.MYANMAR:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("MM")) {
          data.setAbbrevLocn("Myanmar");
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("MM")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "DUMMY_" + mainRecord.getCmrName1Plain());
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA_" + mainRecord.getCmrName1Plain());
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.LAOS:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("LA")) {
          data.setAbbrevLocn("Laos");
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("LA")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "DUMMY_" + mainRecord.getCmrName1Plain());
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA_" + mainRecord.getCmrName1Plain());
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.CAMBODIA:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("KH")) {
          data.setAbbrevLocn("Cambodia");
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("KH")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("DUMMY".equalsIgnoreCase(data.getCustSubGrp()) || "XDUMM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "DUMMY_" + mainRecord.getCmrName1Plain());
      } else if ("ASLOM".equalsIgnoreCase(data.getCustSubGrp()) || "XASLM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "ESA_" + mainRecord.getCmrName1Plain());
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.AUSTRALIA:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("AU")) {
          if (mainRecord.getCmrStateDesc() != null) {
            data.setAbbrevLocn(mainRecord.getCmrStateDesc());
          }
          if (mainRecord.getCmrCity() != null) {
            data.setAbbrevLocn(mainRecord.getCmrCity());
          }
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("AU")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Bluemix Use Only");
      } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Marketplace Use Only");
      } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Softlayer Use Only");
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    case SystemLocation.NEW_ZEALAND:
      if (admin.getReqType().equals("C")) {
        if (mainRecord.getCmrCountryLanded().equalsIgnoreCase("NZ")) {
          if (mainRecord.getCmrStateDesc() != null) {
            data.setAbbrevLocn(mainRecord.getCmrStateDesc());
          }
          if (mainRecord.getCmrCity() != null) {
            data.setAbbrevLocn(mainRecord.getCmrCity());
          }
        }
        if (!mainRecord.getCmrCountryLanded().equalsIgnoreCase("NZ")) {
          data.setAbbrevLocn(mainRecord.getCmrCountryLandedDesc());
        }
      }
      if ("BLUMX".equalsIgnoreCase(data.getCustSubGrp()) || "XBLUM".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Bluemix Use Only");
      } else if ("MKTPC".equalsIgnoreCase(data.getCustSubGrp()) || "XMKTP".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Marketplace Use Only");
      } else if ("SOFT".equalsIgnoreCase(data.getCustSubGrp()) || "XSOFT".equalsIgnoreCase(data.getCustSubGrp())) {
        setAbbrevNM(data, "Softlayer Use Only");
      } else {
        setAbbrevNM(data, mainRecord.getCmrName1Plain());
      }
      break;
    }
    String cmrIssuingCntry = data.getCmrIssuingCntry();
    if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 12 && (cmrIssuingCntry.equals("616") || cmrIssuingCntry.equals("796"))) {
      data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 12));
    }
    if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 9 && !(cmrIssuingCntry.equals("616") || cmrIssuingCntry.equals("796"))) {
      data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 9));
    }

  }

  private void setSector(Admin admin, Data data) {
    if (admin.getReqType().equals("C")) {
      String[] arryIndCdForSectorCOM = { "K", "U", "A" };
      String[] arryIndCdForSectorDIS = { "D", "W", "T", "R" };
      String[] arryIndCdForSectorFSS = { "F", "S", "N" };
      String[] arryIndCdForSectorIND = { "M", "P", "J", "V", "L" };
      String[] arryIndCdForSectorPUB = { "H", "X", "Y", "G", "E" };
      String[] arryIndCdForSectorCSI = { "B", "C" };
      String[] arryIndCdForSectorEXC = { "Z" };

      String[] arryISUCdForSectorGMB = { "32", "34" };

      String subIndCd = data.getSubIndustryCd();
      String IndCd = "";
      if (subIndCd != null) {
        IndCd = subIndCd.substring(0, 1);
      }
      String isuCd = data.getIsuCd();
      Boolean mrcFlag3 = false;
      data.setSectorCd("");
      searchArryAndSetSector(arryIndCdForSectorCOM, IndCd, data, "COM");
      searchArryAndSetSector(arryIndCdForSectorDIS, IndCd, data, "DIS");
      searchArryAndSetSector(arryIndCdForSectorFSS, IndCd, data, "FSS");
      searchArryAndSetSector(arryIndCdForSectorIND, IndCd, data, "IND");
      searchArryAndSetSector(arryIndCdForSectorPUB, IndCd, data, "PUB");
      searchArryAndSetSector(arryIndCdForSectorCSI, IndCd, data, "CSI");
      searchArryAndSetSector(arryIndCdForSectorEXC, IndCd, data, "EXC");

      for (String s : arryISUCdForSectorGMB) {
        if (isuCd != null && s.equals(isuCd)) {
          mrcFlag3 = true;
        } else {
          // do nothing.
        }
      }
      if (mrcFlag3.equals(true) && data.getSectorCd().isEmpty()) {
        data.setSectorCd("GMB");
      } else if (mrcFlag3.equals(false)) {
        // keep Sector Code values when MRC = 2.
      }
    }
  }

  private void searchArryAndSetSector(String[] arry, String searchValue, Data data, String setValue) {
    for (int i = 0; i < arry.length; i++) {
      if (arry[i].equals(searchValue)) {
        data.setSectorCd(setValue);
      }
    }
  }

  private void setMRC(Admin admin, Data data) {
    String[] arryISUCdForMRC3 = { "32", "34" };
    String isuCd = data.getIsuCd();
    Boolean mrcFlag3 = false;
    if (admin.getReqType().equals("C")) {
      data.setMrcCd("");
      if (StringUtils.isNotBlank(data.getIsuCd()) && data.getIsuCd().length() > 0) {
        for (String s : arryISUCdForMRC3) {
          if (isuCd != null && s.equals(isuCd)) {
            mrcFlag3 = true;
          } else {
            // do nothing
          }
        }
      }
      if (mrcFlag3.equals(true)) {
        data.setMrcCd("3");
      } else if (mrcFlag3.equals(false)) {
        data.setMrcCd("2");
      }
    }
  }

  private void setISBU(Admin admin, Data data) {
    if (admin.getReqType().equals("C")) {
      String mrcCd = data.getMrcCd();
      String industryCd = "";
      if (data.getSubIndustryCd() != null) {
        industryCd = data.getSubIndustryCd().substring(0, 1);
      }
      String sectorCd = data.getSectorCd();
      String isbuCd = "";
      // data.setIsbuCd("");
      if (StringUtils.isEmpty(industryCd)) {
        LOG.debug("Industry Code is empty when setting ISBU Code.");
      }
      if (sectorCd.isEmpty()) {
        LOG.debug("sector Code is empty when setting ISBU Code.");
      }
      if (!StringUtils.isEmpty(industryCd) && !StringUtils.isEmpty(sectorCd)) {
        if ("3".equals(mrcCd) && StringUtils.isEmpty(data.getIsbuCd())) {
          isbuCd = "GMB" + industryCd;
        } else if ("2".equals(mrcCd) && StringUtils.isEmpty(data.getIsbuCd())) {
          isbuCd = sectorCd + industryCd;
          data.setIsbuCd(isbuCd);
        }
      }
      if ("744".equals(data.getCmrIssuingCntry()) && "INTER".equals(data.getCustSubGrp()) && data.getIsbuCd().isEmpty()) {
        data.setIsbuCd("INT1");
      } else {
        // do nothing
      }

    }
  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
    // import will now create the correct number of addresses
    // createOtherAddrs(entityManager, admin, data);
  }

  /**
   * Connects to the WTAAS query service and gets the current values of the
   * record
   * 
   * @param mainRecord
   * @throws Exception
   */
  private WtaasRecord retrieveWTAASValues(FindCMRRecordModel mainRecord) throws Exception {
    String cmrIssuingCntry = mainRecord.getCmrIssuedBy();
    String cmrNo = mainRecord.getCmrNum();

    try {
      WtaasClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("CMR_SERVICES_URL"), WtaasClient.class);

      WtaasQueryRequest request = new WtaasQueryRequest();
      request.setCmrNo(cmrNo);
      request.setCountry(cmrIssuingCntry);

      WtaasQueryResponse response = client.executeAndWrap(WtaasClient.QUERY_ID, request, WtaasQueryResponse.class);
      if (response == null || !response.isSuccess()) {
        LOG.warn("Error or no response from WTAAS query.");
        throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE, new Exception("Error or no response from WTAAS query."));
      }
      if ("F".equals(response.getData().get("Status"))) {
        LOG.warn("Customer " + cmrNo + " does not exist in WTAAS.");
        throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE, new Exception("Customer " + cmrNo + " does not exist in WTAAS."));
      }

      WtaasRecord record = WtaasRecord.createFrom(response);
      // record = WtaasRecord.dummy();
      return record;

    } catch (Exception e) {
      LOG.warn("An error has occurred during retrieval of the values.", e);
      throw new CmrException(MessageUtil.ERROR_LEGACY_RETRIEVE, e);
    }

  }

  /**
   * static mapping of the ZS01 RDc address with the corresponding WTAAS address
   * use
   * 
   * @param country
   * @return
   */
  private String getZS01AddressUse(String country) {
    switch (country) {
    case SystemLocation.AUSTRALIA:
      return "B"; // contract
    case SystemLocation.NEW_ZEALAND:
      return "3"; // install
    case SystemLocation.BANGLADESH:
      return "1"; // mailing
    case SystemLocation.INDONESIA:
      return "1"; // mailing
    case SystemLocation.PHILIPPINES:
      return "1"; // mailing
    case SystemLocation.SINGAPORE:
      return "1"; // mailing
    case SystemLocation.VIETNAM:
      return "1"; // mailing
    case SystemLocation.THAILAND:
      return "1"; // mailing
    case SystemLocation.BRUNEI:
      return "1"; // mailing
    case SystemLocation.MALAYSIA:
      return "1"; // mailing
    case SystemLocation.MYANMAR:
      return "1"; // mailing
    case SystemLocation.SRI_LANKA:
      return "1"; // mailing
    case SystemLocation.INDIA:
      return "1"; // mailing
    case SystemLocation.MACAO:
      return "3"; // installing
    case SystemLocation.HONG_KONG:
      return "3"; // installing
    case SystemLocation.TAIWAN:
      return "B"; // legal address
    case SystemLocation.KOREA:
      return "B"; // ??

    default:
      return null;
    }

  }

  /* New functions for AP GCG R2 */

  /**
   * Handles the parsing of Line1-6 from the WTAAS address and puts them on the
   * target record
   * 
   * @param entityManager
   * @param cmrIssuingCntry
   * @param record
   * @param address
   */
  protected abstract void handleWTAASAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel mainRecord,
      FindCMRRecordModel record, WtaasAddress address);

  /**
   * Gets the address type in CreateCMR for the specific rdcType (KTOKD) and
   * sequence, as well as the WTAAS address use.
   * 
   * @param rdcType
   * @param addressSeq
   * @return create cmr address type
   * 
   */
  protected abstract String getMappedAddressType(String country, String rdcType, String addressSeq);

  /**
   * Gets the equivalent address use in WTAAS for the given address type in
   * CreateCMR
   * 
   * @param country
   * @param createCmrAddrType
   * @return
   */
  public abstract String getMappedAddressUse(String country, String createCmrAddrType);

  /**
   * Returns true if the WTAAS address needs to be imported and supported
   * 
   * @param country
   * @return
   */
  public boolean shouldAddWTAASAddess(String country, WtaasAddress address) {
    return true;
  }

  /**
   * Gets the equivalent address Type in Create CMR for the given address type
   * in WTAAS
   * 
   * @param country
   * @param createCmrAddrType
   * @return
   */
  public String getAddrTypeForWTAASAddrUse(String country, String wtaasAddressUse) {
    return null;
  }

  /**
   * Checks the equivalent RDc record from FindCMR of the {@link WtaasAddress}
   * record. The method uses a combination of address type mapping and mapped
   * address uses to locate the equivalent
   * 
   * @param source
   * @param address
   * @return
   */
  private FindCMRRecordModel locateRdcRecord(FindCMRResultModel source, WtaasAddress address) {
    String addrType = null;
    String addrUse = null;
    for (FindCMRRecordModel record : source.getItems()) {
      addrType = getMappedAddressType(record.getCmrIssuedBy(), record.getCmrAddrTypeCode(), record.getCmrAddrSeq());
      if (!StringUtils.isEmpty(addrType)) {
        // it's mapped in createcmr, check equivalent address use if the wtaas
        // address has this use
        addrUse = getMappedAddressUse(record.getCmrIssuedBy(), addrType);
        if (!StringUtils.isEmpty(addrUse) && address.getAddressUse().contains(addrUse)) {
          record.setCmrAddrTypeCode(addrType);
          return record;
        }
      }
    }
    return null;
  }

  /**
   * Auxiliary method to check if the data is a street line
   * 
   * @param data
   * @return
   */
  protected boolean isStreet(String data) {
    if (data == null) {
      return false;
    }
    return data.toUpperCase().contains("ST.") || data.toUpperCase().contains("STREET") || (" " + data + " ").toUpperCase().contains(" AVE ")
        || data.toUpperCase().contains("AVENUE") || (" " + data + " ").toUpperCase().contains("ROAD")
        || (" " + data + " ").toUpperCase().contains(" RD ") || (" " + data + " ").toUpperCase().contains(" RD,")
        || (" " + data + " ").toUpperCase().contains("BOULEVARD") || (" " + data + " ").toUpperCase().contains(" BLVD ");
  }

  /**
   * Auxiliary method to check if the data is a dept/attn line
   * 
   * @param data
   * @return
   */
  protected boolean isAttn(String data) {
    if (data == null) {
      return false;
    }
    return data.toUpperCase().contains("ATTN") || data.toUpperCase().startsWith("ATT.") || data.toUpperCase().startsWith("AT ")
        || data.toUpperCase().startsWith("ATT ") || data.toUpperCase().startsWith("ATT:") || data.toUpperCase().startsWith("TO ")
        || data.toUpperCase().startsWith("TO:") || data.toUpperCase().startsWith("C/O");
  }

  /**
   * Extracts the landed country code and the remaining data based on the
   * address line
   * 
   * @param data
   * @return
   */
  protected String[] extractCountry(String line) {
    String[] data = new String[2];
    if (line != null && line.contains("<") && line.contains(">")) {
      int startIndex = line.indexOf("<");
      int endIndex = line.indexOf(">");
      if (startIndex < endIndex) {
        String country = line.substring(startIndex + 1, endIndex).trim();
        String otherData = line.substring(endIndex + 1).trim();
        data[0] = country;
        data[1] = otherData;
      }
    }
    return data;

  }

  /**
   * Handles converting the RDc values into the correct placeholders in
   * CreateCMR
   * 
   * @param record
   */
  protected void handleRDcRecordValues(FindCMRRecordModel record) {

    String name3 = record.getCmrName3();
    String name4 = record.getCmrName4();

    if (!StringUtils.isEmpty(name3) && !StringUtils.isEmpty(name4)) {
      if (isAttn(name3)) {
        record.setCmrDept(name3);
        record.setCmrStreetAddressCont(name4);
      } else if (isAttn(name4)) {
        record.setCmrDept(name4);
        record.setCmrStreetAddressCont(name3);
      } else {
        record.setCmrDept(name3);
        record.setCmrStreetAddressCont(name4);
      }
    } else if (!StringUtils.isEmpty(name3)) {
      if (isAttn(name3)) {
        record.setCmrDept(name3);
      } else {
        record.setCmrStreetAddressCont(name3);
      }
    } else if (!StringUtils.isEmpty(name4)) {
      if (isAttn(name4)) {
        record.setCmrDept(name4);
      } else {
        record.setCmrStreetAddressCont(name4);
      }
    }

    record.setCmrName3(null);
    record.setCmrName4(null);

  }

  /**
   * For logging only, trace level
   * 
   * @param address
   */
  protected void logExtracts(FindCMRRecordModel address) {
    if (LOG.isTraceEnabled()) {
      LOG.trace("Name: " + address.getCmrName1Plain());
      LOG.trace("Name 2: " + address.getCmrName2Plain());
      LOG.trace("Street: " + address.getCmrStreetAddress());
      LOG.trace("Street Cont 1: " + address.getCmrStreetAddressCont());
      LOG.trace("Attn/Street Con't 2: " + address.getCmrDept());
      LOG.trace("PO Box: " + address.getCmrPOBox());
      LOG.trace("Zip: " + address.getCmrPostalCode());
      LOG.trace("Phone: " + address.getCmrCustPhone());
      LOG.trace("City: " + address.getCmrCity());
      LOG.trace("State: " + address.getCmrState());
      LOG.trace("Country: " + address.getCmrCountryLanded());
    }

    System.out.println("Name: " + address.getCmrName1Plain());
    System.out.println("Name 2: " + address.getCmrName2Plain());
    System.out.println("Street: " + address.getCmrStreetAddress());
    System.out.println("Street Cont 1: " + address.getCmrStreetAddressCont());
    System.out.println("Attn/Street Con't 2: " + address.getCmrDept());
    System.out.println("PO Box: " + address.getCmrPOBox());
    System.out.println("Zip: " + address.getCmrPostalCode());
    System.out.println("Phone: " + address.getCmrCustPhone());
    System.out.println("City: " + address.getCmrCity());
    System.out.println("State: " + address.getCmrState());
    System.out.println("Country: " + address.getCmrCountryLanded());

  }

  protected void doAfterConvert(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel,
      List<FindCMRRecordModel> converted) {

  }

  @Override
  public Map<String, String> getUIFieldIdMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("##OriginatorName", "originatorNm");
    map.put("##SensitiveFlag", "sensitiveFlag");
    map.put("##INACType", "inacType");
    map.put("##ISU", "isuCd");
    map.put("##Sector", "sectorCd");
    map.put("##Cluster", "apCustClusterId");
    map.put("##RestrictedInd", "restrictInd");
    map.put("##CMROwner", "cmrOwner");
    map.put("##PPSCEID", "ppsceid");
    map.put("##CustLang", "custPrefLang");
    map.put("##ProvinceCode", "territoryCd");
    map.put("##CmrNoPrefix", "cmrNoPrefix");
    map.put("##ProvinceName", "busnType");
    map.put("##GlobalBuyingGroupID", "gbgId");
    map.put("##CoverageID", "covId");
    map.put("##OriginatorID", "originatorId");
    map.put("##BPRelationType", "bpRelType");
    map.put("##CAP", "capInd");
    map.put("##LocalTax1", "taxCd1");
    map.put("##RequestReason", "reqReason");
    map.put("##LandedCountry", "landCntry");
    map.put("##CMRIssuingCountry", "cmrIssuingCntry");
    map.put("##INACCode", "inacCd");
    map.put("##CustomerScenarioType", "custGrp");
    map.put("##City1", "city1");
    map.put("##StateProv", "stateProv");
    map.put("##RequestingLOB", "requestingLob");
    map.put("##AddrStdRejectReason", "addrStdRejReason");
    map.put("##ExpediteReason", "expediteReason");
    map.put("##VAT", "vat");
    map.put("##CollectionCd", "collectionCd");
    map.put("##CMRNumber", "cmrNo");
    map.put("##Subindustry", "subIndustryCd");
    map.put("##EnterCMR", "enterCMRNo");
    map.put("##DisableAutoProcessing", "disableAutoProc");
    map.put("##Expedite", "expediteInd");
    map.put("##Affiliate", "affiliate");
    map.put("##BGLDERule", "bgRuleId");
    map.put("##ProspectToLegalCMR", "prospLegalInd");
    map.put("##ISBU", "isbuCd");
    map.put("##ClientTier", "clientTier");
    map.put("##AbbrevLocation", "abbrevLocn");
    map.put("##SalRepNameNo", "repTeamMemberNo");
    map.put("##SAPNumber", "sapNo");
    map.put("##Department", "dept");
    map.put("##StreetAddress2", "addrTxt2");
    map.put("##Company", "company");
    map.put("##StreetAddress1", "addrTxt");
    map.put("##AbbrevName", "abbrevNm");
    map.put("##CustomerName1", "custNm1");
    map.put("##CustomerName2", "custNm2");
    map.put("##ISIC", "isicCd");
    map.put("##MrcCd", "mrcCd");
    map.put("##Enterprise", "enterprise");
    map.put("##PostalCode", "postCd");
    map.put("##SOENumber", "soeReqNo");
    map.put("##DUNS", "dunsNo");
    map.put("##BuyingGroupID", "bgId");
    map.put("##RequesterID", "requesterId");
    map.put("##GeoLocationCode", "geoLocationCd");
    map.put("##MembLevel", "memLvl");
    map.put("##GovIndicator", "govType");
    map.put("##RequestType", "reqType");
    map.put("##CustomerScenarioSubType", "custSubGrp");
    map.put("##RegionCode", "miscBillCd");
    return map;
  }

  @Override
  public String buildAddressForDnbMatching(String country, Addr addr) {
    if (SystemLocation.INDIA.equals(country) || SystemLocation.AUSTRALIA.equals(country) || SystemLocation.SINGAPORE.equals(country)
        || SystemLocation.NEW_ZEALAND.equals(country)) {
      String address = addr.getAddrTxt() != null ? addr.getAddrTxt() : "";
      address += StringUtils.isNotBlank(addr.getAddrTxt2()) ? " " + addr.getAddrTxt2() : "";
      address += StringUtils.isNotBlank(addr.getDept()) ? " " + addr.getDept() : "";
      address = address.trim();
      return address;
    }
    return null;
  }

}
