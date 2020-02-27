/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Creates an HR
 * 
 * @author Jeffrey Zamora
 * 
 */
public class HrTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();
    sb.append("<div class=\"ibm-rule\"><hr></div>\n");
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
}
