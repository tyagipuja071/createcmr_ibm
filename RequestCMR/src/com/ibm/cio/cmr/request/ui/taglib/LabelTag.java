/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;

/**
 * Creates a label container tag
 * 
 * @author Jeffrey Zamora
 * 
 */
public class LabelTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String fieldId;
  private String pageMgrFieldId;
  private boolean forRadioOrCheckbox;
  private String cssClass;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    String css = this.cssClass != null ? this.cssClass : "";
    if (this.forRadioOrCheckbox) {
      css += " cmr-radio-check-label";
    }
    String style = !StringUtils.isEmpty(css) ? "class=\"" + css + "\"" : "";

    sb.append("<label for=\"" + this.fieldId + "\" " + style + ">\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_BODY_INCLUDE;
  }

  @Override
  public int doEndTag() throws JspException {
    StringBuilder sb = new StringBuilder();
    sb.append("</label>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public boolean isForRadioOrCheckbox() {
    return forRadioOrCheckbox;
  }

  public void setForRadioOrCheckbox(boolean forRadioOrCheckbox) {
    this.forRadioOrCheckbox = forRadioOrCheckbox;
  }

  public String getCssClass() {
    return cssClass;
  }

  public void setCssClass(String cssClass) {
    this.cssClass = cssClass;
  }

  public String getPageMgrFieldId() {
    return pageMgrFieldId;
  }

  public void setPageMgrFieldId(String pageMgrFieldId) {
    this.pageMgrFieldId = pageMgrFieldId;
  }
}
