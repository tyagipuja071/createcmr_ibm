/**
 * 
 */
package com.ibm.cmr.create.batch.util.masscreate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author Jeffrey Zamora
 * 
 */
@Entity
public class SearchResultObject {

  @Id
  private String KUNNR;

  // query fields
  private String NAME;
  private String DEL_INDC;
  private String ADDRESS_TYPE_DESC;
  private String CMR_OWNER_DESC;
  private String ADDRESS_TXT;
  private String CITY1_NM;
  private String CITY2_NM;
  private String STATE_PROV_CD;
  private String COUNTY_NM;
  private String COV_TYPE_ID;
  private String BASE_COV_ID;
  private String REVENUE_SUM;
  private String REVENUE_PCT;
  private String LANDING_COUNTRY_CD;
  private String ABBRV_US_NM;
  private String ABBRV_EUR_NM;
  private String CUST_CLASS_CD;
  private String CUST_CLASS_DESC;
  private String VAT;
  private String ENTERPRISE_NO;
  private String SIC_CD;
  private String AFFILIATE_NO;
  private String ISU;
  private String ISU_DESC;
  private String SYS_LOCTN_CD;
  private String SYS_LOCTN_DESC;
  private String CMR_NO;
  private String CUST1_NM;
  private String CUST2_NM;
  private String CUST3_NM;
  private String CUST4_NM;
  private String LAND_CNTRY_NM;
  private String LAND_CNTRY_DESC;
  private String ADDRESS_TYPE;
  private String STATE_PROV_NM;
  private String POSTAL_CD;
  private String COUNTY_CD;
  private String COMPANY_NO;
  private String BUSINESS_REG;
  private String INAC_TYPE_CD;
  private String INAC;
  private String INAC_DESC;
  private String ORDERBLOCK;
  private String CLIENT_TIER_CD;
  private String CLIENT_TIER_DESC;
  private String DEL_COV_INDC;
  private String IBM_CLIENT_ID;
  private String DOM_CLIENT_ID;
  private String AVG_4Y_REVENUE;
  private String SITE_ID;
  private String BUY_GRP_ID;
  private String BUY_GRP_NM;
  private String GBL_BUY_GRP_ID;
  private String GBL_BUY_GRP_NM;
  private String DUNSNO;
  private String PARENT_DUNS_NO;
  private String DOM_ULT_DUNS_NO;
  private String GBL_ULT_DUNS_NO;
  // field for LDE
  private String LDE;

  @Temporal(TemporalType.TIMESTAMP)
  private String SHAD_UPDATE_TS;

  @Temporal(TemporalType.TIMESTAMP)
  private String SAP_TS;

  private String ORDERBLOCKDESC;
  private String ALTERNATE_LANG_NAME1;
  private String ALTERNATE_LANG_ADDRESS;
  private String ALTERNATE_LANG_CITY1;
  private String ALTERNATE_LANG_CITY2;
  private String ALTERNATE_LANG_NAME2;
  private String ALTERNATE_LANG_NAME3;
  private String ALTERNATE_LANG_NAME4;
  private String DOM_CLIENT_NAME;
  private String GBL_CLIENT_ID;
  private String GBL_CLIENT_NAME;
  private String SORTL;
  private String SUB_INDUSTRY;
  private String SIC_DESC;
  private String SUB_INDUSTRY_DESC;
  private String V_TRADESTYLENAME;
  private String COV_NAME;
  private String BASE_COV_NM;
  private String CAP_IND;
  private String RDC_CREATE_DATE;
  private String CUST_PHONE;
  private String CUST_FAX;
  private String PREF_LANG;
  private String LOCAL_TAX2;
  private String SENSITIVE_FLAG;
  private String TRANSPORT_ZONE;
  private String PO_BOX;
  private String PO_BOX_CITY;
  private String PO_BOX_POST_CD;
  private String PPSCEID;
  private String BUILDING;
  private String FLOOR;
  private String OFFICE;
  private String DEPARTMENT;
  private String MEMB_LEVEL;
  private String BP_REL_TYPE;
  private String GOE_IND;
  private String LDE_IND;
  private String COV_CLIENT_TYPE;
  private String COV_CLIENT_SUBTYPE;
  private String COV_CLIENT_TYPE_DESC;
  private String COV_CLIENT_SUBTYPE_DESC;

  /* added for LH requirement, 797865 */
  private String CLIENT_TYPE;

  /* 814271 */
  private String GEO_LOC_CD;
  private String GEO_LOC_DESC;

