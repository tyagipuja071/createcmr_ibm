/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;
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
public class ColumnTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private int span;
  private int width;
  private String containerForField;
  private String forCountry;
  private String exceptForCountry;
  private String exceptForGEO;
  private String forGEO;
  private boolean noEval;

  public String getContainerForField() {
    return containerForField;
  }

  public void setContainerForField(String containerForField) {
    this.containerForField = containerForField;
  }

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

    StringBuilder sb = new StringBuilder();
    String style = this.width > 0 ? "style=\"width:" + this.width + "px\"" : "";
    String id = this.containerForField != null ? " id=\"container-" + this.containerForField + "\"" : "";
    sb.append("                <div class=\"" + getColumnClass() + "\" " + style + " " + id + ">\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_BODY_INCLUDE;
  }

  @Override
  public int doEndTag() throws JspException {
    if (this.noEval) {
      return EVAL_PAGE;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("                </div>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

  private String getColumnClass() {
    switch (this.span) {
    case 1:
      return "ibm-col-6-1";
    case 2:
      return "ibm-col-6-2";
    case 3:
      return "ibm-col-6-3";
    case 4:
      return "ibm-col-6-4";
    case 5:
      return "ibm-col-6-5";
    default:
      return "ibm-col-1-1";
    }
  }

  public int getSpan() {
    return span;
  }

  public void setSpan(int span) {
    this.span = span;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
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
