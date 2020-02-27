/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Renders the information bubble
 * 
 * @author Jeffrey Zamora
 * 
 */
public class InfoTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String text;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
    String resources = request.getContextPath() + "/resources";

    sb.append("<img src=\"" + resources + "/images/info-bubble-icon.png\" title=\"" + this.text + "\" class=\"cmr-info-bubble\">");
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

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}
