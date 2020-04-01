/**
 *
 */
package com.ibm.cio.cmr.request.util.dnb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.ibm.cio.cmr.request.CmrException;
import com.ibm.cio.cmr.request.automation.AutomationEngineData;
import com.ibm.cio.cmr.request.automation.RequestData;
import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.entity.Addr;
import com.ibm.cio.cmr.request.entity.Admin;
import com.ibm.cio.cmr.request.entity.Data;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.ui.PageManager;
import com.ibm.cio.cmr.request.util.CompanyFinder;
import com.ibm.cio.cmr.request.util.RequestUtils;
import com.ibm.cio.cmr.request.util.SystemLocation;
import com.ibm.cio.cmr.request.util.geo.GEOHandler;
import com.ibm.cmr.services.client.CmrServicesFactory;
import com.ibm.cmr.services.client.MatchingServiceClient;
import com.ibm.cmr.services.client.ValidatorClient;
import com.ibm.cmr.services.client.dnb.DnBCompany;
import com.ibm.cmr.services.client.dnb.DnbData;
import com.ibm.cmr.services.client.dnb.DnbOrganizationId;
import com.ibm.cmr.services.client.matching.MatchingResponse;
import com.ibm.cmr.services.client.matching.dnb.DnBMatchingResponse;
import com.ibm.cmr.services.client.matching.gbg.GBGFinderRequest;
import com.ibm.cmr.services.client.validator.ValidationResult;
import com.ibm.cmr.services.client.validator.VatValidateRequest;

/**
 * Class that contains utilities for D&B records
 *
 * @author JeffZAMORA
 *
 */
public class DnBUtil {

  private static final Logger LOG = Logger.getLogger(DnBUtil.class);
  private static Map<String, Map<String, String>> orgIdMap = new HashMap<>();
  public static final String CODE_VAT = "VAT";
  public static final String CODE_TAX_CODE_1 = "TAX_CD1";
  public static final String CODE_SIREN = "SIREN";

