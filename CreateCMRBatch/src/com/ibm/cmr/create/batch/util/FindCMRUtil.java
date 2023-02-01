package com.ibm.cmr.create.batch.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRRecordModel;
import com.ibm.cio.cmr.request.model.requestentry.FindCMRResultModel;
import com.ibm.cmr.create.batch.util.masscreate.SearchResultObject;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

public class FindCMRUtil {

  private static final Logger LOG = Logger.getLogger(FindCMRUtil.class);

  public static FindCMRResultModel findCMRs(String cmrNo, String cmrIssuingCntry) throws Exception {
    LOG.debug("Retrieving current values for CMR No " + cmrNo + " System Location " + cmrIssuingCntry);
    FindCMRResultModel result = new FindCMRResultModel();
    List<SearchResultObject> resultObjects = findCMRsFromService(cmrNo, cmrIssuingCntry);
    List<FindCMRRecordModel> records = new ArrayList<FindCMRRecordModel>();
    FindCMRRecordModel record = null;
    for (SearchResultObject object : resultObjects) {

      record = new FindCMRRecordModel();

      if (object != null) {
        // name1-2
        String name1 = object.getCUST1_NM();
        String name2 = object.getCUST2_NM();
        record.setCmrName(concat(name1, name2, "@"));

        // name3-4
        String name3 = object.getCUST3_NM();
        String name4 = object.getCUST4_NM();
        record.setCmrName2(concat(name3, name4, "@"));

        // main details
        record.setCmrNum(object.getCMR_NO());
        record.setCmrOwner(object.getCMR_OWNER_DESC());
        record.setCmrRevenue(object.getREVENUE_PCT());
        record.setCmrSapNumber(object.getKUNNR());

        // Address Type
        record.setCmrAddrType(object.getADDRESS_TYPE_DESC());
        record.setCmrStreetAddress(object.getADDRESS_TXT());

        // city
        record.setCmrCity(object.getCITY1_NM());
        record.setCmrCity2(object.getCITY2_NM());

        // state
        record.setCmrState(object.getSTATE_PROV_CD());
        record.setCmrStateDesc(object.getSTATE_PROV_NM());

        record.setCmrCounty(object.getCOUNTY_NM());
        record.setCmrPostalCode(object.getPOSTAL_CD());

        // ISU
        record.setIsuCode(object.getISU());
        record.setIsuDescription(object.getISU_DESC());

        // Landed Country
        record.setCmrCountryLanded(object.getLAND_CNTRY_NM());
        record.setCmrCountryLandedDesc(object.getLAND_CNTRY_DESC());

        // international1-2 name concat
        String intlName1 = object.getALTERNATE_LANG_NAME1();
        String intlName2 = object.getALTERNATE_LANG_NAME2();
        record.setCmrIntlName12(concat(intlName1, intlName2, "@"));

        // international3-4 name concat

        String intlName3 = object.getALTERNATE_LANG_NAME3();
        String intlName4 = object.getALTERNATE_LANG_NAME4();
        record.setCmrIntlName34(concat(intlName3, intlName4, "@"));

        // order block
        record.setCmrOrderBlock("BLANK".equals(object.getORDERBLOCK()) ? "" : object.getORDERBLOCK());
        record.setCmrOrderBlockDesc(object.getORDERBLOCKDESC());

        // affiliate number
        record.setCmrAffiliate(object.getAFFILIATE_NO());

        // buying group
        record.setCmrBuyingGroup(object.getBUY_GRP_ID());
        record.setCmrBuyingGroupDesc(object.getBUY_GRP_NM());

        // global buying group
        record.setCmrGlobalBuyingGroup(object.getGBL_BUY_GRP_ID());
        record.setCmrGlobalBuyingGroupDesc(object.getGBL_BUY_GRP_NM());

        // duns number
        record.setCmrDuns(object.getDUNSNO());

        // parent number
        record.setCmrParent(object.getPARENT_DUNS_NO());

        // DU number
        record.setCmrDuNumber(object.getDOM_ULT_DUNS_NO());

        // GU number
        record.setCmrGuNumber(object.getGBL_ULT_DUNS_NO());

        // Domestic client
        record.setCmrDomClient(object.getDOM_CLIENT_ID());
        record.setCmrDomClientDesc(object.getDOM_CLIENT_NAME());

        // Global Client
        record.setCmrGlobClient(object.getGBL_CLIENT_ID());
        record.setCmrGlobClientDesc(object.getGBL_CLIENT_NAME());

        // customer class
        record.setCmrClass(object.getCUST_CLASS_CD());
        record.setCmrClassDesc(object.getCUST_CLASS_DESC());

        // enterprise number
        record.setCmrEnterpriseNumber(object.getENTERPRISE_NO());

        // Issued by (KATR6)
        record.setCmrIssuedBy(object.getSYS_LOCTN_CD());
        record.setCmrIssuedByDesc(object.getSYS_LOCTN_DESC());

        // Intl Street address
        record.setCmrIntlAddress(object.getALTERNATE_LANG_ADDRESS());

        // Intl City1
        record.setCmrIntlCity1(object.getALTERNATE_LANG_CITY1());

        // Intl City2
        record.setCmrIntlCity2(object.getALTERNATE_LANG_CITY2());

        // abbrev name
        record.setCmrShortName(object.getABBRV_EUR_NM());

        // vat
        record.setCmrVat(object.getVAT());

        // business reg
        record.setCmrBusinessReg(object.getBUSINESS_REG());

        // ISIC
        record.setCmrIsic(object.getSIC_CD());
        record.setCmrIsicDesc(object.getSIC_DESC());

        // ISU code and desc
        record.setCmrIsu(object.getISU());
        record.setCmrIsuDesc(object.getISU_DESC());

        // client tier code and desc
        record.setCmrTier(object.getCLIENT_TIER_CD());
        record.setCmrTierDesc(object.getCLIENT_TIER_DESC());

        // sub industry
        record.setCmrSubIndustry(object.getSUB_INDUSTRY());
        record.setCmrSubIndustryDesc(object.getSUB_INDUSTRY_DESC());

        // INAC
        record.setCmrInac(object.getINAC());
        record.setCmrInacDesc(object.getINAC_DESC());

        // company number
        record.setCmrCompanyNo(object.getCOMPANY_NO());

        // SORTL
        record.setCmrSortl(object.getSORTL());

        // Tradestyle
        record.setCmrTradestyle(object.getV_TRADESTYLENAME());

        record.setCmrAddrTypeCode(object.getADDRESS_TYPE());
        record.setCmrIntlName1(intlName1);
        record.setCmrIntlName2(intlName2);
        record.setCmrIntlName3(intlName3);
        record.setCmrIntlName4(intlName4);
        record.setCmrCoverageName(object.getCOV_NAME());
        record.setCmrCoverage(object.getCOV_TYPE_ID());
        record.setCmrName1Plain(name1);
        record.setCmrName2Plain(name2);
        record.setCmrName3(name3);
        record.setCmrName4(name4);
        record.setCmrInacType(object.getINAC_TYPE_CD());
        record.setCmrDelegated(object.getDEL_COV_INDC());
        record.setCmrSitePartyID(object.getSITE_ID());
        record.setCmrRdcCreateDate(object.getRDC_CREATE_DATE());
        record.setCmrCountyCode(object.getCOUNTY_CD());
        record.setCmrCapIndicator(object.getCAP_IND());

        record.setCmrCustPhone(object.getCUST_PHONE());
        record.setCmrCustFax(object.getCUST_FAX());
        record.setCmrPrefLang(object.getPREF_LANG());
        record.setCmrLocalTax2(object.getLOCAL_TAX2());
        record.setCmrSensitiveFlag(object.getSENSITIVE_FLAG());
        record.setCmrTransportZone(object.getTRANSPORT_ZONE());
        record.setCmrPOBox(object.getPO_BOX());
        record.setCmrPOBoxCity(object.getPO_BOX_CITY());
        record.setCmrPOBoxPostCode(object.getPO_BOX_POST_CD());
        record.setCmrPpsceid(object.getPPSCEID());
        record.setCmrBldg(object.getBUILDING());
        record.setCmrFloor(object.getFLOOR());
        record.setCmrOffice(object.getOFFICE());
        record.setCmrDept(object.getDEPARTMENT());
        record.setCmrMembLevel(object.getMEMB_LEVEL());
        record.setCmrBPRelType(object.getBP_REL_TYPE());
        record.setCmrGOEIndicator(object.getGOE_IND());

        /* added for LH requirement, 797865 */
        record.setCmrClientType(object.getCLIENT_TYPE());

        /* added for NDA / Route120 Requirements , 797864 */
        record.setCmrCovClientType(object.getCOV_CLIENT_TYPE());
        record.setCmrCovClientSubType(object.getCOV_CLIENT_SUBTYPE());
        record.setCmrCovClientTypeDesc(object.getCOV_CLIENT_TYPE_DESC());
        record.setCmrCovClientSubTypeDesc(object.getCOV_CLIENT_SUBTYPE_DESC());

        /* 814271 */
        record.setCmrGeoLocCd(object.getGEO_LOC_CD());
        record.setCmrGeoLocDesc(object.getGEO_LOC_DESC());

        /* 825444 - add SADR Lang Code (SPRAS) */
        record.setCmrIntlLangCd(object.getALTERNATE_LANG_CD());
        record.setCmrIntlSubLangCd(object.getALTERNATE_SUB_LANG_CD());
        record.setCmrIntlSubLangDesc(object.getALTERNATE_SUB_LANG_DESC());
        record.setCmrOtherIntlBusinessName(object.getOTH_ALT_LANG_BUS_NAME());
        record.setCmrOtherIntlCity1(object.getOTH_ALT_LANG_CITY1());
        record.setCmrOtherIntlCity2(object.getOTH_ALT_LANG_CITY2());
        record.setCmrOtherIntlAddress(object.getOTH_ALT_LANG_ADDRESS());
        record.setCmrOtherIntlLangCd(object.getOTH_ALT_LANG_CD());
        record.setCmrOtherIntlSubLangCd(object.getOTH_ALT_SUB_LANG_CD());
        record.setCmrOtherIntlSubLangDesc(object.getOTH_ALT_SUB_LANG_DESC());

        /* 825446 - add Global Ultimate Client ID / Name */
        record.setCmrGUClientId(object.getGBL_ULT_CLIENT_ID());
        record.setCmrGUClientName(object.getGBL_ULT_CLIENT_NM());

        /* prospect flag */
        record.setCmrProspect("75".equals(object.getORDERBLOCK()) ? "Y" : "N");

        /* LDE */
        record.setCmrLde(object.getLDE());
        record.setCmrLeadClientRepName(object.getLCR_NAME());
        record.setCmrLeadClientRepNotesId(object.getLCR_NOTES_ID());

        /* 879648 - show business name and lang code in the results */
        record.setCmrBusNm(object.getBUS_NM());
        record.setCmrBusNmLangCd(object.getBUS_NM_LANG_CD());
        record.setCmrBusNmLangDesc(object.getBUS_NM_LANG_NM());
        record.setCmrBusNmNative(object.getBUS_NM_SADR());
        record.setCmrBusNmNativeLangCd(object.getBUS_NM_SADR_LANG_CD());
        record.setCmrBusNmNativeLangDesc(object.getBUS_NM_SADR_LANG_NM());

        /* Base covereage */
        record.setCmrBaseCoverage(object.getBASE_COV_ID());
        record.setCmrBaseCoverageName(object.getBASE_COV_NM());

        /* 960371 - Add Industry Code and Name */
        record.setCmrIndustryCd(object.getINDUSTRY_CD());
        record.setCmrIndustryName(object.getINDUSTRY_DESC());

        /* SaaS changes */
        record.setCmrLite(object.getLIGHT_CMR());
        record.setCmrCoverageEligible(object.getCOVERAGE_ELIGIBLE());
        record.setSearchScore(object.getSEARCH_SCORE() != null ? Float.parseFloat(object.getSEARCH_SCORE()) : 0);

        record.setCmrAddrSeq(object.getADDR_SEQ());

        // CREATCMR-1277 CA MASSCREATE
        record.setCmrSellBoNum(object.getSELLING_BO_NUM());
        record.setCmrInstlBoNum(object.getINSTL_BO_NUM());
        record.setCmrTaxExInd(object.getPROV_TAX_EX_IND());
        record.setCmrEnggBoGrp(object.getENGG_BO_GRP());
        record.setCmrAccRecvBo(object.getACC_RECV_BO());
        record.setCmrCustCreditCode(object.getCUST_CREDIT_CD());
        record.setCmrBillPlnTyp(object.getBILL_PLN_TYP());
        record.setCmrQstNo(object.getQST_ID());
        record.setCmrLicNo(object.getSW_EXEMPT_LIC_NUM());
        record.setCmrTaxExemptReas(object.getTAX_EXEMPT_REASON());
        record.setCmrLeasingInd(object.getLEASING_IND());
        record.setCmrPurOrdNo(object.getPUR_ORD_NO());
        record.setCmrLtPymntInd(object.getLT_PYMNT_IND());
        record.setCmrEstabFnInd(object.getEST_FUNC_CD());
        record.setCmrNoInvc(object.getNO_OF_INVC());
      }
      records.add(record);
    }
    result.setItems(records);
    return result;
  }

