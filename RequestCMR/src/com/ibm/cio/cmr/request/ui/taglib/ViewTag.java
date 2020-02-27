/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.ui.PageManager;

/**
 * Creates a column on the section's row
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ViewTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String forCountry;
  private String exceptForCountry;
  private String exceptForGEO;
  private String forGEO;
  private boolean noEval;

  @Override
  public int doStartTag() throws JspException {
    HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
    String cntry = PageManager.getCurrentCountry(request);

    if (!StringUtils.isBlank(this.exceptForCountry) && !StringUtils.isBlank(cntry)) {
      List<String> cntryList = Arrays.asList(this.exceptForCountry.trim().split(","));
      if (cntryList.contains(cntry)) {
        this.noEval = true;
        return SKIP_BODY;
      }
    }

    if (!StringUtils.isBlank(this.exceptForGEO) && !StringUtils.isBlank(cntry)) {
      List<String> geoList = Arrays.asList(this.exceptForGEO.trim().split(","));
      for (String geo : geoList) {
        if (PageManager.fromGeo(geo, cntry)) {
          this.noEval = true;
          return SKIP_BODY;
        }
      }
    }

    if (!StringUtils.isBlank(this.forCountry) && !StringUtils.isBlank(cntry)) {
      List<String> cntryList = Arrays.asList(this.forCountry.trim().split(","));
      if (!cntryList.contains(cntry)) {
        this.noEval = true;
        return SKIP_BODY;
      }
    }

    if (!StringUtils.isBlank(this.forGEO) && !StringUtils.isBlank(cntry)) {
      boolean inGeo = false;
      for (String geo : this.forGEO.trim().split(",")) {
        if (PageManager.fromGeo(geo, cntry)) {
          inGeo = true;
          break;
        }
      }
      if (!inGeo) {
        this.noEval = true;
        return SKIP_BODY;
      }
    }
    return EVAL_BODY_INCLUDE;
  }

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  public String getForCountry() {
    return forCountry;
  }

  public void setForCountry(String forCountry) {
    this.forCountry = forCountry;
  }

  public String getForGEO() {
    return forGEO;
  }

  public void setForGEO(String forGEO) {
    this.forGEO = forGEO;
  }

  public boolean isNoEval() {
    return noEval;
  }

  public void setNoEval(boolean noEval) {
    this.noEval = noEval;
  }

  public String getExceptForCountry() {
    return exceptForCountry;
  }

  public void setExceptForCountry(String exceptForCountry) {
    this.exceptForCountry = exceptForCountry;
  }

  public String getExceptForGEO() {
    return exceptForGEO;
  }

  public void setExceptForGEO(String exceptForGEO) {
    this.exceptForGEO = exceptForGEO;
  }

}
