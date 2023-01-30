/**
 * 
 */
package com.ibm.cio.cmr.request.model;

/**
 * Represents a company record from the CompanyFinder utility
 * 
 * @author JeffZAMORA
 *
 */
public class CompanyRecordModel implements Comparable<CompanyRecordModel> {

  public static final String REC_TYPE_CMR = "CMR";
  public static final String REC_TYPE_DNB = "DNB";
  public static final String REC_TYPE_REQUEST = "REQ";
  public static final String GOE_STATUS_UNKNOWN = "U";
  public static final String GOE_STATUS_NO = "N";
  public static final String GOE_STATUS_YES = "Y";
  public static final String GOE_STATUS_STATE_OWNED = "S";

  private String recType;
  private String name;
  private String countryCd;
  private String stateProv;
  private String streetAddress1;
  private String streetAddress2;
  private String city;
  private String postCd;
  private String vat;
  private String vatInd;
  private String taxCd1;
  private String matchGrade;

  private String cmrNo;
  private String issuingCntry;
  private String dunsNo;
  private String goeStatus;
  private String subRegion;

  private boolean hasCmr;
  private boolean hasDnb;
  private boolean orgIdMatch;
  private String reqType;

  private String altName;
  private String altStreet;
  private String altCity;
  private String restrictTo;

  private Double revenue;

  private boolean highestRevenue;
  private String operStatusCode;
  private boolean isPoolRecord;
  private String addDnBMatches;

  private String cied;

  private long overrideReqId;

  @Override
  public int compareTo(CompanyRecordModel o) {
    if (o == null) {
      return -1;
    }

    // move prospects up
    if ("CMR".equals(this.recType) && this.cmrNo.startsWith("P")
        && (!"CMR".equals(o.recType) || ("CMR".equals(o.recType) && !o.cmrNo.startsWith("P")))) {
      return -1;
    }
    if ("CMR".equals(o.recType) && o.cmrNo.startsWith("P")
        && (!"CMR".equals(this.recType) || ("CMR".equals(this.recType) && !this.cmrNo.startsWith("P")))) {
      return 1;
    }
    // move CMR types down CREATCMR-7388
    if ("CMR".equals(this.recType) && !"CMR".equals(o.getRecType())) {
      return 1;
    }
    if (!"CMR".equals(this.recType) && "CMR".equals(o.getRecType())) {
      return -1;
    }

    if ("REQ".equals(this.recType) && !"REQ".equals(o.getRecType())) {
      return -1;
    }
    if (!"REQ".equals(this.recType) && "REQ".equals(o.getRecType())) {
      return 1;
    }

    // use match grades
    int w1 = this.computeMatchWeight();
    int w2 = o.computeMatchWeight();
    if (w1 > w2) {
      return -1;
    }
    if (w1 < w2) {
      return 1;
    }

    if (this.revenue != null && o.revenue != null) {
      return -1 * (this.revenue.compareTo(o.revenue));
    }

    return 0;
  }

  public String getRecType() {
    return recType;
  }