  private static List<SearchResultObject> findCMRsFromService(String cmrNo, String cmrIssuingCntry) throws Exception {
    // JSONObject request = new JSONObject();
    // request.put("cmrResultRows", 100);
    // request.put("cmrIssueCategory", cmrIssuingCntry);
    // request.put("cmrNum", cmrNo);
    // SearchClient client =
    // CmrServicesFactory.getInstance().createClient(SystemConfiguration.getValue("BATCH_CI_SERVICES_URL"),
    // SearchClient.class);
    // JSONArray results = client.execute("findcmr", request, JSONArray.class);

    // SearchRequest request = new SearchRequest();
    // request.setCmrResultRows(100);
    // request.setCmrNum(cmrNo);
    // request.setCmrIssueCategory(cmrIssuingCntry);
    // request.setAppId("findcmr");

    // SearchServicesClient client = new
    // SearchServicesClient(SystemConfiguration.getValue("BATCH_CI_SERVICES_URL"));
    // JSONArray results = client.sendRequest(request);

    JSONArray results = fetchFromSearchServicesClient();

    List<SearchResultObject> searchResults = new ArrayList<SearchResultObject>();
    LOG.debug("Parsing JSON results from the service...");

    JSONObject main = null;
    SearchResultObject result = null;
    if (results != null && results.size() > 0) {
      for (int i = 0; i < results.size(); i++) {
        main = (JSONObject) results.get(i);
        result = convertToResultObject(main);
        searchResults.add(result);
      }
    }
    return searchResults;
  }

