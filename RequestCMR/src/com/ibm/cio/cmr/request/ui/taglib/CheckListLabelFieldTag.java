/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * 
 * @author Jeffrey Zamora
 * 
 */
public class CheckListLabelFieldTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String labelWidth;
  private String label;
  private boolean addSpace = false;
  private boolean boldLabel = true;

  @Override
  public int doStartTag() throws JspException {

    String colWidth = "width=\"" + this.labelWidth + "\"";
    String tag = this.boldLabel ? "th" : "td";
    StringBuilder sb = new StringBuilder();
    sb.append("  <tr>\n");
    sb.append("    <" + tag + " " + colWidth + " " + (this.addSpace ? "class=\"subheader\"" : "") + ">\n");
    sb.append("      ").append(StringEscapeUtils.escapeHtml(this.label));
    sb.append("    </" + tag + ">\n");
    sb.append("    <td width=\"*\">\n");
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
    sb.append("    </td>\n");
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

  public boolean isBoldLabel() {
    return boldLabel;
  }

  public void setBoldLabel(boolean boldLabel) {
    this.boldLabel = boldLabel;
  }

  public String getLabelWidth() {
    return labelWidth;
  }

  public void setLabelWidth(String labelWidth) {
    this.labelWidth = labelWidth;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

}