  /* 825444 - add SADR Lang Code (SPRAS) */
  private String ALTERNATE_LANG_CD;
  private String ALTERNATE_SUB_LANG_CD;
  private String ALTERNATE_SUB_LANG_DESC;
  private String OTH_ALT_LANG_BUS_NAME;
  private String OTH_ALT_LANG_CITY1;
  private String OTH_ALT_LANG_CITY2;
  private String OTH_ALT_LANG_ADDRESS;
  private String OTH_ALT_LANG_CD;
  private String OTH_ALT_SUB_LANG_CD;
  private String OTH_ALT_SUB_LANG_DESC;

  /* 825446 - add Global Ultimate Client ID / Name */
  private String GBL_ULT_CLIENT_ID;
  private String GBL_ULT_CLIENT_NM;

  private String LCR_NAME;
  private String LCR_NOTES_ID;

  /* 879648 - show business name and lang code in the results */
  private String BUS_NM;
  private String BUS_NM_SADR;
  private String BUS_NM_LANG_CD;
  private String BUS_NM_LANG_NM;
  private String BUS_NM_SADR_LANG_CD;
  private String BUS_NM_SADR_LANG_NM;

  private String INDUSTRY_CD;
  private String INDUSTRY_DESC;

  /* SaaS changes */
  private String LIGHT_CMR;
  private String PROSPECT_CMR;
  private String COVERAGE_ELIGIBLE;
  private String SEARCH_SCORE;

  /* SaaS 1031600, 1042676 */
  private String TAX_CERT_STATUS;

  private String ADDR_SEQ;

  public String getKUNNR() {
    return KUNNR;
  }

  public void setKUNNR(String kUNNR) {
    KUNNR = kUNNR;
  }

  public String getNAME() {
    return NAME;
  }

  public void setNAME(String nAME) {
    NAME = nAME;
  }

  public String getDEL_INDC() {
    return DEL_INDC;
  }

  public void setDEL_INDC(String dEL_INDC) {
    DEL_INDC = dEL_INDC;
  }

  public String getADDRESS_TYPE_DESC() {
    return ADDRESS_TYPE_DESC;
  }

  public void setADDRESS_TYPE_DESC(String aDDRESS_TYPE_DESC) {
    ADDRESS_TYPE_DESC = aDDRESS_TYPE_DESC;
  }

  public String getCMR_OWNER_DESC() {
    return CMR_OWNER_DESC;
  }

  public void setCMR_OWNER_DESC(String cMR_OWNER_DESC) {
    CMR_OWNER_DESC = cMR_OWNER_DESC;
  }

  public String getADDRESS_TXT() {
    return ADDRESS_TXT;
  }

  public void setADDRESS_TXT(String aDDRESS_TXT) {
    ADDRESS_TXT = aDDRESS_TXT;
  }

  public String getCITY1_NM() {
    return CITY1_NM;
  }

  public void setCITY1_NM(String cITY1_NM) {
    CITY1_NM = cITY1_NM;
  }

  public String getCITY2_NM() {
    return CITY2_NM;
  }

  public void setCITY2_NM(String cITY2_NM) {
    CITY2_NM = cITY2_NM;
  }

  public String getSTATE_PROV_CD() {
    return STATE_PROV_CD;
  }

  public void setSTATE_PROV_CD(String sTATE_PROV_CD) {
    STATE_PROV_CD = sTATE_PROV_CD;
  }

  public String getCOUNTY_NM() {
    return COUNTY_NM;
  }

  public void setCOUNTY_NM(String cOUNTY_NM) {
    COUNTY_NM = cOUNTY_NM;
  }

  public String getCOV_TYPE_ID() {
    return COV_TYPE_ID;
  }

  public void setCOV_TYPE_ID(String cOV_TYPE_ID) {
    COV_TYPE_ID = cOV_TYPE_ID;
  }

  public String getREVENUE_SUM() {
    return REVENUE_SUM;
  }

  public void setREVENUE_SUM(String rEVENUE_SUM) {
    REVENUE_SUM = rEVENUE_SUM;
  }

  public String getREVENUE_PCT() {
    return REVENUE_PCT;
  }

  public void setREVENUE_PCT(String rEVENUE_PCT) {
    REVENUE_PCT = rEVENUE_PCT;
  }

  public String getLANDING_COUNTRY_CD() {
    return LANDING_COUNTRY_CD;
  }

  public void setLANDING_COUNTRY_CD(String lANDING_COUNTRY_CD) {
    LANDING_COUNTRY_CD = lANDING_COUNTRY_CD;
  }

  public String getABBRV_US_NM() {
    return ABBRV_US_NM;
  }

  public void setABBRV_US_NM(String aBBRV_US_NM) {
    ABBRV_US_NM = aBBRV_US_NM;
  }

  public String getABBRV_EUR_NM() {
    return ABBRV_EUR_NM;
  }

  public void setABBRV_EUR_NM(String aBBRV_EUR_NM) {
    ABBRV_EUR_NM = aBBRV_EUR_NM;
  }

  public String getCUST_CLASS_CD() {
    return CUST_CLASS_CD;
  }

