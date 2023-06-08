/**
 * 
 */
package com.ibm.cio.cmr.request.util.geo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.AddrPK;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.AdminPK;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.entity.DataPK;
import com.ibm.cio.cmr.request.model.requestentry.AddressModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cio.cmr.request.util.wtaas.WtaasAddress;
import com.ibm.cio.cmr.request.util.wtaas.WtaasQueryKeys.Address;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;

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
public class ANZHandler extends APHandler {

  public static Map<String, String> LANDED_CNTRY_MAP = new HashMap<String, String>();
  private static final String[] NZ_SUPPORTED_ADDRESS_USES = { "1", "2", "3", "4", "5", "G", "H" };
  private static final String[] AU_SUPPORTED_ADDRESS_USES = { "2", "B", "G", "H", "D", "3", "A", "7", "6", "5", "1" };

  static {
    LANDED_CNTRY_MAP.put(SystemLocation.AUSTRALIA, "AU");
    LANDED_CNTRY_MAP.put(SystemLocation.NEW_ZEALAND, "NZ");
  }
  private static final Logger LOG = Logger.getLogger(ANZHandler.class);

  public static void main(String[] args) {
    // parse testing
    WtaasAddress address = new WtaasAddress();
    address.getValues().put(Address.Line1, "CUSTOMER NAME");
    address.getValues().put(Address.Line2, "CUSTOMER NAME CON'T");
    address.getValues().put(Address.Line3, "ATT OSCAR");
    address.getValues().put(Address.Line4, "STREET NAME");
    address.getValues().put(Address.Line5, "CITY                STS POSTCODE");
    // address.getValues().put(Address.Line6, "<PH> CBS CBPOST");

    FindCMRRecordModel record = new FindCMRRecordModel();
    ANZHandler handler = new ANZHandler();
    handler.handleWTAASAddressImport(null, SystemLocation.AUSTRALIA, null, record, address);

    record = new FindCMRRecordModel();
    record.setCmrName3("NAME3");
    // record.setCmrName4("NAME4");
    record.setCmrStreetAddress("STREET");
    handler.handleRDcRecordValues(record);
    System.out.println("Street: " + record.getCmrStreetAddress());
    System.out.println("Street Con't: " + record.getCmrStreetAddressCont());
    System.out.println("Dept: " + record.getCmrDept());
  }

  @Override
  protected void handleWTAASAddressImport(EntityManager entityManager, String cmrIssuingCntry, FindCMRRecordModel mainRecord,
      FindCMRRecordModel record, WtaasAddress address) {

    String line1 = address.get(Address.Line1);
    String line2 = address.get(Address.Line2);
    String line3 = address.get(Address.Line3);
    String line4 = address.get(Address.Line4);
    String line5 = address.get(Address.Line5);
    String line6 = address.get(Address.Line6);
    List<String> linesToCheck = Arrays.asList(line2, line3, line4, line5, line6);

    // line 1 - always customer name
    if (SystemLocation.AUSTRALIA.equals(cmrIssuingCntry)) {
      record.setCmrName1Plain(line1.trim());
    } else if (SystemLocation.NEW_ZEALAND.equals(cmrIssuingCntry)) {
      // only get name if it's mailing
      linesToCheck = Arrays.asList(line1, line2, line3, line4, line5, line6);
      if (address.getAddressUse().contains("1")) {
        record.setCmrName1Plain(line1.trim());
      }
    }

    List<String> lines = new ArrayList<String>();

    for (String line : linesToCheck) {
      if (!StringUtils.isEmpty(line)) {
        lines.add(line.trim());
      }
    }
    // max line count = 5, lines list size max = 4
    int lineCount = lines.size() + 1;

    // last line is always city + state(21) + postal code(25) for AUS and city +
    // postal code(25)
    String line = lines.get(lines.size() - 1);

    String city = null;
    String state = null;
    String postalCode = null;

    boolean crossBorder = line.startsWith("<");
    if (crossBorder) {
      // cross border handling
      String[] data = extractCountry(line6);
      if (data[0] != null) {
        String landCntry = getCountryCode(entityManager, data[0]);
        if (landCntry != null) {
          LOG.debug("Setting cross border country to " + landCntry);
          record.setCmrCountryLanded(landCntry);
        }
      }
      if (data[1] != null) {
        if (data[1].contains(" ")) {
          state = data[1].substring(0, data[1].indexOf(" ")).trim();
          postalCode = data[1].substring(data[1].indexOf(" ") + 1).trim();
          record.setCmrState(state);
          record.setCmrPostalCode(postalCode);
        } else {
          record.setCmrPostalCode(data[1].trim());
        }
      }
    } else {
      line = StringUtils.rightPad(line, 30, ' ');
      if (SystemLocation.AUSTRALIA.equals(cmrIssuingCntry)) {
        city = line.substring(0, 20).trim();
        state = line.substring(20, 24).trim();
      } else if (SystemLocation.NEW_ZEALAND.equals(cmrIssuingCntry)) {
        city = line.substring(0, 24).trim();
        state = null;
      }
      postalCode = line.substring(24).trim();

      record.setCmrCity(city);
      record.setCmrState(!StringUtils.isEmpty(state) ? state : null);
      record.setCmrPostalCode(!StringUtils.isEmpty(postalCode) ? postalCode : null);
      record.setCmrCountryLanded(LANDED_CNTRY_MAP.get(cmrIssuingCntry));
    }

    // second to last line is always street
    line = lines.get(lines.size() - 2);
    record.setCmrStreetAddress(line);

    line = null;
    // handle possibilities of 4 or 5 inputs
    if (lineCount == 5) {
      // name, name con't, attn/street con't, street, city
      line = lines.get(0);
      record.setCmrName2Plain(line);

      line = lines.get(1);
    } else if (lineCount == 4) {
      // name, name con't OR attn/street con't, street, city
      line = lines.get(0);
    }
    if (!StringUtils.isEmpty(line)) {
      if (isStreet(line)) {
        record.setCmrStreetAddressCont(line);
      } else if (isAttn(line)) {
        record.setCmrDept(line);
      } else {
        // cannot be mapped, put at street con't
        record.setCmrStreetAddressCont(line);
      }

    }

    if (StringUtils.isBlank(record.getCmrName1Plain()) && SystemLocation.NEW_ZEALAND.equals(cmrIssuingCntry)) {
      // handle special case in NZ when address does not have name
      record.setCmrName1Plain(mainRecord.getCmrName1Plain());
      record.setCmrName2Plain(mainRecord.getCmrName2Plain());
    }
    logExtracts(record);

  }

