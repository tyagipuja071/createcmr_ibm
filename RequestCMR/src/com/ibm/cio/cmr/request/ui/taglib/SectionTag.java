/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Container for one section of the page.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class SectionTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String id;
  private boolean hidden;
  private boolean alwaysShown;
  private boolean addBackground;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();
    String id = this.id != null ? " id=\"" + this.id + "\"" : "";
    String style = this.hidden ? " style=\"display:none\"" : "";
    String always = this.alwaysShown ? " cmr-nohide" : "";
    String bg = this.addBackground ? " cmr-bg" : "";
    sb.append("        <div class=\"cmr-sub" + always + bg + "\" " + id + " " + style + ">\n");
    sb.append("          <div class=\"ibm-columns\">\n");
    sb.append("            <div class=\"ibm-col-1-1\">\n");
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

    sb.append("            </div>\n");
    sb.append("          </div>\n");
    sb.append("        </div>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  public boolean isAlwaysShown() {
    return alwaysShown;
  }

  public void setAlwaysShown(boolean alwaysShown) {
    this.alwaysShown = alwaysShown;
  }

  public boolean isAddBackground() {
    return addBackground;
  }

  public void setAddBackground(boolean addBackground) {
    this.addBackground = addBackground;
  }

}