  public void setCUST_CLASS_CD(String cUST_CLASS_CD) {
    CUST_CLASS_CD = cUST_CLASS_CD;
  }

  public String getCUST_CLASS_DESC() {
    return CUST_CLASS_DESC;
  }

  public void setCUST_CLASS_DESC(String cUST_CLASS_DESC) {
    CUST_CLASS_DESC = cUST_CLASS_DESC;
  }

  public String getVAT() {
    return VAT;
  }

  public void setVAT(String vAT) {
    VAT = vAT;
  }

  public String getENTERPRISE_NO() {
    return ENTERPRISE_NO;
  }

  public void setENTERPRISE_NO(String eNTERPRISE_NO) {
    ENTERPRISE_NO = eNTERPRISE_NO;
  }

  public String getSIC_CD() {
    return SIC_CD;
  }

  public void setSIC_CD(String sIC_CD) {
    SIC_CD = sIC_CD;
  }

  public String getAFFILIATE_NO() {
    return AFFILIATE_NO;
  }

  public void setAFFILIATE_NO(String aFFILIATE_NO) {
    AFFILIATE_NO = aFFILIATE_NO;
  }

  public String getISU() {
    return ISU;
  }

  public void setISU(String iSU) {
    ISU = iSU;
  }

  public String getISU_DESC() {
    return ISU_DESC;
  }

  public void setISU_DESC(String iSU_DESC) {
    ISU_DESC = iSU_DESC;
  }

  public String getSYS_LOCTN_CD() {
    return SYS_LOCTN_CD;
  }

  public void setSYS_LOCTN_CD(String sYS_LOCTN_CD) {
    SYS_LOCTN_CD = sYS_LOCTN_CD;
  }

  public String getSYS_LOCTN_DESC() {
    return SYS_LOCTN_DESC;
  }

  public void setSYS_LOCTN_DESC(String sYS_LOCTN_DESC) {
    SYS_LOCTN_DESC = sYS_LOCTN_DESC;
  }

  public String getCMR_NO() {
    return CMR_NO;
  }

  public void setCMR_NO(String cMR_NO) {
    CMR_NO = cMR_NO;
  }

  public String getCUST1_NM() {
    return CUST1_NM;
  }

  public void setCUST1_NM(String cUST1_NM) {
    CUST1_NM = cUST1_NM;
  }

  public String getCUST2_NM() {
    return CUST2_NM;
  }

  public void setCUST2_NM(String cUST2_NM) {
    CUST2_NM = cUST2_NM;
  }

  public String getCUST3_NM() {
    return CUST3_NM;
  }

  public void setCUST3_NM(String cUST3_NM) {
    CUST3_NM = cUST3_NM;
  }

  public String getCUST4_NM() {
    return CUST4_NM;
  }

  public void setCUST4_NM(String cUST4_NM) {
    CUST4_NM = cUST4_NM;
  }

  public String getLAND_CNTRY_NM() {
    return LAND_CNTRY_NM;
  }

  public void setLAND_CNTRY_NM(String lAND_CNTRY_NM) {
    LAND_CNTRY_NM = lAND_CNTRY_NM;
  }

  public String getLAND_CNTRY_DESC() {
    return LAND_CNTRY_DESC;
  }

  public void setLAND_CNTRY_DESC(String lAND_CNTRY_DESC) {
    LAND_CNTRY_DESC = lAND_CNTRY_DESC;
  }

  public String getADDRESS_TYPE() {
    return ADDRESS_TYPE;
  }

  public void setADDRESS_TYPE(String aDDRESS_TYPE) {
    ADDRESS_TYPE = aDDRESS_TYPE;
  }

  public String getSTATE_PROV_NM() {
    return STATE_PROV_NM;
  }

  public void setSTATE_PROV_NM(String sTATE_PROV_NM) {
    STATE_PROV_NM = sTATE_PROV_NM;
  }

  public String getPOSTAL_CD() {
    return POSTAL_CD;
  }

  public void setPOSTAL_CD(String pOSTAL_CD) {
    POSTAL_CD = pOSTAL_CD;
  }

  public String getCOUNTY_CD() {
    return COUNTY_CD;
  }

  public void setCOUNTY_CD(String cOUNTY_CD) {
    COUNTY_CD = cOUNTY_CD;
  }

  public String getCOMPANY_NO() {
    return COMPANY_NO;
  }

  public void setCOMPANY_NO(String cOMPANY_NO) {
    COMPANY_NO = cOMPANY_NO;
  }

  public String getBUSINESS_REG() {
    return BUSINESS_REG;
  }

  public void setBUSINESS_REG(String bUSINESS_REG) {
    BUSINESS_REG = bUSINESS_REG;
  }

  public String getINAC_TYPE_CD() {
    return INAC_TYPE_CD;
  }

