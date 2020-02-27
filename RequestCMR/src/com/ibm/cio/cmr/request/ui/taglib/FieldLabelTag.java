/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.ibm.cio.cmr.request.ui.PageManager;

/**
 * @author Jeffrey Zamora
 * 
 */
public class FieldLabelTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String fieldId;
  private String defaultLabel;

  @Override
  public int doStartTag() throws JspException {
    HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
    String label = PageManager.getLabel(request, this.fieldId, this.defaultLabel != null ? this.defaultLabel : "(NoLabelFound)");
    try {
      this.pageContext.getOut().write("<span id=\"cmr-fld-lbl-" + this.fieldId + "\">" + label + "</span>");
    } catch (IOException e) {
      throw new JspException(e);
    }
    return SKIP_BODY;
  }

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getDefaultLabel() {
    return defaultLabel;
  }

  public void setDefaultLabel(String defaultLabel) {
    this.defaultLabel = defaultLabel;
  }

}