  @Override
  protected String getMappedAddressType(String country, String rdcType, String addressSeq) {
    switch (country) {
    case SystemLocation.AUSTRALIA:
      if (StringUtils.isNumeric(addressSeq)) {
        addressSeq = StringUtils.leftPad(addressSeq, 2, '0');
      }
      if ("ZS01".equals(rdcType) && "07".equals(addressSeq)) {
        return "ZS01"; // contract
      }
      if ("ZP01".equals(rdcType) && "01".equals(addressSeq)) {
        return "ZP01"; // bill to
      }
      if ("ZI01".equals(rdcType) && "02".equals(addressSeq)) {
        return "ZI01"; // install at
      }
      if ("ZI01".equals(rdcType) && "03".equals(addressSeq)) {
        return "ZF01"; // shipment
      }
      if ("ZI01".equals(rdcType) && "G".equals(addressSeq)) {
        return "CTYG"; // address G
      }
      if ("ZP01".equals(rdcType) && "H".equals(addressSeq)) {
        return "CTYH"; // address H
      }
      return null;
    case SystemLocation.NEW_ZEALAND:
      if (StringUtils.isNumeric(addressSeq)) {
        addressSeq = StringUtils.leftPad(addressSeq, 2, '0');
      }
      if ("ZS01".equals(rdcType) && "02".equals(addressSeq)) {
        return "ZS01"; // install
      }
      if ("ZP01".equals(rdcType) && "01".equals(addressSeq)) {
        return "ZP01"; // bill
      }
      if ("ZI01".equals(rdcType) && "09".equals(addressSeq)) {
        return "ZI01"; // ship
      }
      if ("ZI01".equals(rdcType) && "03".equals(addressSeq)) {
        return "ZF01"; // software shipment, not yet mapped in LOV
      }
      if ("ZI01".equals(rdcType) && "G".equals(addressSeq)) {
        return "CTYG"; // address G, not yet mapped in LOV
      }
      if ("ZP01".equals(rdcType) && "H".equals(addressSeq)) {
        return "CTYH"; // address G, not yet mapped in LOV
      }
      return null;
    }
    return null;
  }

  @Override
  public String getMappedAddressUse(String country, String createCmrAddrType) {
    switch (country) {
    case SystemLocation.AUSTRALIA:
      switch (createCmrAddrType) {
      case "ZP01":
        // Billing
        return "2";
      case "ZS01":
        // Contract
        return "B";
      case "CTYG":
        // Ctry Use G
        return "G";
      case "CTYH":
        // Ctry Use H
        return "H";
      case "EDUC":
        // Education
        return "D";
      case "ZI01":
        // Installing
        return "3";
      case "MAIL":
        // Mailing
        return "A";
      case "PUBB":
        // Publication Bill to
        return "7";
      case "PUBS":
        // Publication Ship To
        return "6";
      case "ZF01":
        // Software shipment
        return "5";
      case "STAT":
        // Statement
        return "1";
      }
      return null;
    case SystemLocation.NEW_ZEALAND:
      switch (createCmrAddrType) {
      case "ZP01":
        // Billing
        return "2";
      case "ZS01":
        // Installing
        return "3";
      case "MAIL":
        // Mailing
        return "1";
      case "XXXX":
        // Mailing
        return "1";
      case "ZI01":
        // Shipping
        return "4";
      case "ZF01":
        // Software shipment
        return "5";
      case "CTYG":
        // Ctry Use G
        return "G";
      case "CTYH":
        // Ctry Use H
        return "H";
      }
      return null;
    }
    return null;
  }