  public void setINAC_TYPE_CD(String iNAC_TYPE_CD) {
    INAC_TYPE_CD = iNAC_TYPE_CD;
  }

  public String getINAC() {
    return INAC;
  }

  public void setINAC(String iNAC) {
    INAC = iNAC;
  }

  public String getINAC_DESC() {
    return INAC_DESC;
  }

  public void setINAC_DESC(String iNAC_DESC) {
    INAC_DESC = iNAC_DESC;
  }

  public String getORDERBLOCK() {
    return ORDERBLOCK;
  }

  public void setORDERBLOCK(String oRDERBLOCK) {
    ORDERBLOCK = oRDERBLOCK;
  }

  public String getCLIENT_TIER_CD() {
    return CLIENT_TIER_CD;
  }

  public void setCLIENT_TIER_CD(String cLIENT_TIER_CD) {
    CLIENT_TIER_CD = cLIENT_TIER_CD;
  }

  public String getCLIENT_TIER_DESC() {
    return CLIENT_TIER_DESC;
  }

  public void setCLIENT_TIER_DESC(String cLIENT_TIER_DESC) {
    CLIENT_TIER_DESC = cLIENT_TIER_DESC;
  }

  public String getDEL_COV_INDC() {
    return DEL_COV_INDC;
  }

  public void setDEL_COV_INDC(String dEL_COV_INDC) {
    DEL_COV_INDC = dEL_COV_INDC;
  }

  public String getIBM_CLIENT_ID() {
    return IBM_CLIENT_ID;
  }

  public void setIBM_CLIENT_ID(String iBM_CLIENT_ID) {
    IBM_CLIENT_ID = iBM_CLIENT_ID;
  }

  public String getDOM_CLIENT_ID() {
    return DOM_CLIENT_ID;
  }

  public void setDOM_CLIENT_ID(String dOM_CLIENT_ID) {
    DOM_CLIENT_ID = dOM_CLIENT_ID;
  }

  public String getAVG_4Y_REVENUE() {
    return AVG_4Y_REVENUE;
  }

  public void setAVG_4Y_REVENUE(String aVG_4Y_REVENUE) {
    AVG_4Y_REVENUE = aVG_4Y_REVENUE;
  }

  public String getSITE_ID() {
    return SITE_ID;
  }

  public void setSITE_ID(String sITE_ID) {
    SITE_ID = sITE_ID;
  }

  public String getBUY_GRP_ID() {
    return BUY_GRP_ID;
  }

  public void setBUY_GRP_ID(String bUY_GRP_ID) {
    BUY_GRP_ID = bUY_GRP_ID;
  }

  public String getBUY_GRP_NM() {
    return BUY_GRP_NM;
  }

  public void setBUY_GRP_NM(String bUY_GRP_NM) {
    BUY_GRP_NM = bUY_GRP_NM;
  }

  public String getGBL_BUY_GRP_ID() {
    return GBL_BUY_GRP_ID;
  }

  public void setGBL_BUY_GRP_ID(String gBL_BUY_GRP_ID) {
    GBL_BUY_GRP_ID = gBL_BUY_GRP_ID;
  }

  public String getGBL_BUY_GRP_NM() {
    return GBL_BUY_GRP_NM;
  }

  public void setGBL_BUY_GRP_NM(String gBL_BUY_GRP_NM) {
    GBL_BUY_GRP_NM = gBL_BUY_GRP_NM;
  }

  public String getDUNSNO() {
    return DUNSNO;
  }

  public void setDUNSNO(String dUNSNO) {
    DUNSNO = dUNSNO;
  }

  public String getPARENT_DUNS_NO() {
    return PARENT_DUNS_NO;
  }

  public void setPARENT_DUNS_NO(String pARENT_DUNS_NO) {
    PARENT_DUNS_NO = pARENT_DUNS_NO;
  }

  public String getDOM_ULT_DUNS_NO() {
    return DOM_ULT_DUNS_NO;
  }

  public void setDOM_ULT_DUNS_NO(String dOM_ULT_DUNS_NO) {
    DOM_ULT_DUNS_NO = dOM_ULT_DUNS_NO;
  }

  public String getGBL_ULT_DUNS_NO() {
    return GBL_ULT_DUNS_NO;
  }

  public void setGBL_ULT_DUNS_NO(String gBL_ULT_DUNS_NO) {
    GBL_ULT_DUNS_NO = gBL_ULT_DUNS_NO;
  }

  public String getSHAD_UPDATE_TS() {
    return SHAD_UPDATE_TS;
  }

  public void setSHAD_UPDATE_TS(String sHAD_UPDATE_TS) {
    SHAD_UPDATE_TS = sHAD_UPDATE_TS;
  }

  public String getSAP_TS() {
    return SAP_TS;
  }

