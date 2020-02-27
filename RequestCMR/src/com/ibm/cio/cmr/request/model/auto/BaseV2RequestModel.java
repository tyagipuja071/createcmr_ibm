/**
 * 
 */
package com.ibm.cio.cmr.request.model.auto;

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;
import com.ibm.cio.cmr.request.model.requestentry.RequestEntryModel;

/**
 * @author JeffZAMORA
 * 
 */
public class BaseV2RequestModel extends BaseModel {

  private String cmrIssuingCntry;
  private String reqType;
  private String requestingLob;
  private String reqReason;
  private String custGrp;
  private String custSubGrp;
  private String cmrNo;
  private long reqId;
  private String dplChkResult;
  private String findCmrResult;
  private String findDnbResult;
  private String comment;
  private String custClass;
  private String dupCmrRsn;

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public static <T extends BaseV2RequestModel> T createFromRequest(RequestEntryModel model, HttpServletRequest request, Class<T> targetClass)
      throws Exception {
    T instance = targetClass.newInstance();

    PropertyUtils.copyProperties(instance, model);
    for (Enumeration<String> elems = request.getParameterNames(); elems.hasMoreElements();) {
      String paramName = elems.nextElement();
      try {
        PropertyUtils.copyProperties(instance, Collections.singletonMap(paramName, request.getParameter(paramName)));
      } catch (Exception e) {
        // do a one by one copy so that only the errors will be skipped, not
        // whole copy
      }
    }

    return instance;

  }

  @Override
  public boolean allKeysAssigned() {
    return false;
  }

  @Override
  public String getRecordDescription() {
    return null;
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
  }

  @Override
  public void addKeyParameters(ModelMap map) {
  }

  public String getCmrIssuingCntry() {
    return cmrIssuingCntry;
  }

  public void setCmrIssuingCntry(String cmrIssuingCntry) {
    this.cmrIssuingCntry = cmrIssuingCntry;
  }

  public String getReqType() {
    return reqType;
  }

  public void setReqType(String reqType) {
    this.reqType = reqType;
  }

  public String getRequestingLob() {
    return requestingLob;
  }

  public void setRequestingLob(String requestingLob) {
    this.requestingLob = requestingLob;
  }

  public String getReqReason() {
    return reqReason;
  }

  public void setReqReason(String reqReason) {
    this.reqReason = reqReason;
  }

  public String getCustGrp() {
    return custGrp;
  }

  public void setCustGrp(String custGrp) {
    this.custGrp = custGrp;
  }

  public String getCustSubGrp() {
    return custSubGrp;
  }

  public void setCustSubGrp(String custSubGrp) {
    this.custSubGrp = custSubGrp;
  }

  public long getReqId() {
    return reqId;
  }

  public void setReqId(long reqId) {
    this.reqId = reqId;
  }

  public String getDplChkResult() {
    return dplChkResult;
  }

  public void setDplChkResult(String dplChkResult) {
    this.dplChkResult = dplChkResult;
  }

  public String getFindCmrResult() {
    return findCmrResult;
  }

  public void setFindCmrResult(String findCmrResult) {
    this.findCmrResult = findCmrResult;
  }

  public String getFindDnbResult() {
    return findDnbResult;
  }

  public void setFindDnbResult(String findDnbResult) {
    this.findDnbResult = findDnbResult;
  }

  public String getCmrNo() {
    return cmrNo;
  }

  public void setCmrNo(String cmrNo) {
    this.cmrNo = cmrNo;
  }

  public String getCustClass() {
    return custClass;
  }

  public void setCustClass(String custClass) {
    this.custClass = custClass;
  }

  public String getDupCmrRsn() {
    return dupCmrRsn;
  }

  public void setDupCmrRsn(String dupCmrRsn) {
    this.dupCmrRsn = dupCmrRsn;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

}
