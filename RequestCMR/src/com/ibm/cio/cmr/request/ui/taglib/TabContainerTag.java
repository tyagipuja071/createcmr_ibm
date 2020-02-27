/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Tag for the Tabs container.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class TabContainerTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private boolean noPad;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    sb.append("    <div class=\"cmr-tabs" + (this.noPad ? " nopad" : "") + "\">\n");
    sb.append("      <div class=\"cmr-tab-cont\">\n");
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

    sb.append("      </div>\n");
    sb.append("    </div>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

  public boolean isNoPad() {
    return noPad;
  }

  public void setNoPad(boolean noPad) {
    this.noPad = noPad;
  }

}