  public void setSAP_TS(String sAP_TS) {
    SAP_TS = sAP_TS;
  }

  public String getORDERBLOCKDESC() {
    return ORDERBLOCKDESC;
  }

  public void setORDERBLOCKDESC(String oRDERBLOCKDESC) {
    ORDERBLOCKDESC = oRDERBLOCKDESC;
  }

  public String getALTERNATE_LANG_NAME1() {
    return ALTERNATE_LANG_NAME1;
  }

  public void setALTERNATE_LANG_NAME1(String aLTERNATE_LANG_NAME1) {
    ALTERNATE_LANG_NAME1 = aLTERNATE_LANG_NAME1;
  }

  public String getALTERNATE_LANG_ADDRESS() {
    return ALTERNATE_LANG_ADDRESS;
  }

  public void setALTERNATE_LANG_ADDRESS(String aLTERNATE_LANG_ADDRESS) {
    ALTERNATE_LANG_ADDRESS = aLTERNATE_LANG_ADDRESS;
  }

  public String getALTERNATE_LANG_CITY1() {
    return ALTERNATE_LANG_CITY1;
  }

  public void setALTERNATE_LANG_CITY1(String aLTERNATE_LANG_CITY1) {
    ALTERNATE_LANG_CITY1 = aLTERNATE_LANG_CITY1;
  }

  public String getALTERNATE_LANG_CITY2() {
    return ALTERNATE_LANG_CITY2;
  }

  public void setALTERNATE_LANG_CITY2(String aLTERNATE_LANG_CITY2) {
    ALTERNATE_LANG_CITY2 = aLTERNATE_LANG_CITY2;
  }

  public String getALTERNATE_LANG_NAME2() {
    return ALTERNATE_LANG_NAME2;
  }

  public void setALTERNATE_LANG_NAME2(String aLTERNATE_LANG_NAME2) {
    ALTERNATE_LANG_NAME2 = aLTERNATE_LANG_NAME2;
  }

  public String getALTERNATE_LANG_NAME4() {
    return ALTERNATE_LANG_NAME4;
  }

  public void setALTERNATE_LANG_NAME4(String aLTERNATE_LANG_NAME4) {
    ALTERNATE_LANG_NAME4 = aLTERNATE_LANG_NAME4;
  }

  public String getDOM_CLIENT_NAME() {
    return DOM_CLIENT_NAME;
  }

  public void setDOM_CLIENT_NAME(String dOM_CLIENT_NAME) {
    DOM_CLIENT_NAME = dOM_CLIENT_NAME;
  }

  public String getGBL_CLIENT_ID() {
    return GBL_CLIENT_ID;
  }

  public void setGBL_CLIENT_ID(String gBL_CLIENT_ID) {
    GBL_CLIENT_ID = gBL_CLIENT_ID;
  }

  public String getGBL_CLIENT_NAME() {
    return GBL_CLIENT_NAME;
  }

  public void setGBL_CLIENT_NAME(String gBL_CLIENT_NAME) {
    GBL_CLIENT_NAME = gBL_CLIENT_NAME;
  }

  public String getSORTL() {
    return SORTL;
  }

  public void setSORTL(String sORTL) {
    SORTL = sORTL;
  }

  public String getSUB_INDUSTRY() {
    return SUB_INDUSTRY;
  }

  public void setSUB_INDUSTRY(String sUB_INDUSTRY) {
    SUB_INDUSTRY = sUB_INDUSTRY;
  }

  public String getSIC_DESC() {
    return SIC_DESC;
  }

  public void setSIC_DESC(String sIC_DESC) {
    SIC_DESC = sIC_DESC;
  }

  public String getSUB_INDUSTRY_DESC() {
    return SUB_INDUSTRY_DESC;
  }

  public void setSUB_INDUSTRY_DESC(String sUB_INDUSTRY_DESC) {
    SUB_INDUSTRY_DESC = sUB_INDUSTRY_DESC;
  }

  public String getV_TRADESTYLENAME() {
    return V_TRADESTYLENAME;
  }

  public void setV_TRADESTYLENAME(String v_TRADESTYLENAME) {
    V_TRADESTYLENAME = v_TRADESTYLENAME;
  }

  public String getCOV_NAME() {
    return COV_NAME;
  }

  public String getLCR_NAME() {
    return LCR_NAME;
  }

  public void setLCR_NAME(String lCR_NAME) {
    LCR_NAME = lCR_NAME;
  }

  public String getLCR_NOTES_ID() {
    return LCR_NOTES_ID;
  }

  public void setLCR_NOTES_ID(String lCR_NOTES_ID) {
    LCR_NOTES_ID = lCR_NOTES_ID;
  }

  public void setCOV_NAME(String cOV_NAME) {
    COV_NAME = cOV_NAME;
  }

