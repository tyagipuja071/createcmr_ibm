/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cio.cmr.request.model.requestentry.ImportCMRModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.wodm.coverage.CoverageInput;

/**
 * {@link GEOHandler} for:
 * <ul>
 * <li>616 - Australia</li>
 * <li>796 - New Zealand</li>
 * </ul>
 * 
 * @author JeffZAMORA
 * 
 */
public class ANZHandler extends GEOHandler {

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();
  private static final List<String> fields = new ArrayList<>();
  static {
    LANDED_CNTRY_MAP.put(SystemLocation.AUSTRALIA, "AU");
    LANDED_CNTRY_MAP.put(SystemLocation.NEW_ZEALAND, "NZ");
  }
  private static final Logger LOG = Logger.getLogger(ANZHandler.class);
  private static final String SOLD_TO_ADDR_TYPE = "ZS01";
  private static final String BILL_TO_ADDR_TYPE = "ZP01";
  private static final String INSTALL_AT_ADDR_TYPE = "ZI01";
  private static final String SHIP_TO_ADDR_TYPE = "ZD01";

  private static final String SOLD_TO_FIXED_SEQ_AU = "07";
  private static final String SOLD_TO_FIXED_SEQ_NZ = "02";
  private static final List<String> BILL_TO_FIXED_SEQ = Arrays.asList("020");
  private static final List<String> INSTALL_AT_FIXED_SEQ_AU = Arrays.asList("050");
  private static final List<String> INSTALL_AT_FIXED_SEQ_NZ = Arrays.asList("050");
  private static final List<String> SHIP_TO_FIXED_SEQ = Arrays.asList("040");

