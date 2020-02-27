/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Generates a button on the page
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ButtonTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String onClick;
  private String label;
  private boolean highlight;
  private boolean pad;
  private String styleClass;
  private String id;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    if (this.pad) {
      sb.append("<span class=\"ibm-sep\">&nbsp;</span>");
    }
    String sClass = this.styleClass != null ? " " + this.styleClass : "";
    String id = this.id != null ? " id=\"" + this.id + "\"" : "";
    sb.append("<input class=\"ibm-btn-cancel-" + (this.highlight ? "pri" : "sec") + " ibm-btn-small" + sClass + "\" type=\"button\"  value=\""
        + this.label + "\" onclick=\"" + this.onClick + "\"" + id + " title=\"" + this.label + "\">");
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

  public String getOnClick() {
    return onClick;
  }

  public void setOnClick(String onClick) {
    this.onClick = onClick;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public boolean isHighlight() {
    return highlight;
  }

  public void setHighlight(boolean highlight) {
    this.highlight = highlight;
  }

  public boolean isPad() {
    return pad;
  }

  public void setPad(boolean pad) {
    this.pad = pad;
  }

  public String getStyleClass() {
    return styleClass;
  }

  public void setStyleClass(String styleClass) {
    this.styleClass = styleClass;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

}