  private static JSONArray fetchFromSearchServicesClient() {
    String cmrservices = SystemConfiguration.getValue("BATCH_CI_SERVICES_URL");
    String serviceId = SystemConfiguration.getSystemProperty("service.id");
    String servicePwd = SystemConfiguration.getSystemProperty("service.password");
    cmrservices += "/service/search/findcmr";
    cmrservices += "?svcId=" + serviceId + "&svcPwd=" + servicePwd;
    JSONArray result = new JSONArray();
    try {
      URL url = new URL(cmrservices);
      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setConnectTimeout(1000 * 30); // 30 seconds
      InputStream is = conn.getInputStream();
      StringBuffer sb = new StringBuffer();
      try {
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        BufferedReader reader = new BufferedReader(isr);
        String str;
        while ((str = reader.readLine()) != null) {
          sb.append(str);
        }
      } finally {
        is.close();
        conn.disconnect();
      }
      return JSONArray.parse(sb.toString());

    } catch (Exception e) {
      LOG.error("Error when pinging CI Services", e);
    }
    return result;
  }

  public static SearchResultObject convertToResultObject(JSONObject main) throws JSONException, IllegalAccessException, InstantiationException {
    SearchResultObject result = new SearchResultObject();

    JSONObject primary = null;
    primary = (JSONObject) main.get("PRIMARY_CMR_DETAILS");

    result.setABBRV_EUR_NM(getString("ABBRV_NM", main));
    result.setABBRV_US_NM(getString("SEARCH_TERM", main));
    result.setADDRESS_TXT(getString("ADDRESS_STREET", primary));
    result.setADDRESS_TYPE(getString("ADDRESS_TYPE", main));
    result.setADDRESS_TYPE_DESC(getString("ADDRESS_TYPE_DESC", main));
    result.setAFFILIATE_NO(getString("AFFILIATE_NO", main));
    result.setALTERNATE_LANG_ADDRESS(getString("ALTERNATE_LANG_ADDRESS", main));
    result.setALTERNATE_LANG_CITY1(getString("ALTERNATE_LANG_CITY1", main));
    result.setALTERNATE_LANG_CITY2(getString("ALTERNATE_LANG_CITY2", main));
    result.setALTERNATE_LANG_NAME1(getString("ALTERNATE_LANG_NM1", main));
    result.setALTERNATE_LANG_NAME2(getString("ALTERNATE_LANG_NM2", main));
    result.setALTERNATE_LANG_NAME3(getString("ALTERNATE_LANG_NM3", main));
    result.setALTERNATE_LANG_NAME4(getString("ALTERNATE_LANG_NM4", main));
    try {
      result.setAVG_4Y_REVENUE("" + main.get("AVG_4YR_REV_SORT"));
    } catch (Exception e) {
      result.setAVG_4Y_REVENUE("0.00");
    }
    result.setBP_REL_TYPE(getString("BP_REL_TYPE", main));
    result.setBUILDING(getString("BUILDING", main));
    result.setBUSINESS_REG(getString("BUSINESS_REG_NO", main));
    result.setBUY_GRP_ID(getString("BUY_GRP_ID", main));
    result.setBUY_GRP_NM(getString("BUY_GRP_NM", main));
    result.setCAP_IND(getString("CAP_IND", main));
    result.setCITY1_NM(getString("ADDRESS_CITY", primary));
    result.setCITY2_NM(getString("CITY2_NM", main));
    result.setCLIENT_TIER_CD(getString("QUAD_TIER_CD", main));
    result.setCLIENT_TIER_DESC(getString("QUAD_TIER_DESC", main));
    result.setCLIENT_TYPE(getString("CLIENT_TYPE", main));
    result.setCMR_NO(getString("CMR_NO", main));
    result.setCMR_OWNER_DESC(getString("CMR_OWNER_DESC", main));
    result.setCOMPANY_NO(getString("COMPANY_NO", main));
    result.setCOUNTY_CD(getString("ADDRESS_COUNTY_CD", main));
    result.setCOUNTY_NM(getString("ADDRESS_COUNTY_DESC", main));
    result.setCOV_CLIENT_SUBTYPE(getString("COV_CLIENT_SUBTYPE_CD", main));
    result.setCOV_CLIENT_SUBTYPE_DESC(getString("COV_CLIENT_SUBTYPE_DESC", main));
    result.setCOV_CLIENT_TYPE(getString("COV_CLIENT_TYPE_CD", main));
    result.setCOV_CLIENT_TYPE_DESC(getString("COV_CLIENT_TYPE_DESC", main));
    result.setCOV_NAME(getString("COV_NM", main));
    result.setCOV_TYPE_ID(getString("COV_TYPE_ID", main));
    result.setCUST1_NM(getString("CUST_NM1", main));
    result.setCUST2_NM(getString("CUST_NM2", main));
    result.setCUST3_NM(getString("CUST_NM3", main));
    result.setCUST4_NM(getString("CUST_NM4", main));
    result.setCUST_CLASS_CD(getString("CUST_CLASS_CD", main));
    result.setCUST_CLASS_DESC(getString("CUST_CLASS_DESC", main));
    result.setCUST_FAX(getString("FAX_NO", primary));
    result.setCUST_PHONE(getString("PHONE_NO", primary));
    result.setDEL_COV_INDC(getString("DELEGATED_COV_IND", main));
    result.setDEL_INDC(getString("DELETE_IND", main));
    result.setDEPARTMENT(getString("DEPARTMENT", main));
    result.setDOM_CLIENT_ID(getString("DOM_CLIENT_ID", main));
    result.setDOM_CLIENT_NAME(getString("DOM_CLIENT_NM", main));
    result.setDOM_ULT_DUNS_NO(getString("DOM_ULT_DUNS_NO", main));
    result.setDUNSNO(getString("DUNS_NO", main));
    result.setENTERPRISE_NO(getString("ENTERPRISE_NO", main));
    result.setFLOOR(getString("FLOOR", main));
    result.setGBL_BUY_GRP_ID(getString("GBL_BUY_GRP_ID", main));
    result.setGBL_BUY_GRP_NM(getString("GBL_BUY_GRP_NM", main));
    result.setGBL_CLIENT_ID(getString("GBL_CLIENT_ID", main));
    result.setGBL_CLIENT_NAME(getString("GBL_CLIENT_NM", main));
    result.setGBL_ULT_DUNS_NO(getString("GBL_ULT_DUNS_NO", main));
    result.setGOE_IND(getString("GOE_IND", main));
    result.setINAC(getString("INAC_CD", main));
    result.setINAC_DESC(getString("INAC_DESC", main));
    result.setINAC_TYPE_CD(getString("INAC_TYPE", main));
    result.setISU(getString("ISU_CD", main));
    result.setISU_DESC(getString("ISU_DESC", main));
    result.setKUNNR(getString("MPP_NO", main));
    result.setLAND_CNTRY_DESC(getString("ADDRESS_COUNTRY_DESC", main));
    result.setLAND_CNTRY_NM(getString("LANDING_COUNTRY_CD", main));
    result.setLANDING_COUNTRY_CD(getString("LANDING_COUNTRY_CD", main));
    // sro.setLDE_IND(obj.getString("name"));
    result.setLOCAL_TAX2(getString("LOCAL_TAX2", main));
    result.setMEMB_LEVEL(getString("MEMBERSHIP_LEVEL", main));
    result.setNAME(getString("CUST_NM1", main) + "@" + getString("CUST_NM2", main));
    result.setOFFICE(getString("OFFICE", main));
    result.setORDERBLOCK(getString("ORDER_BLOCK_CD", main));
    if ("BLANK".equals(result.getORDERBLOCK())) {
      result.setORDERBLOCK("");
    }
    result.setORDERBLOCKDESC(getString("ORDER_BLOCK_DESC", main));
    result.setPARENT_DUNS_NO(getString("PARENT_DUNS_NO", main));
    result.setPO_BOX(getString("PO_BOX", main));
    result.setPO_BOX_CITY(getString("PO_BOX_CITY", main));
    result.setPO_BOX_POST_CD(getString("PO_BOX_POSTAL_CD", main));
    result.setPOSTAL_CD(getString("ADDRESS_POSTAL_CD", primary));
    result.setPPSCEID(getString("PPSCEID", main));
    result.setPREF_LANG(getString("LANGUAGE_CD", primary));
    result.setRDC_CREATE_DATE(getString("RDC_CREATE_DATE", main));
    try {
      result.setREVENUE_SUM("" + main.get("SUM_4Y_REVENUE"));
    } catch (Exception e) {

    }
    result.setSAP_TS(getString("SAP_TS", main));
    result.setSENSITIVE_FLAG(getString("SECURITY_CLASS_CD", main));
    result.setSHAD_UPDATE_TS(getString("SHAD_UPDATE_TS", main));
    result.setSIC_CD(getString("INDUSTRY_SIC_CD", main));
    result.setSIC_DESC(getString("INDUSTRY_SIC_DESC", main));
    result.setSITE_ID(getString("SITE_ID", main));
    result.setSORTL(getString("SEARCH_TERM", main));
    result.setSTATE_PROV_CD(getString("ADDRESS_STATE_CD", primary));
    result.setSTATE_PROV_NM(getString("ADDRESS_STATE_DESC", main));
    result.setSUB_INDUSTRY(getString("SUB_INDUSTRY_CD", main));
    result.setSUB_INDUSTRY_DESC(getString("SUB_INDUSTRY_DESC", main));
    result.setSYS_LOCTN_CD(getString("ISSUING_COUNTRY_CD", main));
    result.setSYS_LOCTN_DESC(getString("ISSUING_COUNTRY_DESC", main));
    result.setTRANSPORT_ZONE(getString("TRANSPORT_ZONE", main));
    result.setV_TRADESTYLENAME(getString("TRADESTYLE_NM", main));
    result.setVAT(getString("VAT", main));
    result.setGEO_LOC_CD(getString("GEO_LOCATION_CD", main));
    result.setGEO_LOC_DESC(getString("GEO_LOCATION_DESC", main));
    result.setGEO_LOC_DESC(getString("GEO_LOCATION_DESC", main));
    // base coverage
    result.setBASE_COV_ID(getString("BASE_COV_TYPE_ID", main));
    result.setBASE_COV_NM(getString("BASE_COV_NM", main));
    /* 879648 - show business name and lang code in the results */
    result.setBUS_NM(getString("BUS_NM", primary));
    result.setBUS_NM_LANG_CD(getString("BUS_NM_LANG_CD", primary));
    result.setBUS_NM_LANG_NM(getString("BUS_NM_LANG_NM", primary));
    result.setBUS_NM_SADR(getString("BUS_NM_SADR", primary));
    result.setBUS_NM_SADR_LANG_CD(getString("BUS_NM_SADR_LANG_CD", primary));
    result.setBUS_NM_SADR_LANG_NM(getString("BUS_NM_SADR_LANG_NM", primary));

    /* 825446 - add Global Ultimate Client ID / Name */
    result.setGBL_ULT_CLIENT_ID(getString("GBL_ULT_CLIENT_ID", main));
    result.setGBL_ULT_CLIENT_NM(getString("GBL_ULT_CLIENT_NM", main));
    result.setLDE(getString("LDE", main));
    try {
      JSONObject lcr = (JSONObject) main.get("LEAD_CLIENT_REP");
      if (lcr != null) {
        result.setLCR_NAME(lcr.get("FIRST_NAME") + " " + lcr.get("LAST_NAME"));
        result.setLCR_NOTES_ID((String) lcr.get("NOTES_ID"));
      }
    } catch (Exception e) {
    }

    /* 960371 - Add Industry Code and Name */
    result.setINDUSTRY_CD(getString("INDUSTRY_CD", main));
    result.setINDUSTRY_DESC(getString("INDUSTRY_DESC", main));

    /* SaaS changes */
    result.setLIGHT_CMR(getString("LITE_CMR", main));
    String ob = result.getORDERBLOCK();
    result.setPROSPECT_CMR(ob != null && "75".equals(ob.trim()) ? "Y" : "N");
    result.setCOVERAGE_ELIGIBLE(getString("COVERAGE_ELIGIBLE", main));
    result.setSEARCH_SCORE(main.get("searchScore") + "");

    /* SaaS 1031600, 1042676 */
    result.setTAX_CERT_STATUS(getString("TAX_CERT_STATUS", main));

    result.setADDR_SEQ(getString("ADDR_SEQ", main));

    // CREATCMR-1277 CA MASSCREATE
    result.setSELLING_BO_NUM(getString("SELLING_BO_NUM", main));
    result.setINSTL_BO_NUM(getString("INSTL_BO_NUM", main));
    result.setPROV_TAX_EX_IND(getString("PROV_TAX_EX_IND", main));
    result.setENGG_BO_GRP(getString("ENGG_BO_GRP", main));
    result.setACC_RECV_BO(getString("ACC_RECV_BO", main));
    result.setCUST_CREDIT_CD(getString("CUST_CREDIT_CD", main));
    result.setBILL_PLN_TYP(getString("BILL_PLN_TYP", main));
    result.setQST_ID(getString("QST_ID", main));
    result.setSW_EXEMPT_LIC_NUM(getString("SW_EXEMPT_LIC_NUM", main));
    result.setTAX_EXEMPT_REASON(getString("TAX_EXEMPT_REASON", main));
    result.setLEASING_IND(getString("LEASING_IND", main));
    result.setPUR_ORD_NO(getString("PUR_ORD_NO", main));
    result.setLT_PYMNT_IND(getString("LT_PYMNT_IND", main));
    result.setEST_FUNC_CD(getString("EST_FUNC_CD", main));
    result.setNO_OF_INVC(getString("NO_OF_INVC", main));

    return result;
  }

  private static String getString(String key, JSONObject json) throws JSONException, IllegalAccessException, InstantiationException {
    String val = getObject(json, key, String.class);
    return val != null ? val.trim() : "";
  }

  @SuppressWarnings("unchecked")
  private static <T> T getObject(JSONObject json, String key, Class<T> targetClass)
      throws JSONException, IllegalAccessException, InstantiationException {
    if (!json.containsKey(key)) {
      return null;
    }
    try {
      return (T) json.get(key);
    } catch (Exception e) {
      return null;
    }
  }

  private static String concat(String val1, String val2, String concatChar) {
    if (val1 != null && StringUtils.isNotBlank(val1) && val2 != null && StringUtils.isNotBlank(val2)) {
      return (val1 + concatChar + val2);
    } else if (val1 != null && StringUtils.isNotBlank(val1)) {
      return (val1);
    } else {
      return (val2);
    }

  }

}