  public static void main(String[] args) {
  }

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
  }

  @Override
  public void doFilterAddresses(List<AddressModel> results) {
    List<AddressModel> addrsToRemove = new ArrayList<AddressModel>();
    for (AddressModel addrModel : results) {
      if ("XXXX".equalsIgnoreCase(addrModel.getAddrType())) {
        addrsToRemove.add(addrModel);
      }
    }
    results.removeAll(addrsToRemove);
  }

  protected void handleRDcRecordValues(FindCMRRecordModel record) {
    String name3 = record.getCmrName3();
    String name4 = record.getCmrName4();
    String street = record.getCmrStreetAddress();

    record.setCmrDept(name3);
    if (!StringUtils.isEmpty(name4)) {
      record.setCmrStreetAddress(name4);
      record.setCmrStreetAddressCont(street);
    } else {
      record.setCmrStreetAddress(street);
      record.setCmrStreetAddressCont(null);
    }
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
    map.put("##CustomerServiceCd", "engineeringBo");
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
    map.put("##StreetAddress2", "addrTxt2");
    map.put("##Department", "dept");
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
    map.put("##CustClass", "custClass");
    return map;
  }

  @Override
  public boolean isNewMassUpdtTemplateSupported(String issuingCountry) {
    return false;
  }

  @Override
  public boolean matchDnbMailingAddr(DnBMatchingResponse dnbRecord, Addr addr, String issuingCountry, Boolean allowLongNameAddress) {
    if ("616".equals(issuingCountry) || "796".equals(issuingCountry)) {
      // match address
      String address = addr.getAddrTxt() != null ? addr.getAddrTxt() : "";
      address += StringUtils.isNotBlank(addr.getAddrTxt2()) ? " " + addr.getAddrTxt2() : "";
      address = address.trim();

      String handlerAddress = buildAddressForDnbMatching(issuingCountry, addr);
      if (!StringUtils.isBlank(handlerAddress)) {
        address = handlerAddress;
        LOG.debug("Address used for matching: " + address);
      }

      String MailingDnbAddress = dnbRecord.getMailingDnbStreetLine1() != null ? dnbRecord.getMailingDnbStreetLine1() : "";
      if (StringUtils.isNotBlank(addr.getAddrTxt2())) {
        MailingDnbAddress += StringUtils.isNotBlank(dnbRecord.getMailingDnbStreetLine2()) ? " " + dnbRecord.getMailingDnbStreetLine2() : "";
      }
      MailingDnbAddress = MailingDnbAddress.trim();

      // CREATCMR-8430: return false for mailing address matching if
      // MailingDnbAddress is blank
      // CREATCMR-8553: for AU, if mailing address is null in DNB, return false;
      if (StringUtils.isNotBlank(address) && StringUtils.isBlank(MailingDnbAddress)) {
        return false;
      }

      Boolean isReshuffledAddr = compareReshuffledAddress(MailingDnbAddress, address, issuingCountry);
      if ((StringUtils.isNotBlank(address) && StringUtils.isNotBlank(MailingDnbAddress)
          && StringUtils.getLevenshteinDistance(address.toUpperCase(), MailingDnbAddress.toUpperCase()) > 8
          && !(allowLongNameAddress && MailingDnbAddress.replaceAll("\\s", "").contains(address.replaceAll("\\s", "")))) && !isReshuffledAddr) {
        return false;
      }
      // match postal cd
      if (StringUtils.isNotBlank(addr.getPostCd()) && StringUtils.isNotBlank(dnbRecord.getMailingDnbPostalCd())) {
        String currentPostalCode = addr.getPostCd();
        String dnbMailingPostalCode = dnbRecord.getMailingDnbPostalCd();
        if (currentPostalCode.length() != dnbMailingPostalCode.length()) {
          if (calAlignPostalCodeLength(currentPostalCode, dnbMailingPostalCode)) {
            return false;
          }
        }
        if (currentPostalCode.length() == dnbMailingPostalCode.length()) {
          if (!isPostalCdCloselyMatchesDnB(currentPostalCode, dnbMailingPostalCode)) {
            return false;
          }
        }
      }
      // match city
      if (StringUtils.isNotBlank(addr.getCity1()) && StringUtils.isNotBlank(dnbRecord.getMailingDnbCity())
          && StringUtils.getLevenshteinDistance(addr.getCity1().toUpperCase(), dnbRecord.getMailingDnbCity().toUpperCase()) > 6) {
        return false;
      }
      // Mailing Address close matching - END
      return true;
    } else {
      return false;
    }
  }

  private static boolean calAlignPostalCodeLength(String currPostalCd, String dnBPostalCd) {
    String shortPostalCd = "";
    currPostalCd = currPostalCd.replaceAll("[^\\w\\s]+", "").trim();
    dnBPostalCd = dnBPostalCd.replaceAll("[^\\w\\s]+", "").trim();
    boolean res = true;
    if (currPostalCd.length() > dnBPostalCd.length()) {
      shortPostalCd = currPostalCd.substring(0, dnBPostalCd.length());
      currPostalCd = shortPostalCd;
    } else {
      shortPostalCd = dnBPostalCd.substring(0, currPostalCd.length());
      dnBPostalCd = shortPostalCd;
    }
    LOG.debug("Shortned postal code= " + shortPostalCd);
    res = isPostalCdCloselyMatchesDnB(currPostalCd, dnBPostalCd);
    return res;
  }

  private static boolean isPostalCdCloselyMatchesDnB(String currPostalCd, String dnBPostalCd) {
    boolean res = true;
    if (dnBPostalCd.length() <= 5 && StringUtils.getLevenshteinDistance(currPostalCd.toUpperCase(), dnBPostalCd.toUpperCase()) > 2) {
      res = false;
    } else if (dnBPostalCd.length() > 5 && StringUtils.getLevenshteinDistance(currPostalCd.toUpperCase(), dnBPostalCd.toUpperCase()) > 3) {
      res = false;
    }
    return res;
  }

  @Override
  public void doBeforeDataSave(EntityManager entityManager, Admin admin, Data data, String cmrIssuingCntry) throws Exception {
    String sql = ExternalizedQuery.getSql("DNB.GET_CURR_SOLD_TO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", data.getId().getReqId());
    query.setForReadOnly(true);
    Addr soldTo = query.getSingleResult(Addr.class);
    if (soldTo != null) {
      setAbbrevLocNMBeforeAddrSave(entityManager, soldTo, data.getCmrIssuingCntry());
    }
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
          // CREATCMR-7653: for NZ, don't overwrite abbv name
        }
        break;
      }
      if (data.getAbbrevLocn() != null && data.getAbbrevLocn().length() > 12) {
        data.setAbbrevLocn(data.getAbbrevLocn().substring(0, 12));
      }

      String custSubGrp = data.getCustSubGrp();
      List<String> custSubGrpList = Arrays.asList("NRML", "INTER", "DUMMY", "AQSTN", "BLUMX", "MKTPC", "ECSYS", "ESOSW", "CROSS", "XAQST", "XBLUM",
          "XMKTP", "XESO", "PRIV", "NRMLC", "KYND");
      if (custSubGrpList.contains(custSubGrp) && SystemLocation.NEW_ZEALAND.equals(cmrIssuingCntry)) {
        data.setEngineeringBo("9920");
      }
      data.setRepTeamMemberNo("000000");
      if (data.getSubIndustryCd() != null
          && ("G".equals(data.getSubIndustryCd().substring(0, 1)) || "Y".equals(data.getSubIndustryCd().substring(0, 1)))) {
        data.setGovType("Y");
      } else {
        data.setGovType("N");
      }
      data.setMrcCd("");
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

  private void setAbbrevNM(Data data, String abbrevNM) {

    if (!StringUtils.isBlank(abbrevNM))
      if (abbrevNM.length() > 21)
        data.setAbbrevNm(abbrevNM.substring(0, 21));
      else
        data.setAbbrevNm(abbrevNM);
  }

  @Override
  public void convertFrom(EntityManager entityManager, FindCMRResultModel source, RequestEntryModel reqEntry, ImportCMRModel searchModel)
      throws Exception {
    List<FindCMRRecordModel> recordsFromSearch = source.getItems();
    List<FindCMRRecordModel> filteredRecords = new ArrayList<>();

    if (CmrConstants.REQ_TYPE_CREATE.equals(reqEntry.getReqType())) {
      for (FindCMRRecordModel record : recordsFromSearch) {
        if ("ZS01".equals(record.getCmrAddrTypeCode())) {
          if (SystemLocation.AUSTRALIA.equals(record.getCmrIssuedBy())) {
            record.setCmrAddrSeq(SOLD_TO_FIXED_SEQ_AU);
          } else {
            record.setCmrAddrSeq(SOLD_TO_FIXED_SEQ_NZ);
          }
          filteredRecords.add(record);
        }

        if (StringUtils.isNotBlank(record.getCmrAddrSeq()) && "ZP01".equals(record.getCmrAddrTypeCode())
            && StringUtils.isNotEmpty(record.getExtWalletId())) {
          record.setCmrAddrTypeCode("PG01");
          filteredRecords.add(record);
        }
      }
      source.setItems(filteredRecords);
    } else {
      if (recordsFromSearch != null && !recordsFromSearch.isEmpty() && recordsFromSearch.size() > 0) {
        doFilterAddresses(reqEntry, recordsFromSearch, filteredRecords);
        if (!filteredRecords.isEmpty() && filteredRecords.size() > 0 && filteredRecords != null) {
          source.setItems(filteredRecords);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static void doFilterAddresses(RequestEntryModel reqEntry, Object mainRecords, Object filteredRecords) {
    if (mainRecords instanceof java.util.List<?> && filteredRecords instanceof java.util.List<?>) {
      List<FindCMRRecordModel> recordsToCheck = (List<FindCMRRecordModel>) mainRecords;
      List<FindCMRRecordModel> recordsToReturn = (List<FindCMRRecordModel>) filteredRecords;
      for (Object tempRecObj : recordsToCheck) {
        if (tempRecObj instanceof FindCMRRecordModel) {
          FindCMRRecordModel tempRec = (FindCMRRecordModel) tempRecObj;
          String addrSeq = tempRec.getCmrAddrSeq();
          String cmrIssuingCntry = tempRec.getCmrIssuedBy();
          if (StringUtils.isNotEmpty(addrSeq)) {
            if (CmrConstants.ANZ_COUNTRIES.contains(cmrIssuingCntry)) {
              if (StringUtils.isNotBlank(addrSeq) && "ZP01".equals(tempRec.getCmrAddrTypeCode())
                  && StringUtils.isNotEmpty(tempRec.getExtWalletId())) {
                tempRec.setCmrAddrTypeCode("PG01");
              }
            }
          }
          recordsToReturn.add(tempRec);
        }
      }
    }

  }

  @Override
  public void setDataValuesOnImport(Admin admin, Data data, FindCMRResultModel results, FindCMRRecordModel mainRecord) throws Exception {
    List<String> anzCountries = Arrays.asList(SystemLocation.AUSTRALIA, SystemLocation.NEW_ZEALAND);
    if (CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType()) && anzCountries.contains(data.getCmrIssuingCntry())) {
      admin.setOldCustNm1(mainRecord.getCmrName1Plain());
      admin.setOldCustNm2(mainRecord.getCmrName2Plain());
      data.setCollectionCd(mainRecord.getCmrAccRecvBo() != null ? mainRecord.getCmrAccRecvBo() : "0000");
    }
  }

  @Override
  public void setAdminValuesOnImport(Admin admin, FindCMRRecordModel currentRecord) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void createOtherAddressesOnDNBImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void setAddressValuesOnImport(Addr address, Admin admin, FindCMRRecordModel currentRecord, String cmrNo) throws Exception {
    address.setCustNm1(currentRecord.getCmrName1Plain());
    address.setCustNm2(currentRecord.getCmrName2Plain());
    String name3 = currentRecord.getCmrName3();
    String name4 = currentRecord.getCmrName4();
    String stras = currentRecord.getCmrStreetAddress();

    if (StringUtils.isNotEmpty(name3) && StringUtils.isNotEmpty(name4) && StringUtils.isNotEmpty(stras)) {
      address.setAddrTxt(name4);
      address.setAddrTxt2(stras);
      address.setDept(name3.substring(4));
    }

    if (StringUtils.isEmpty(name3) && StringUtils.isNotEmpty(name4) && StringUtils.isNotEmpty(stras)) {
      if (name4.contains("ATTN")) {
        address.setDept(name4.substring(4));
        address.setAddrTxt(stras);
      } else {
        address.setAddrTxt(name4);
        address.setAddrTxt2(stras);
      }
    }

    if (StringUtils.isEmpty(name3) && StringUtils.isEmpty(name4) && StringUtils.isNotEmpty(stras)) {
      address.setAddrTxt(stras);
    }
  }

  @Override
  public int getName1Length() {
    return 35;
  }

  @Override
  public int getName2Length() {
    return 35;
  }

  @Override
  public void setAdminDefaultsOnCreate(Admin admin) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setDataDefaultsOnCreate(Data data, EntityManager entityManager) {
    // TODO Auto-generated method stub

  }

  @Override
  public void appendExtraModelEntries(EntityManager entityManager, ModelAndView mv, RequestEntryModel model) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void handleImportByType(String requestType, Admin admin, Data data, boolean importing) {
    // TODO Auto-generated method stub

  }

  @Override
  public void convertCoverageInput(EntityManager entityManager, CoverageInput request, Addr mainAddr, RequestEntryModel data) {
    // TODO Auto-generated method stub
    request.setSORTL(data.getApCustClusterId());

  }

  @Override
  public boolean retrieveInvalidCustomersForCMRSearch(String cmrIssuingCntry) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void doBeforeAdminSave(EntityManager entityManager, Admin admin, String cmrIssuingCntry) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean customerNamesOnAddress() {
    return true;
  }

  @Override
  public boolean useSeqNoFromImport() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean skipOnSummaryUpdate(String cntry, String field) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void doAfterImport(EntityManager entityManager, Admin admin, Data data) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public List<String> getAddressFieldsForUpdateCheck(String cmrIssuingCntry) {

    if (fields.size() == 0) {
      fields
          .addAll(Arrays.asList("CUST_NM1", "CUST_NM2", "DEPT", "STATE_PROV", "CITY1", "POST_CD", "LAND_CNTRY", "ADDR_TXT", "ADDR_TXT_2", "SAP_NO"));
    }
    return fields;
  }

  @Override
  public boolean hasChecklist(String cmrIssiungCntry) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String generateModifyAddrSeqOnCopy(EntityManager entityManager, String addrType, long reqId, String oldAddrSeq, String cmrIssuingCntry) {
    String newAddrSeq = null;
    newAddrSeq = generateAddrSeq(entityManager, addrType, reqId, cmrIssuingCntry);
    return newAddrSeq;
  }

  @Override
  public String generateAddrSeq(EntityManager entityManager, String addrType, long reqId, String cmrIssuingCntry) {
    String newAddrSeq = "";

    if (!StringUtils.isEmpty(addrType)) {

      AdminPK adminPK = new AdminPK();
      adminPK.setReqId(reqId);
      Admin admin = entityManager.find(Admin.class, adminPK);

      boolean isUpdate = CmrConstants.REQ_TYPE_UPDATE.equals(admin.getReqType());

      if (SystemLocation.AUSTRALIA.equals(cmrIssuingCntry)) {
        newAddrSeq = getNewAddressSeqAU(entityManager, reqId, addrType, isUpdate);
      } else if (SystemLocation.NEW_ZEALAND.equals(cmrIssuingCntry)) {
        newAddrSeq = getNewAddressSeqNZ(entityManager, reqId, addrType, isUpdate);
      }
    }
    return newAddrSeq;
  }

  private String getNewAddressSeqAU(EntityManager entityManager, long reqId, String addrType, boolean isUpdate) {
    String newAddrSeq = "";
    switch (addrType) {
    case SOLD_TO_ADDR_TYPE:
      newAddrSeq = SOLD_TO_FIXED_SEQ_AU;
      break;
    case BILL_TO_ADDR_TYPE:
      newAddrSeq = getNewSeqFromSeqToCheck(entityManager, reqId, addrType, BILL_TO_FIXED_SEQ, isUpdate);
      break;
    case INSTALL_AT_ADDR_TYPE:
      newAddrSeq = getNewSeqFromSeqToCheck(entityManager, reqId, addrType, INSTALL_AT_FIXED_SEQ_AU, isUpdate);
      break;
    case SHIP_TO_ADDR_TYPE:
      newAddrSeq = getNewSeqFromSeqToCheck(entityManager, reqId, addrType, SHIP_TO_FIXED_SEQ, isUpdate);
      break;
    default:
      newAddrSeq = "";
      break;
    }
    return newAddrSeq;
  }

  private String getNewAddressSeqNZ(EntityManager entityManager, long reqId, String addrType, boolean isUpdate) {
    String newAddrSeq = "";
    switch (addrType) {
    case SOLD_TO_ADDR_TYPE:
      newAddrSeq = SOLD_TO_FIXED_SEQ_NZ;
      break;
    case BILL_TO_ADDR_TYPE:
      newAddrSeq = getNewSeqFromSeqToCheck(entityManager, reqId, addrType, BILL_TO_FIXED_SEQ, isUpdate);
      break;
    case INSTALL_AT_ADDR_TYPE:
      newAddrSeq = getNewSeqFromSeqToCheck(entityManager, reqId, addrType, INSTALL_AT_FIXED_SEQ_NZ, isUpdate);
      break;
    case SHIP_TO_ADDR_TYPE:
      newAddrSeq = getNewSeqFromSeqToCheck(entityManager, reqId, addrType, SHIP_TO_FIXED_SEQ, isUpdate);
      break;
    default:
      newAddrSeq = "";
      break;
    }
    return newAddrSeq;
  }

  private String getNewSeqFromSeqToCheck(EntityManager entityManager, long reqId, String addrType, List<String> seqToCheck, boolean isUpdate) {
    Set<String> existingSeq = getExistingAddrSeqInclRdc(entityManager, reqId);
    boolean isAddrTypeExist = false;

    if (isUpdate) {
      List<Addr> allAddrByTypeFromAddr = getAddressByType(entityManager, addrType, reqId);
      int addrCount = allAddrByTypeFromAddr.size();
      if (addrCount > 0) {
        isAddrTypeExist = true;
      }
    }

    if (existingSeq.isEmpty()) {
      return seqToCheck.get(0);
    } else {
      return String.valueOf(getNewSeqAdditionalAddr(existingSeq, addrType, isAddrTypeExist, isUpdate));
    }
  }

  private String getNewSeqAdditionalAddr(Set<String> existingAddrSeqSet, String addrType, boolean isAddrTypeExist, boolean isUpdate) {
    int candidateSeqNum = 0;
    switch (addrType) {
    case BILL_TO_ADDR_TYPE:

      if (isUpdate && isAddrTypeExist) {
        candidateSeqNum = 21;
      } else {
        candidateSeqNum = 20;
      }
      break;
    case INSTALL_AT_ADDR_TYPE:
      if (isUpdate && isAddrTypeExist) {
        candidateSeqNum = 51;
      } else {
        candidateSeqNum = 50;
      }
      break;
    case SHIP_TO_ADDR_TYPE:
      candidateSeqNum = 40;
      break;
    default:
      candidateSeqNum = 0;
      LOG.debug("ANZ additional address failed to assign sequence");
      break;
    }

    LOG.info("Candidate Seq: " + candidateSeqNum);
    return getAvailAddrSeqNumInclRdc(existingAddrSeqSet, candidateSeqNum);
  }

  private String getAvailAddrSeqNumInclRdc(Set<String> existingAddrSeqSet, int candidateSeqNum) {
    int availSeqNum = 0;
    if (existingAddrSeqSet.contains(String.format("%03d", candidateSeqNum))) {
      availSeqNum = candidateSeqNum;
      while (existingAddrSeqSet.contains(String.format("%03d", availSeqNum))) {
        availSeqNum++;
        if (availSeqNum > 999) {
          availSeqNum = 1;
        }
      }
    } else {
      availSeqNum = candidateSeqNum;
    }

    LOG.info("Avail: " + availSeqNum);

    return String.format("%03d", availSeqNum);
  }

  private List<Addr> getAddressByType(EntityManager entityManager, String addrType, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GET.BYTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    List<Addr> addrList = query.getResults(Addr.class);
    return addrList;
  }

  private Set<String> getExistingAddrSeqInclRdc(EntityManager entityManager, long reqId) {
    DataPK pk = new DataPK();
    pk.setReqId(reqId);
    Data data = entityManager.find(Data.class, pk);

    String cmrNo = data.getCmrNo();
    Set<String> allAddrSeqFromAddr = getAllSavedSeqFromAddr(entityManager, reqId);
    Set<String> allAddrSeqFromRdc = getAllSavedSeqFromRdc(entityManager, cmrNo, data.getCmrIssuingCntry());

    Set<String> mergedAddrSet = new HashSet<>();
    mergedAddrSet.addAll(allAddrSeqFromAddr);
    mergedAddrSet.addAll(allAddrSeqFromRdc);

    return mergedAddrSet;
  }

  private Set<String> getAllSavedSeqFromAddr(EntityManager entityManager, long reqId) {
    String sql = ExternalizedQuery.getSql("CA.GET.ADDRSEQ.BY_REQID");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    List<String> results = query.getResults(String.class);

    Set<String> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(results);

    return addrSeqSet;
  }

  private Set<String> getAllSavedSeqFromRdc(EntityManager entityManager, String cmrNo, String cmrIssuingCntry) {
    String sql = ExternalizedQuery.getSql("CA.GET.KNA1_ZZKV_SEQNO");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("KATR6", cmrIssuingCntry);
    query.setParameter("ZZKV_CUSNO", cmrNo);

    List<String> resultsRDC = query.getResults(String.class);
    Set<String> addrSeqSet = new HashSet<>();
    addrSeqSet.addAll(resultsRDC);

    return addrSeqSet;
  }

}