  static {
    // register here all DNB code is relevant per country

    registerDnBVATCode("BE", 99); // VAT Number
    registerDnBVATCode("DE", 6867); // VAT Number
    registerDnBVATCode("ES", 2472); // VAT Number
    registerDnBVATCode("FR", 2080); // VAT Number
    registerDnBTaxCd1Code("FR", 2081); // SIRET
    registerOtherDnBCode("FR", CODE_SIREN, 2078); // SIREN
    registerDnBVATCode("IT", 481); // VAT Number
    registerDnBVATCode("LU", 480); // VAT Number
    registerDnBVATCode("NL", 6273); // VAT Number
    registerDnBVATCode("IN", 0); // Unknown
    registerDnBVATCode("RU", 1437); // Tax Registration Number (Russian
                                    // Federation)
    registerDnBVATCode("CH", 28865); // Swiss Uniform Identification Number
    registerDnBVATCode("SE", 1861); // SW Business Registration Number
    registerOtherDnBCode("SG", "SG_REG_NO", 1386); // Singapore Registration
                                                   // File Number
    registerDnBVATCode("IL", 1365); // Israel Registration Number
    registerDnBVATCode("JP", 32475); // Corporate Number
    registerDnBVATCode("CN", 22958); // Business Registration Number
    registerDnBVATCode("BR", 1340); // Brazilian General Record of Taxpayers
    registerDnBVATCode("AU", 17891); // Business Number (Australia)
    registerDnBVATCode("AD", 1332); // Andorra Fiscal Code
    registerDnBVATCode("AF", 32824); //  Business Registration Number
    registerDnBVATCode("AL", 9341); // Business Registration Number (Albania)
    registerDnBVATCode("AM", 14215); // Government Gazette Number (Armenia)
    registerDnBVATCode("AR", 1334); // Argentinian Unique Tax Identifier Key
    registerDnBVATCode("AT", 1423); // Value Added Tax (Austria)
    registerDnBVATCode("AZ", 17284); // Tax Registration Number (Azerbaijan)
    registerDnBVATCode("BA", 17282); // Tax Registration Number (Bosnia
                                     // Herzegovina)
    registerDnBVATCode("BG", 1425); // Tax Registration Number (Bulgaria)
    registerDnBVATCode("CL", 1344); // Chilean Unique Tax Identifier
    registerDnBVATCode("CY", 17283); // Tax Registration Number (Cyprus)
    registerDnBVATCode("CZ", 1351); // Czech Republic VAT Number
    registerDnBVATCode("DK", 521); // CVR-no
    registerDnBVATCode("EE", 1428); // Tax Registration Number (Estonia)
    registerDnBVATCode("EG", 9330); // Tax Registration Number (Egypt)
    registerDnBVATCode("FI", 553); // Company House Registration Number
    registerDnBVATCode("GL", 521); // CVR-no
    registerDnBVATCode("GR", 14259); // Tax Registration Number (Greece)
    registerDnBVATCode("HK", 1357); // Hong Kong Business Registration Number
    registerDnBVATCode("HR", 14257); // Tax Registration Number (Croatia)
    registerDnBVATCode("HU", 1361); // Hungarian VAT Number (Hungary)
    registerDnBVATCode("KG", 17285); // Tax Registration Number (Kirghizia)
    registerDnBVATCode("KR", 1387); // South Korean Commercial Registry
                                    // Businesss Number
    registerDnBVATCode("KZ", 9380); // Business Registration Number (Kazakhstan)
    registerDnBVATCode("LB", 9385); // Business Registration Number (Lebanon)
    registerDnBVATCode("LS", 15168); // Tax Registration Number (ZA)
    registerDnBVATCode("LT", 9388); // Business Registration Number (Lithuania)
    registerDnBVATCode("LV", 1431); // Tax Registration Number (Latvia)
    registerDnBVATCode("ME", 0); // Unknown
    registerDnBVATCode("MK", 17277); // Tax Registration Number (Former Yugoslav
                                     // Rep of Macedonia)
    registerDnBVATCode("MT", 9336); // Tax Registration Number (Malta)
    registerDnBVATCode("MU", 9394); // Business Registration Number (Mauritius)
    registerDnBVATCode("NA", 15168); // Tax Registration Number (ZA)
    registerDnBVATCode("NO", 1699); // Register of Business Enterprises Number
    registerDnBVATCode("NZ", 578); // New Zealand Company Number
    registerDnBVATCode("PE", 1382); // Peruvian Sole Commercial Registry Number
    registerDnBVATCode("PF", 17890); // Business Registration Number (Australia)
    registerDnBVATCode("PL", 1385); // Polish Tax Identifier
    registerDnBVATCode("PT", 11659); // Chamber Of Commerce Number
    registerDnBVATCode("PY", 1381); // Paraguayan Unique Tax Registration
    registerDnBVATCode("RO", 17278); // Tax Registration Number (Romania)
    registerDnBVATCode("RS", 0); // Unknown
    registerDnBVATCode("RW", 9404); // Business Registration Number (Rwanda)
    registerDnBVATCode("SI", 1439); // Tax Registration Number (Slovenia)
    registerDnBVATCode("SK", 0); // Unknown
    registerDnBVATCode("TH", 1391); // Registration Number (Thailand)
    registerDnBVATCode("TJ", 14260); // Tax Registration Number (Tajikistan)
    registerDnBVATCode("TR", 1442); // Tax Registration Number (Turkey)
    registerDnBVATCode("TW", 1390); // Taiwan Business Registration Number
    registerDnBVATCode("UA", 9431); // Government Gazette Number (Ukraine)
    registerDnBVATCode("UY", 1394); // Uruguayan Unique Tax Registration
    registerDnBVATCode("UZ", 17279); // Tax Registration Number (Uzbekistan)
    registerDnBVATCode("VE", 1396); // Venezuelan Registry of Fiscal Information
    registerDnBVATCode("VN", 1397); // Business Registration Number (Vietnam)

  }