  public void setRecType(String recType) {
    this.recType = recType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCountryCd() {
    return countryCd;
  }

  public void setCountryCd(String countryCd) {
    this.countryCd = countryCd;
  }

  public String getStreetAddress1() {
    return streetAddress1;
  }

  public void setStreetAddress1(String streetAddress1) {
    this.streetAddress1 = streetAddress1;
  }

  public String getStreetAddress2() {
    return streetAddress2;
  }

  public void setStreetAddress2(String streetAddress2) {
    this.streetAddress2 = streetAddress2;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getIssuingCntry() {
    return issuingCntry;
  }

  public void setIssuingCntry(String issuingCntry) {
    this.issuingCntry = issuingCntry;
  }

  public String getDunsNo() {
    return dunsNo;
  }

  public void setDunsNo(String dunsNo) {
    this.dunsNo = dunsNo;
  }

  public String getGoeStatus() {
    return goeStatus;
  }

  public void setGoeStatus(String goeStatus) {
    this.goeStatus = goeStatus;
  }

  public String getStateProv() {
    return stateProv;
  }

  public void setStateProv(String stateProv) {
    this.stateProv = stateProv;
  }

  public String getPostCd() {
    return postCd;
  }

  public void setPostCd(String postCd) {
    this.postCd = postCd;
  }

  public String getVat() {
    return vat;
  }

  public void setVat(String vat) {
    this.vat = vat;
  }

  public String getVatInd() {
    return vatInd;
  }

  public void setVatInd(String vatInd) {
    this.vatInd = vatInd;
  }

  public String getMatchGrade() {
    return matchGrade;
  }

  public void setMatchGrade(String matchGrade) {
    this.matchGrade = matchGrade;
  }

  public boolean isHasCmr() {
    return hasCmr;
  }

  public void setHasCmr(boolean hasCmr) {
    this.hasCmr = hasCmr;
  }

  public boolean isHasDnb() {
    return hasDnb;
  }

  public void setHasDnb(boolean hasDnb) {
    this.hasDnb = hasDnb;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public String getSubRegion() {
    return subRegion;
  }

  public void setSubRegion(String subRegion) {
    this.subRegion = subRegion;
  }

  public String getAltName() {
    return altName;
  }

  public void setAltName(String altName) {
    this.altName = altName;
  }

  public String getAltStreet() {
    return altStreet;
  }

  public void setAltStreet(String altStreet) {
    this.altStreet = altStreet;
  }

  public String getAltCity() {
    return altCity;
  }

  public void setAltCity(String altCity) {
    this.altCity = altCity;
  }

  private int computeMatchWeight() {
    switch (this.matchGrade) {
    case "A":
      return 100;
    case "E1":
      return 95;
    case "E2":
      return 90;
    case "E3":
      return 85;
    case "E4":
      return 80;
    case "F1":
      return 75;
    case "F2":
      return 70;
    case "F3":
      return 65;
    case "F4":
      return 60;
    case "LANG":
      return 98;
    case "DUNS":
      return 67;
    default:
      return 0;
    }
  }

  public Double getRevenue() {
    return revenue;
  }

  public void setRevenue(Double revenue) {
    this.revenue = revenue;
  }

  public boolean isHighestRevenue() {
    return highestRevenue;
  }

  public void setHighestRevenue(boolean highestRevenue) {
    this.highestRevenue = highestRevenue;
  }

  public String getTaxCd1() {
    return taxCd1;
  }

  public void setTaxCd1(String taxCd1) {
    this.taxCd1 = taxCd1;
  }

  public boolean isOrgIdMatch() {
    return orgIdMatch;
  }

  public void setOrgIdMatch(boolean orgIdMatch) {
    this.orgIdMatch = orgIdMatch;
  }

  public String getOperStatusCode() {
    return operStatusCode;
  }

  public void setOperStatusCode(String operStatusCode) {
    this.operStatusCode = operStatusCode;
  }

  public String getRestrictTo() {
    return restrictTo;
  }

  public void setRestrictTo(String restrictTo) {
    this.restrictTo = restrictTo;
  }

  public boolean isPoolRecord() {
    return isPoolRecord;
  }

  public void setPoolRecord(boolean isPoolRecord) {
    this.isPoolRecord = isPoolRecord;
  }

  public String getAddDnBMatches() {
    return addDnBMatches;
  }

  public void setAddDnBMatches(String addDnBMatches) {
    this.addDnBMatches = addDnBMatches;
  }

  public String getCied() {
    return cied;
  }

  public void setCied(String cied) {
    this.cied = cied;
  }

  public long getOverrideReqId() {
    return overrideReqId;
  }

  public void setOverrideReqId(long overrideReqId) {
    this.overrideReqId = overrideReqId;
  }
}