  @Override
  public boolean shouldAddWTAASAddess(String country, WtaasAddress address) {

    boolean shouldAddWTAASAddr = false;
    if (SystemLocation.AUSTRALIA.equalsIgnoreCase(country)) {
      for (String addrUse : Arrays.asList(AU_SUPPORTED_ADDRESS_USES)) {
        if (address.getAddressUse().contains(addrUse)) {
          shouldAddWTAASAddr = true;
        }
      }
    }
    if (SystemLocation.NEW_ZEALAND.equalsIgnoreCase(country)) {
      for (String addrUse : Arrays.asList(NZ_SUPPORTED_ADDRESS_USES)) {
        if (address.getAddressUse().contains(addrUse)) {
          shouldAddWTAASAddr = true;
        }
      }
    }
    return shouldAddWTAASAddr;
  }

  @Override
  public String getAddrTypeForWTAASAddrUse(String country, String wtaasAddressUse) {
    String[] uses = wtaasAddressUse.split("");
    for (String use : uses) {
      if (!StringUtils.isBlank(use)) {
        switch (country) {
        case SystemLocation.AUSTRALIA:
          switch (use) {
          case "2":
            // Billing
            return "ZP01";
          case "B":
            // Contract
            return "ZS01";
          case "G":
            // Ctry Use G
            return "CTYG";
          case "H":
            // Ctry Use H
            return "CTYH";
          case "D":
            // Education
            return "EDUC";
          case "3":
            // Installing
            return "ZI01";
          case "A":
            // Mailing
            return "MAIL";
          case "7":
            // Publication Bill to
            return "PUBB";
          case "6":
            // Publication Ship To
            return "PUBS";
          case "5":
            // Software shipment
            return "ZF01";
          case "1":
            // Statement
            return "STAT";
          }
          return null;
        case SystemLocation.NEW_ZEALAND:
          switch (use) {
          case "2":
            // Billing
            return "ZP01";
          case "3":
            // Installing
            return "ZS01";
          case "1":
            // Mailing
            return "MAIL";
          case "4":
            // Shipping
            return "ZI01";
          case "5":
            // Software shipment
            return "ZF01";
          case "G":
            // Ctry Use G
            return "CTYG";
          case "H":
            // Ctry Use H
            return "CTYH";
          }
          return null;
        }
      }
    }
    return null;
  }

  // special handling for NZ Mailing

  @Override
  public void doBeforeAddrSave(EntityManager entityManager, Addr addr, String cmrIssuingCntry) throws Exception {
    Addr mailing = getAddressByType(entityManager, "MAIL", addr.getId().getReqId());
    if (SystemLocation.NEW_ZEALAND.equals(cmrIssuingCntry)) {
      if (mailing == null) {
        // create a dummy mailing
        AddrPK pk = new AddrPK();
        pk.setReqId(addr.getId().getReqId());
        pk.setAddrType("XXXX");
        pk.setAddrSeq("X");

        mailing = entityManager.find(Addr.class, pk);
        if (mailing == null) {
          mailing = new Addr();
          mailing.setId(pk);
          mailing.setDplChkResult(CmrConstants.ADDRESS_Not_Required);

          LOG.debug("Creating dummy mailing address..");
          entityManager.persist(mailing);
          entityManager.flush();
        }
      }

      AdminPK adminPK = new AdminPK();
      adminPK.setReqId(addr.getId().getReqId());
      Admin admin = entityManager.find(Admin.class, adminPK);
      if (admin.getReqType().equals("C") && !addr.getLandCntry().equalsIgnoreCase("NZ")) {
        if (addr.getPostCd().length() > 6) {
          addr.setPostCd("0121");
        }
      }
    }
  }

  private Addr getAddressByType(EntityManager entityManager, String addrType, long reqId) {
    String sql = ExternalizedQuery.getSql("ADDRESS.GET.BYTYPE");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("REQ_ID", reqId);
    query.setParameter("ADDR_TYPE", addrType);
    List<Addr> addrList = query.getResults(1, Addr.class);
    if (addrList != null && addrList.size() > 0) {
      return addrList.get(0);
    }
    return null;
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

  @Override
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

}