  /**
   * Creates a {@link FindCMRRecordModel} object containing the details of the
   * D&B record with the specified DUNS No.
   *
   * @param issuingCntry
   * @param dunsNo
   * @param postalCode
   * @return
   * @throws Exception
   */
  public static FindCMRRecordModel extractRecordFromDnB(String issuingCntry, String dunsNo, String postalCode) throws Exception {
    DnbData dnb = CompanyFinder.getDnBDetails(dunsNo);
    if (dnb == null || dnb.getResults() == null || dnb.getResults().isEmpty()) {
      throw new Exception("D&B record for DUNS " + dunsNo + " cannot be retrieved.");
    }
    DnBCompany company = dnb.getResults().get(0);
    if ("O".equals(company.getOperStatusCode())) {
      throw new CmrException(new Exception("The company is Out of Business based on D&B records."));
    }

    // covert from D&B data to FindCMR model
    FindCMRRecordModel cmrRecord = new FindCMRRecordModel();
    cmrRecord.setCmrIssuedBy(issuingCntry);
    cmrRecord.setCmrIsic(company.getIbmIsic());
    cmrRecord.setCmrDuns(company.getDunsNo());
    GEOHandler geoHandler = RequestUtils.getGEOHandler(issuingCntry);
    if (geoHandler != null) {
      int splitLength = geoHandler.getName1Length();
      int splitLength2 = geoHandler.getName2Length();
      String[] parts = geoHandler.doSplitName(company.getCompanyName(), "", splitLength, splitLength2);
      cmrRecord.setCmrName1Plain(parts[0]);
      cmrRecord.setCmrName2Plain(parts[1]);
    } else {
      if (company.getCompanyName().length() > 30) {
        cmrRecord.setCmrName1Plain(company.getCompanyName().substring(0, 30).toUpperCase());
        cmrRecord.setCmrName2Plain(company.getCompanyName().substring(30).toUpperCase());
      } else {
        cmrRecord.setCmrName1Plain(company.getCompanyName().toUpperCase());
      }
    }
    cmrRecord
        .setCmrCountryLanded(company.getPrimaryCountry() != null ? company.getPrimaryCountry() : PageManager.getDefaultLandedCountry(issuingCntry));
    cmrRecord.setCmrStreetAddress(company.getPrimaryAddress() != null ? company.getPrimaryAddress() : company.getMailingAddress());
    cmrRecord.setCmrStreet(cmrRecord.getCmrStreetAddress());
    cmrRecord.setCmrStreetAddressCont(company.getPrimaryAddressCont() != null ? company.getPrimaryAddressCont()
        : (company.getPrimaryAddress() == null ? company.getMailingAddressCont() : ""));

    cmrRecord.setCmrCity(company.getPrimaryCity() != null ? company.getPrimaryCity() : company.getMailingCity());
    cmrRecord.setCmrState(company.getPrimaryStateCode() != null ? company.getPrimaryStateCode() : company.getMailingStateCode());
    if (cmrRecord.getCmrState() != null && cmrRecord.getCmrState().length() > 3) {
      cmrRecord.setCmrState(null);
    }
    cmrRecord.setCmrPostalCode(company.getPrimaryPostalCode() != null ? company.getPrimaryPostalCode() : company.getMailingPostalCode());
    if (cmrRecord.getCmrPostalCode() == null) {
      cmrRecord.setCmrPostalCode(postalCode);

    }
    cmrRecord.setCmrCustPhone(company.getPhoneNo() != null ? company.getPhoneNo().replaceAll("[^0-9]", "") : null);
    cmrRecord.setCmrAddrTypeCode("ZS01");
    // org id
    String country = cmrRecord.getCmrCountryLanded();
    List<DnbOrganizationId> orgIds = company.getOrganizationIdDetails();
    if (orgIds != null && !orgIds.isEmpty()) {
      List<DnbOrganizationId> dnbOrgIds = new ArrayList<DnbOrganizationId>();
      dnbOrgIds.addAll(orgIds);
      Collections.sort(dnbOrgIds, new OrgIdComparator());
      String vat = DnBUtil.getVAT(country, dnbOrgIds);
      if (!StringUtils.isBlank(vat)) {
        // do 2 VAT checks
        boolean vatValid = validateVAT(country, vat);
        if (!vatValid && !vat.startsWith(country)) {
          vatValid = validateVAT(country, country + vat);
          if (vatValid) {
            vat = country + vat;
          }
        }
        cmrRecord.setCmrVat(vat);
      }
      String taxCd1 = DnBUtil.getTaxCode1(country, dnbOrgIds);
      cmrRecord.setCmrBusinessReg(taxCd1);
      LOG.debug("VAT: " + vat + " Tax Code 1: " + taxCd1);
    }

    return cmrRecord;
  }