  public String getCAP_IND() {
    return CAP_IND;
  }

  public void setCAP_IND(String cAP_IND) {
    CAP_IND = cAP_IND;
  }

  public String getRDC_CREATE_DATE() {
    return RDC_CREATE_DATE;
  }

  public void setRDC_CREATE_DATE(String rDC_CREATE_DATE) {
    RDC_CREATE_DATE = rDC_CREATE_DATE;
  }

  public String getCUST_PHONE() {
    return CUST_PHONE;
  }

  public void setCUST_PHONE(String cUST_PHONE) {
    CUST_PHONE = cUST_PHONE;
  }

  public String getCUST_FAX() {
    return CUST_FAX;
  }

  public void setCUST_FAX(String cUST_FAX) {
    CUST_FAX = cUST_FAX;
  }

  public String getPREF_LANG() {
    return PREF_LANG;
  }

  public void setPREF_LANG(String pREF_LANG) {
    PREF_LANG = pREF_LANG;
  }

  public String getLOCAL_TAX2() {
    return LOCAL_TAX2;
  }

  public void setLOCAL_TAX2(String lOCAL_TAX2) {
    LOCAL_TAX2 = lOCAL_TAX2;
  }

  public String getSENSITIVE_FLAG() {
    return SENSITIVE_FLAG;
  }

  public void setSENSITIVE_FLAG(String sENSITIVE_FLAG) {
    SENSITIVE_FLAG = sENSITIVE_FLAG;
  }

  public String getTRANSPORT_ZONE() {
    return TRANSPORT_ZONE;
  }

  public void setTRANSPORT_ZONE(String tRANSPORT_ZONE) {
    TRANSPORT_ZONE = tRANSPORT_ZONE;
  }

  public String getPO_BOX() {
    return PO_BOX;
  }

  public void setPO_BOX(String pO_BOX) {
    PO_BOX = pO_BOX;
  }

  public String getPO_BOX_CITY() {
    return PO_BOX_CITY;
  }

  public void setPO_BOX_CITY(String pO_BOX_CITY) {
    PO_BOX_CITY = pO_BOX_CITY;
  }

  public String getPO_BOX_POST_CD() {
    return PO_BOX_POST_CD;
  }

  public void setPO_BOX_POST_CD(String pO_BOX_POST_CD) {
    PO_BOX_POST_CD = pO_BOX_POST_CD;
  }

  public String getPPSCEID() {
    return PPSCEID;
  }

  public void setPPSCEID(String pPSCEID) {
    PPSCEID = pPSCEID;
  }

  public String getBUILDING() {
    return BUILDING;
  }

  public void setBUILDING(String bUILDING) {
    BUILDING = bUILDING;
  }

  public String getFLOOR() {
    return FLOOR;
  }

  public void setFLOOR(String fLOOR) {
    FLOOR = fLOOR;
  }

  public String getOFFICE() {
    return OFFICE;
  }

  public void setOFFICE(String oFFICE) {
    OFFICE = oFFICE;
  }

  public String getDEPARTMENT() {
    return DEPARTMENT;
  }

  public void setDEPARTMENT(String dEPARTMENT) {
    DEPARTMENT = dEPARTMENT;
  }

  public String getMEMB_LEVEL() {
    return MEMB_LEVEL;
  }

  public void setMEMB_LEVEL(String mEMB_LEVEL) {
    MEMB_LEVEL = mEMB_LEVEL;
  }

  public String getBP_REL_TYPE() {
    return BP_REL_TYPE;
  }

  public void setBP_REL_TYPE(String bP_REL_TYPE) {
    BP_REL_TYPE = bP_REL_TYPE;
  }

  public String getGOE_IND() {
    return GOE_IND;
  }

  public void setGOE_IND(String gOE_IND) {
    GOE_IND = gOE_IND;
  }

  public String getCOV_CLIENT_TYPE() {
    return COV_CLIENT_TYPE;
  }

  public void setCOV_CLIENT_TYPE(String cOV_CLIENT_TYPE) {
    COV_CLIENT_TYPE = cOV_CLIENT_TYPE;
  }

  public String getCOV_CLIENT_SUBTYPE() {
    return COV_CLIENT_SUBTYPE;
  }

  public void setCOV_CLIENT_SUBTYPE(String cOV_CLIENT_SUBTYPE) {
    COV_CLIENT_SUBTYPE = cOV_CLIENT_SUBTYPE;
  }

  public String getCOV_CLIENT_TYPE_DESC() {
    return COV_CLIENT_TYPE_DESC;
  }

  public void setCOV_CLIENT_TYPE_DESC(String cOV_CLIENT_TYPE_DESC) {
    COV_CLIENT_TYPE_DESC = cOV_CLIENT_TYPE_DESC;
  }

