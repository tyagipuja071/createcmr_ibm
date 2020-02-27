/**
 * 
 */
package com.ibm.cio.cmr.request.model.requestentry;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.cio.cmr.request.model.BaseModel;

/**
 * @author Eduard Bernardo
 * 
 */
public class ValidationUrlModel extends BaseModel implements Comparable<ValidationUrlModel> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private long displaySeqNo;
  private String cntryCd;
  private String url;
  private String descrTxt;
  private Date createTs;
  private String createBy;
  private Date updtTs;
  private String updtBy;
  private String comments;
  private String createTsString;
  private String updtTsString;

  public long getDisplaySeqNo() {
    return displaySeqNo;
  }

  public void setDisplaySeqNo(long displaySeqNo) {
    this.displaySeqNo = displaySeqNo;
  }

  public String getCntryCd() {
    return cntryCd;
  }

  public void setCntryCd(String cntryCd) {
    this.cntryCd = cntryCd;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDescrTxt() {
    return descrTxt;
  }

  public void setDescrTxt(String descrTxt) {
    this.descrTxt = descrTxt;
  }

  @Override
  public int compareTo(ValidationUrlModel o) {
    if (this.displaySeqNo < o.displaySeqNo) {
      return -1;
    } else if (this.displaySeqNo > o.displaySeqNo) {
      return 1;
    }
    return 0;
  }

  public Date getCreateTs() {
    return createTs;
  }

  public void setCreateTs(Date createTs) {
    this.createTs = createTs;
  }

  public String getCreateBy() {
    return createBy;
  }

  public void setCreateBy(String createBy) {
    this.createBy = createBy;
  }

  public Date getUpdtTs() {
    return updtTs;
  }

  public void setUpdtTs(Date updtTs) {
    this.updtTs = updtTs;
  }

  public String getUpdtBy() {
    return updtBy;
  }

  public void setUpdtBy(String updtBy) {
    this.updtBy = updtBy;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public String getCreateTsString() {
    return createTsString;
  }

  public void setCreateTsString(String createTsString) {
    this.createTsString = createTsString;
  }

  public String getUpdtTsString() {
    return updtTsString;
  }

  public void setUpdtTsString(String updtTsString) {
    this.updtTsString = updtTsString;
  }

  @Override
  public boolean allKeysAssigned() {
    return (this.displaySeqNo > 0) && !StringUtils.isEmpty(this.cntryCd);
  }

  @Override
  public String getRecordDescription() {
    return "Validation URL";
  }

  @Override
  public void addKeyParameters(ModelAndView mv) {
    mv.addObject("internalTyp", this.displaySeqNo);
    mv.addObject("cmrIssuingCntry", this.cntryCd);
  }

  @Override
  public void addKeyParameters(ModelMap map) {
    map.put("internalTyp", this.displaySeqNo);
    map.put("cmrIssuingCntry", this.cntryCd);
  }

}