  /**
   * Gets the DnB code for the equivalent of the VAT field
   *
   * @param country
   * @return
   */
  public static String getVatCode(String country) {
    return getDnBCode(country, CODE_VAT);
  }

  /**
   * Gets the DnB code for the equivalent of the TAX_CD1 field
   *
   * @param country
   * @return
   */
  public static String getTaxCd1Code(String country) {
    return getDnBCode(country, CODE_TAX_CODE_1);
  }

  /**
   * Gets the mapped D&B code for the country for the given code indicator
   *
   * @param country
   * @param codeKey
   * @return
   */

  public static String getDnBCode(String country, String codeKey) {
    if (orgIdMap.get(country) == null) {
      return null;
    }
    return orgIdMap.get(country).get(codeKey);
  }

  /**
   * Checks if the D&B code is registered as a relevant code
   *
   * @param country
   * @param dnbId
   * @return
   */
  public static boolean isRelevant(String country, DnbOrganizationId dnbId) {
    String code = dnbId.getDnbCode();
    code = StringUtils.leftPad("" + code, 6, '0');
    if (orgIdMap.get(country) == null) {
      return false;
    }
    return orgIdMap.get(country).containsValue(code);

  }

  /**
   * Checks if the D&B ID matches the registered code
   *
   * @param country
   * @param dnbId
   * @return
   */
  public static boolean matchesCode(String country, String codeKey, DnbOrganizationId dnbId) {
    String dnbCode = dnbId.getDnbCode();
    dnbCode = StringUtils.leftPad("" + dnbCode, 6, '0');
    if (orgIdMap.get(country) == null) {
      return false;
    }
    String registeredDnBCode = orgIdMap.get(country).get(codeKey);
    return registeredDnBCode != null && registeredDnBCode.equals(dnbCode);
  }

  /**
   * Extracts the relevant VAT value from the list of {@link DnbOrganizationId}
   *
   * @param country
   * @param ids
   * @return
   */
  public static String getVAT(String country, List<DnbOrganizationId> ids) {
    return getCodeValue(country, CODE_VAT, ids);
  }

  /**
   * Extracts the relevant TAX_CD1 value from the list of
   * {@link DnbOrganizationId}
   *
   * @param country
   * @param ids
   * @return
   */
  public static String getTaxCode1(String country, List<DnbOrganizationId> ids) {
    return getCodeValue(country, CODE_TAX_CODE_1, ids);
  }

  /**
   * Gets the specific value from the list of {@link DnbOrganizationId} that
   * matches the codeKey
   *
   * @param country
   * @param codeKey
   * @param ids
   * @return
   */
  public static String getCodeValue(String country, String codeKey, List<DnbOrganizationId> ids) {
    for (DnbOrganizationId id : ids) {
      if (matchesCode(country, codeKey, id)) {
        return id.getOrganizationIdCode();
      }
    }
    return null;
  }

  /**
   * Sets the D&B code as the VAT equivalent
   *
   * @param country
   * @param dnbCodeId
   */
  private static void registerDnBVATCode(String country, int dnbCodeId) {
    if (orgIdMap.get(country) == null) {
      orgIdMap.put(country, new HashMap<String, String>());
    }
    orgIdMap.get(country).put("VAT", StringUtils.leftPad("" + dnbCodeId, 6, '0'));
  }

  /**
   * Sets the D&B code as the Tax Code 1 equivalent
   *
   * @param country
   * @param dnbCodeId
   */
  private static void registerDnBTaxCd1Code(String country, int dnbCodeId) {
    if (orgIdMap.get(country) == null) {
      orgIdMap.put(country, new HashMap<String, String>());
    }
    orgIdMap.get(country).put("TAX_CD1", StringUtils.leftPad("" + dnbCodeId, 6, '0'));
  }

  /**
   * Sets the D&B code as an extra tracked field
   *
   * @param country
   * @param dnbCodeId
   */
  private static void registerOtherDnBCode(String country, String code, int dnbCodeId) {
    if (orgIdMap.get(country) == null) {
      orgIdMap.put(country, new HashMap<String, String>());
    }
    orgIdMap.get(country).put(code, StringUtils.leftPad("" + dnbCodeId, 6, '0'));
  }