  public String getCOV_CLIENT_SUBTYPE_DESC() {
    return COV_CLIENT_SUBTYPE_DESC;
  }

  public void setCOV_CLIENT_SUBTYPE_DESC(String cOV_CLIENT_SUBTYPE_DESC) {
    COV_CLIENT_SUBTYPE_DESC = cOV_CLIENT_SUBTYPE_DESC;
  }

  public void setALTERNATE_LANG_NAME3(String aLTERNATE_LANG_NAME3) {
    ALTERNATE_LANG_NAME3 = aLTERNATE_LANG_NAME3;
  }

  public String getALTERNATE_LANG_NAME3() {
    return ALTERNATE_LANG_NAME3;
  }

  public String getLDE_IND() {
    return LDE_IND;
  }

  public void setLDE_IND(String lDE_IND) {
    LDE_IND = lDE_IND;
  }

  public String getCLIENT_TYPE() {
    return CLIENT_TYPE;
  }

  public void setCLIENT_TYPE(String cLIENT_TYPE) {
    CLIENT_TYPE = cLIENT_TYPE;
  }

  public String getGEO_LOC_CD() {
    return GEO_LOC_CD;
  }

  public void setGEO_LOC_CD(String gEO_LOC_CD) {
    GEO_LOC_CD = gEO_LOC_CD;
  }

  public String getGEO_LOC_DESC() {
    return GEO_LOC_DESC;
  }

  public void setGEO_LOC_DESC(String gEO_LOC_DESC) {
    GEO_LOC_DESC = gEO_LOC_DESC;
  }

  public String getALTERNATE_LANG_CD() {
    return ALTERNATE_LANG_CD;
  }

  public void setALTERNATE_LANG_CD(String aLTERNATE_LANG_CD) {
    ALTERNATE_LANG_CD = aLTERNATE_LANG_CD;
  }

  public String getGBL_ULT_CLIENT_ID() {
    return GBL_ULT_CLIENT_ID;
  }

  public void setGBL_ULT_CLIENT_ID(String gBL_ULT_CLIENT_ID) {
    GBL_ULT_CLIENT_ID = gBL_ULT_CLIENT_ID;
  }

  public String getGBL_ULT_CLIENT_NM() {
    return GBL_ULT_CLIENT_NM;
  }

  public void setGBL_ULT_CLIENT_NM(String gBL_ULT_CLIENT_NM) {
    GBL_ULT_CLIENT_NM = gBL_ULT_CLIENT_NM;
  }

  public String getALTERNATE_SUB_LANG_CD() {
    return ALTERNATE_SUB_LANG_CD;
  }

  public void setALTERNATE_SUB_LANG_CD(String aLTERNATE_SUB_LANG_CD) {
    ALTERNATE_SUB_LANG_CD = aLTERNATE_SUB_LANG_CD;
  }

  public String getOTH_ALT_LANG_CITY1() {
    return OTH_ALT_LANG_CITY1;
  }

  public void setOTH_ALT_LANG_CITY1(String oTH_ALT_LANG_CITY1) {
    OTH_ALT_LANG_CITY1 = oTH_ALT_LANG_CITY1;
  }

  public String getOTH_ALT_LANG_CITY2() {
    return OTH_ALT_LANG_CITY2;
  }

  public void setOTH_ALT_LANG_CITY2(String oTH_ALT_LANG_CITY2) {
    OTH_ALT_LANG_CITY2 = oTH_ALT_LANG_CITY2;
  }

  public String getOTH_ALT_LANG_ADDRESS() {
    return OTH_ALT_LANG_ADDRESS;
  }

  public void setOTH_ALT_LANG_ADDRESS(String oTH_ALT_LANG_ADDRESS) {
    OTH_ALT_LANG_ADDRESS = oTH_ALT_LANG_ADDRESS;
  }

  public String getOTH_ALT_LANG_CD() {
    return OTH_ALT_LANG_CD;
  }

  public void setOTH_ALT_LANG_CD(String oTH_ALT_LANG_CD) {
    OTH_ALT_LANG_CD = oTH_ALT_LANG_CD;
  }

  public String getOTH_ALT_SUB_LANG_CD() {
    return OTH_ALT_SUB_LANG_CD;
  }

  public void setOTH_ALT_SUB_LANG_CD(String oTH_ALT_SUB_LANG_CD) {
    OTH_ALT_SUB_LANG_CD = oTH_ALT_SUB_LANG_CD;
  }

  public String getOTH_ALT_LANG_BUS_NAME() {
    return OTH_ALT_LANG_BUS_NAME;
  }

  public void setOTH_ALT_LANG_BUS_NAME(String oTH_ALT_LANG_BUS_NAME) {
    OTH_ALT_LANG_BUS_NAME = oTH_ALT_LANG_BUS_NAME;
  }

