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
public class ModalTag extends TagSupport {

  private String id;
  private String title;
  private int widthId;

  private static final long serialVersionUID = 1L;

  @Override
  public int doStartTag() throws JspException {
    StringBuilder sb = new StringBuilder();
    sb.append("<div class=\"ibm-common-overlay " + getModalClass() + "\"");
    sb.append(" id=\"" + this.id + "\" title=\"" + this.title + "\">");
    sb.append(" <div class=\"ibm-head\">");
    sb.append("   <p>");
    // sb.append("     <a class=\"ibm-common-overlay-close\" href=\"#close\">Close [x]</a>");
    sb.append("   </p>");
    sb.append(" </div>");
    sb.append("");
    sb.append(" <div class=\"ibm-body\">");
    sb.append("   <div class=\"ibm-main\">");
    sb.append("     <div class=\"ibm-title\">");
    sb.append("       <h2 id=\"" + this.id + "Title\">" + this.title + "</h2>");
    sb.append("     </div>");
    sb.append("     <div class=\"ibm-container ibm-alternate\">");
    sb.append("       <div class=\"ibm-container-body\">");
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
    sb.append("       </div>");
    sb.append("     </div>");
    sb.append("   </div>");
    sb.append(" </div>");
    sb.append("</div>");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

  private String getModalClass() {
    switch (this.widthId) {
    case 980:
      return "ibm-overlay-alt-three";
    case 750:
      return "ibm-overlay-alt-two";
    case 570:
      return "ibm-overlay-alt";
    default:
      return "";
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getWidthId() {
    return widthId;
  }

  public void setWidthId(int widthId) {
    this.widthId = widthId;
  }

}