  /**
   * Connects to VAT validation service and validates VAT
   *
   * @param country
   * @param vat
   * @return
   * @throws Exception
   */
  private static boolean validateVAT(String country, String vat) throws Exception {
    LOG.debug("Validating VAT " + vat + " for " + country);
    String baseUrl = SystemConfiguration.getValue("CMR_SERVICES_URL");
    ValidatorClient client = CmrServicesFactory.getInstance().createClient(baseUrl, ValidatorClient.class);
    VatValidateRequest vatRequest = new VatValidateRequest();
    vatRequest.setCountry(country);
    vatRequest.setVat(vat);

    try {
      ValidationResult validation = client.executeAndWrap(ValidatorClient.VAT_APP_ID, vatRequest, ValidationResult.class);
      return validation.isSuccess();
    } catch (Exception e) {
      LOG.error("Error in VAT validation", e);
      return false;
    }
  }

  /**
   * Does a {@link StringUtils#getLevenshteinDistance(String, String)}
   * comparison of the address data against the DnB record and determines if
   * they match
   *
   * @param handler
   * @param admin
   * @param addresses
   * @param dnbRecord
   * @return
   */
  public static boolean closelyMatchesDnb(String country, Addr addr, Admin admin, DnBMatchingResponse dnbRecord) {

    boolean result = true;

    GEOHandler handler = RequestUtils.getGEOHandler(country);
    if (StringUtils.isNotBlank(getCustomerName(handler, admin, addr)) && StringUtils.isNotBlank(dnbRecord.getDnbName())
        && StringUtils.getLevenshteinDistance(getCustomerName(handler, admin, addr).toUpperCase(), dnbRecord.getDnbName().toUpperCase()) > 16) {
      result = false;
    }
    String address = addr.getAddrTxt() != null ? addr.getAddrTxt() : "";
    address += StringUtils.isNotBlank(addr.getAddrTxt2()) ? " " + addr.getAddrTxt2() : "";
    address = address.trim();

    String dnbAddress = dnbRecord.getDnbStreetLine1() != null ? dnbRecord.getDnbStreetLine1() : "";
    dnbAddress += StringUtils.isNotBlank(dnbRecord.getDnbStreetLine2()) ? " " + dnbRecord.getDnbStreetLine2() : "";
    dnbAddress = dnbAddress.trim();

    if (StringUtils.isNotBlank(address) && StringUtils.isNotBlank(dnbAddress)
        && StringUtils.getLevenshteinDistance(address.toUpperCase(), dnbAddress.toUpperCase()) > 8) {
      result = false;
    }
    if (StringUtils.isNotBlank(addr.getPostCd()) && StringUtils.isNotBlank(dnbRecord.getDnbPostalCode())
        && StringUtils.getLevenshteinDistance(addr.getPostCd().toUpperCase(), dnbRecord.getDnbPostalCode().toUpperCase()) > 2) {
      result = false;
    }
    if (StringUtils.isNotBlank(addr.getCity1()) && StringUtils.isNotBlank(dnbRecord.getDnbCity())
        && StringUtils.getLevenshteinDistance(addr.getCity1().toUpperCase(), dnbRecord.getDnbCity().toUpperCase()) > 6) {
      result = false;
    }

    return result;
  }

  /**
   * Does a {@link StringUtils#getLevenshteinDistance(String, String)}
   * comparison of the address data against the DnB record and determines if
   * they match
   *
   * @param addr
   * @param handler
   * @param admin
   * @param addresses
   * @param dnbRecord
   * @return
   */
  public static boolean closelyMatchesDnb(String country, Addr addr, Admin admin, DnBCompany dnbRecord) {

    DnBMatchingResponse dnbTemp = new DnBMatchingResponse();
    dnbTemp.setDnbName(dnbRecord.getCompanyName());
    dnbTemp.setDnbStreetLine1(dnbRecord.getPrimaryAddress() == null ? dnbRecord.getMailingAddress() : dnbRecord.getPrimaryAddress());
    dnbTemp.setDnbStreetLine2(dnbRecord.getPrimaryAddress() == null ? dnbRecord.getMailingAddressCont() : dnbRecord.getPrimaryAddressCont());
    dnbTemp.setDnbPostalCode(dnbRecord.getPrimaryAddress() == null ? dnbRecord.getMailingPostalCode() : dnbRecord.getPrimaryPostalCode());
    dnbTemp.setDnbCity(dnbRecord.getPrimaryAddress() == null ? dnbRecord.getMailingCity() : dnbRecord.getPrimaryCity());

    return closelyMatchesDnb(country, addr, admin, dnbTemp);
  }