  public String getALTERNATE_SUB_LANG_DESC() {
    return ALTERNATE_SUB_LANG_DESC;
  }

  public void setALTERNATE_SUB_LANG_DESC(String aLTERNATE_SUB_LANG_DESC) {
    ALTERNATE_SUB_LANG_DESC = aLTERNATE_SUB_LANG_DESC;
  }

  public String getOTH_ALT_SUB_LANG_DESC() {
    return OTH_ALT_SUB_LANG_DESC;
  }

  public void setOTH_ALT_SUB_LANG_DESC(String oTH_ALT_SUB_LANG_DESC) {
    OTH_ALT_SUB_LANG_DESC = oTH_ALT_SUB_LANG_DESC;
  }

  public String getLDE() {
    return LDE;
  }

  public void setLDE(String lDE) {
    LDE = lDE;
  }

  public String getBUS_NM_LANG_CD() {
    return BUS_NM_LANG_CD;
  }

  public void setBUS_NM_LANG_CD(String bUS_NM_LANG_CD) {
    BUS_NM_LANG_CD = bUS_NM_LANG_CD;
  }

  public String getBUS_NM_LANG_NM() {
    return BUS_NM_LANG_NM;
  }

  public void setBUS_NM_LANG_NM(String bUS_NM_LANG_NM) {
    BUS_NM_LANG_NM = bUS_NM_LANG_NM;
  }

  public String getBUS_NM_SADR_LANG_CD() {
    return BUS_NM_SADR_LANG_CD;
  }

  public void setBUS_NM_SADR_LANG_CD(String bUS_NM_SADR_LANG_CD) {
    BUS_NM_SADR_LANG_CD = bUS_NM_SADR_LANG_CD;
  }

  public String getBUS_NM_SADR_LANG_NM() {
    return BUS_NM_SADR_LANG_NM;
  }

  public void setBUS_NM_SADR_LANG_NM(String bUS_NM_SADR_LANG_NM) {
    BUS_NM_SADR_LANG_NM = bUS_NM_SADR_LANG_NM;
  }

  public String getBUS_NM() {
    return BUS_NM;
  }

  public void setBUS_NM(String bUS_NM) {
    BUS_NM = bUS_NM;
  }

  public String getBUS_NM_SADR() {
    return BUS_NM_SADR;
  }

  public void setBUS_NM_SADR(String bUS_NM_SADR) {
    BUS_NM_SADR = bUS_NM_SADR;
  }

  public String getBASE_COV_ID() {
    return BASE_COV_ID;
  }

  public void setBASE_COV_ID(String bASE_COV_ID) {
    BASE_COV_ID = bASE_COV_ID;
  }

  public String getBASE_COV_NM() {
    return BASE_COV_NM;
  }

  public void setBASE_COV_NM(String bASE_COV_NM) {
    BASE_COV_NM = bASE_COV_NM;
  }

  public String getINDUSTRY_CD() {
    return INDUSTRY_CD;
  }

  public void setINDUSTRY_CD(String iNDUSTRY_CD) {
    INDUSTRY_CD = iNDUSTRY_CD;
  }

  public String getINDUSTRY_DESC() {
    return INDUSTRY_DESC;
  }

  public void setINDUSTRY_DESC(String iNDUSTRY_DESC) {
    INDUSTRY_DESC = iNDUSTRY_DESC;
  }

  public String getLIGHT_CMR() {
    return LIGHT_CMR;
  }

  public void setLIGHT_CMR(String lIGHT_CMR) {
    LIGHT_CMR = lIGHT_CMR;
  }

  public String getCOVERAGE_ELIGIBLE() {
    return COVERAGE_ELIGIBLE;
  }

  public void setCOVERAGE_ELIGIBLE(String cOVERAGE_ELIGIBLE) {
    COVERAGE_ELIGIBLE = cOVERAGE_ELIGIBLE;
  }

  public String getSEARCH_SCORE() {
    return SEARCH_SCORE;
  }

  public void setSEARCH_SCORE(String sEARCH_SCORE) {
    SEARCH_SCORE = sEARCH_SCORE;
  }

  public String getPROSPECT_CMR() {
    return PROSPECT_CMR;
  }

  public void setPROSPECT_CMR(String pROSPECT_CMR) {
    PROSPECT_CMR = pROSPECT_CMR;
  }

  public String getTAX_CERT_STATUS() {
    return TAX_CERT_STATUS;
  }

  public void setTAX_CERT_STATUS(String tAX_CERT_STATUS) {
    TAX_CERT_STATUS = tAX_CERT_STATUS;
  }

  public String getADDR_SEQ() {
    return ADDR_SEQ;
  }

  public void setADDR_SEQ(String aDDR_SEQ) {
    ADDR_SEQ = aDDR_SEQ;
  }
}
