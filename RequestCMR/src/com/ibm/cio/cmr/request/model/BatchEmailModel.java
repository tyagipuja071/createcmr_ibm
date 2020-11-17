<<<<<<< HEAD
package com.ibm.cio.cmr.request.model;

/**
 * Container for parameters in email notification. The class has a backing Map
 * for the params.
 * 
 * @author Garima Narang
 * 
 */

public class BatchEmailModel {

  String mailSubject;
  String receipent;
  String requesterName;
  String requesterId;
  String requestId;
  String custNm;
  String issuingCountry;
  String cmrNumber;
  String subregion;
  String directUrlLink;
  String stringToReplace;
  String valToBeReplaceBy;

  public String getMailSubject() {
    return mailSubject;
  }

  public void setMailSubject(String mailSubject) {
    this.mailSubject = mailSubject;
  }

  public String getReceipent() {
    return receipent;
  }

  public void setReceipent(String receipent) {
    this.receipent = receipent;
  }

  public String getRequesterName() {
    return requesterName;
  }

  public void setRequesterName(String requesterName) {
    this.requesterName = requesterName;
  }

  public String getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getCustNm() {
    return custNm;
  }

  public void setCustNm(String custNm) {
    this.custNm = custNm;
  }

  public String getIssuingCountry() {
    return issuingCountry;
  }

  public void setIssuingCountry(String issuingCountry) {
    this.issuingCountry = issuingCountry;
  }

  public String getCmrNumber() {
    return cmrNumber;
  }

  public void setCmrNumber(String cmrNumber) {
    this.cmrNumber = cmrNumber;
  }

  public String getSubregion() {
    return subregion;
  }

  public void setSubregion(String subregion) {
    this.subregion = subregion;
  }

  public String getDirectUrlLink() {
    return directUrlLink;
  }

  public void setDirectUrlLink(String directUrlLink) {
    this.directUrlLink = directUrlLink;
  }

  public String getStringToReplace() {
    return stringToReplace;
  }

  public void setStringToReplace(String stringToReplace) {
    this.stringToReplace = stringToReplace;
  }

  public String getValToBeReplaceBy() {
    return valToBeReplaceBy;
  }

  public void setValToBeReplaceBy(String valToBeReplaceBy) {
    this.valToBeReplaceBy = valToBeReplaceBy;
  }

}
=======
package com.ibm.cio.cmr.request.model;

/**
 * Container for parameters in email notification. The class has a backing Map
 * for the params.
 * 
 * @author Garima Narang
 * 
 */

public class BatchEmailModel {

  String mailSubject;
  String receipent;
  String requesterName;
  String requesterId;
  String requestId;
  String custNm;
  String issuingCountry;
  String cmrNumber;
  String subregion;
  String directUrlLink;
  String stringToReplace;
  String valToBeReplaceBy;
  String addtlField1Value;
  String addtlField2Value;
  boolean enableAddlField1;
  boolean enableAddlField2;

  public String getMailSubject() {
    return mailSubject;
  }

  public void setMailSubject(String mailSubject) {
    this.mailSubject = mailSubject;
  }

  public String getReceipent() {
    return receipent;
  }

  public void setReceipent(String receipent) {
    this.receipent = receipent;
  }

  public String getRequesterName() {
    return requesterName;
  }

  public void setRequesterName(String requesterName) {
    this.requesterName = requesterName;
  }

  public String getRequesterId() {
    return requesterId;
  }

  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public String getCustNm() {
    return custNm;
  }

  public void setCustNm(String custNm) {
    this.custNm = custNm;
  }

  public String getIssuingCountry() {
    return issuingCountry;
  }

  public void setIssuingCountry(String issuingCountry) {
    this.issuingCountry = issuingCountry;
  }

  public String getCmrNumber() {
    return cmrNumber;
  }

  public void setCmrNumber(String cmrNumber) {
    this.cmrNumber = cmrNumber;
  }

  public String getSubregion() {
    return subregion;
  }

  public void setSubregion(String subregion) {
    this.subregion = subregion;
  }

  public String getDirectUrlLink() {
    return directUrlLink;
  }

  public void setDirectUrlLink(String directUrlLink) {
    this.directUrlLink = directUrlLink;
  }

  public String getStringToReplace() {
    return stringToReplace;
  }

  public void setStringToReplace(String stringToReplace) {
    this.stringToReplace = stringToReplace;
  }

  public String getValToBeReplaceBy() {
    return valToBeReplaceBy;
  }

  public void setValToBeReplaceBy(String valToBeReplaceBy) {
    this.valToBeReplaceBy = valToBeReplaceBy;
  }

  public String getAddtlField1Value() {
    return addtlField1Value;
  }

  public void setAddtlField1Value(String addtlField1Value) {
    this.addtlField1Value = addtlField1Value;
  }

  public String getAddtlField2Value() {
    return addtlField2Value;
  }

  public void setAddtlField2Value(String addtlField2Value) {
    this.addtlField2Value = addtlField2Value;
  }

  public boolean isEnableAddlField1() {
    return enableAddlField1;
  }

  public void setEnableAddlField1(boolean enableAddlField1) {
    this.enableAddlField1 = enableAddlField1;
  }

  public boolean isEnableAddlField2() {
    return enableAddlField2;
  }

  public void setEnableAddlField2(boolean enableAddlField2) {
    this.enableAddlField2 = enableAddlField2;
  }

}
>>>>>>> green_south_africa