  /**
   * returns concatenated customerName from admin or address as per country
   * settings
   *
   * @param handler
   * @param admin
   * @param soldTo
   * @return
   */
  private static String getCustomerName(GEOHandler handler, Admin admin, Addr soldTo) {
    String customerName = null;
    if (!handler.customerNamesOnAddress()) {
      customerName = admin.getMainCustNm1() + (StringUtils.isBlank(admin.getMainCustNm2()) ? "" : " " + admin.getMainCustNm2());
    } else {
      customerName = soldTo.getCustNm1() + (StringUtils.isBlank(soldTo.getCustNm2()) ? "" : " " + soldTo.getCustNm2());
    }
    return customerName;
  }

  /**
   * checks if a DnB response contains valid matches i.e., Confidence Code>7
   * 
   * @param response
   * @return
   */
  public static boolean hasValidMatches(MatchingResponse<DnBMatchingResponse> response) {
    if (response.getSuccess() && response.getMatched() && !response.getMatches().isEmpty()) {
      for (DnBMatchingResponse dnbRecord : response.getMatches()) {
        if (dnbRecord.getConfidenceCode() > 7) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 
   * gets DnB matches on the basis of input data for the provided addr type
   * 
   * @param handler
   * @param requestData
   * @param engineData
   * @param addrType
   * @return
   * @throws Exception
   */
  public static MatchingResponse<DnBMatchingResponse> getMatches(GEOHandler handler, RequestData requestData, AutomationEngineData engineData,
      String addrType) throws Exception {
    MatchingResponse<DnBMatchingResponse> response = new MatchingResponse<DnBMatchingResponse>();
    Admin admin = requestData.getAdmin();
    Data data = requestData.getData();
    addrType = StringUtils.isNotBlank(addrType) ? addrType : "ZS01";
    Addr addr = requestData.getAddress(addrType);
    GBGFinderRequest request = new GBGFinderRequest();
    request.setMandt(SystemConfiguration.getValue("MANDT"));
    if (addr != null) {
      if (StringUtils.isNotBlank(data.getVat())) {
        request.setOrgId(data.getVat());
      } else if (StringUtils.isNotBlank(addr.getVat())) {
        if (SystemLocation.SWITZERLAND.equalsIgnoreCase(data.getCmrIssuingCntry())) {
          request.setOrgId(addr.getVat().split("\\s")[0]);
        } else {
          request.setOrgId(addr.getVat());
        }
      }

      request.setCity(addr.getCity1());
      if (StringUtils.isBlank(request.getCity()) && SystemLocation.SINGAPORE.equals(data.getCmrIssuingCntry())) {
        // here for now, find a way to move to common class
        request.setCity("SINGAPORE");
      }
      request.setCustomerName(getCustomerName(handler, admin, addr));
      request.setStreetLine1(addr.getAddrTxt());
      request.setStreetLine2(addr.getAddrTxt2());
      request.setLandedCountry(addr.getLandCntry());
      request.setPostalCode(addr.getPostCd());
      request.setStateProv(addr.getStateProv());
      request.setMinConfidence("4");
      MatchingServiceClient client = CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_SERVICES_URL"),
          MatchingServiceClient.class);
      client.setReadTimeout(1000 * 60 * 5);
      LOG.debug("Connecting to the Advanced D&B Matching Service at " + SystemConfiguration.getValue("BATCH_SERVICES_URL"));
      MatchingResponse<?> rawResponse = client.executeAndWrap(MatchingServiceClient.DNB_SERVICE_ID, request, MatchingResponse.class);
      ObjectMapper mapper = new ObjectMapper();
      String json = mapper.writeValueAsString(rawResponse);

      TypeReference<MatchingResponse<DnBMatchingResponse>> ref = new TypeReference<MatchingResponse<DnBMatchingResponse>>() {
      };

      response = mapper.readValue(json, ref);
    }
    return response;
  }

}
