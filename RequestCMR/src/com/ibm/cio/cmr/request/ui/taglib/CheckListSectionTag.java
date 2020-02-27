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
public class CheckListSectionTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String name;

  @Override
  public int doStartTag() throws JspException {

    StringBuilder sb = new StringBuilder();
    sb.append("  <tr>\n");
    sb.append("    <td colspan=\"3\">\n");
    sb.append("      <table class=\"checklist-questions\">\n");
    sb.append("        <tr>\n");
    sb.append("          <th colspan=\"3\">" + StringEscapeUtils.escapeHtml(this.name) + "</th>\n");
    sb.append("        </tr>\n");
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
    sb.append("      </table>\n");
    sb.append("    </td>\n");
    sb.append("  </tr>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
