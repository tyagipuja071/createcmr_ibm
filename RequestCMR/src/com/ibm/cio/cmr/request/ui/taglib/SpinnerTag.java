/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SpinnerTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String fieldId;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    sb.append("<span id=\"" + this.fieldId + "_spinner\" class=\"ibm-spinner-small\"></span>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
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
}
