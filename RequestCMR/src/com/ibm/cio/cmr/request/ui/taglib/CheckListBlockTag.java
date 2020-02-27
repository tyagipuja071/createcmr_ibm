/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 
 * @author Jeffrey Zamora
 * 
 */
public class CheckListBlockTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private boolean addSpace = true;
  private boolean boldText = true;

  @Override
  public int doStartTag() throws JspException {

    String colWidth = "colspan=\"3\"";
    String tag = this.boldText ? "th" : "td";
    StringBuilder sb = new StringBuilder();
    sb.append("  <tr>\n");
    sb.append("    <" + tag + " " + colWidth + " " + (this.addSpace ? "class=\"subheader\"" : "") + ">\n");
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
    String tag = this.boldText ? "th" : "td";
    sb.append("    </" + tag + ">\n");
    sb.append("  </tr>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

  public boolean isAddSpace() {
    return addSpace;
  }

  public void setAddSpace(boolean addSpace) {
    this.addSpace = addSpace;
  }

  public boolean isBoldText() {
    return boldText;
  }

  public void setBoldText(boolean boldText) {
    this.boldText = boldText;
  }

}
