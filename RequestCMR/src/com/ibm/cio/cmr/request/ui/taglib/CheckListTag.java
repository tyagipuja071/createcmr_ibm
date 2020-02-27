/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Jeffrey Zamora
 * 
 */
public class CheckListTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String title1;
  private String title2;

  @Override
  public int doStartTag() throws JspException {

    StringBuilder sb = new StringBuilder();
    sb.append("<table class=\"checklist\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
    try {
      sb.append("  <tr>\n");
      sb.append("    <th colspan=\"3\" class=\"header\">" + StringEscapeUtils.escapeHtml(this.title1) + "</th>\n");
      sb.append("  </tr>\n");
      if (!StringUtils.isBlank(this.title2)) {
        sb.append("  <tr>\n");
        sb.append("    <th colspan=\"3\" class=\"header\">" + StringEscapeUtils.escapeHtml(this.title2) + "</th>\n");
        sb.append("  </tr>\n");
      }
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_BODY_INCLUDE;
  }

  @Override
  public int doEndTag() throws JspException {
    StringBuilder sb = new StringBuilder();
    sb.append("</table>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

  public String getTitle1() {
    return title1;
  }

  public void setTitle1(String title1) {
    this.title1 = title1;
  }

  public String getTitle2() {
    return title2;
  }

  public void setTitle2(String title2) {
    this.title2 = title2;
  }

}
