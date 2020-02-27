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
public class RowTag extends TagSupport {

  private static final long serialVersionUID = 1L;
  private boolean addBackground;
  private int topPad;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();
    String style = this.topPad > 0 ? " style=\"padding-top:" + this.topPad + "px\"" : "";
    sb.append("              <div class=\"ibm-columns" + (this.addBackground ? " cmr-bg" : "") + "\"" + style + ">\n");
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

  public boolean isAddBackground() {
    return addBackground;
  }

  public void setAddBackground(boolean addBackground) {
    this.addBackground = addBackground;
  }

  public int getTopPad() {
    return topPad;
  }

  public void setTopPad(int topPad) {
    this.topPad = topPad;
  }
}
