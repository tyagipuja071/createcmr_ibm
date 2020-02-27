/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Generates the row for buttons
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ButtonsRowTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    sb.append("              <div class=\"ibm-buttons-row\">\n");
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
    sb.append("              </div>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

}
