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
public class ContentTag extends TagSupport {

  private static final long serialVersionUID = -3883930527541467652L;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    sb.append("<div class=\"ibm-columns\">\n");

    sb.append("<!--  All Content -->\n");
    sb.append("  <div class=\"cmr-all-sub\" id=\"cmr-all-sub\">\n");
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

    sb.append("  </div>\n");
    sb.append("</div>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }
}
