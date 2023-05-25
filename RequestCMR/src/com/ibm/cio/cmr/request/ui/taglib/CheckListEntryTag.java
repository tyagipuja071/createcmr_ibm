/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang3.StringUtils;

import com.ibm.cio.cmr.request.model.requestentry.CheckListModel;

/**
 * 
 * @author Jeffrey Zamora
 * 
 */
public class CheckListEntryTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String section;
  private int number = -1;
  private boolean dplField;
  private boolean matchField;
  private String displayNumber;

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  @Override
  public int doStartTag() throws JspException {

    HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
    CheckListModel model = (CheckListModel) request.getAttribute("checklist");
    if (model == null) {
      throw new JspException("The 'checklist' attribute was not added to the view.");
    }

    if (!StringUtils.isEmpty(this.section) && this.number <= 0) {
      throw new JspException("number missing while section was specified.");
    }

    if (StringUtils.isEmpty(this.section) && this.number > 0) {
      throw new JspException("section missing while number was specified.");
    }

    if (this.section != null && this.section.compareTo("E") > 0) {
      throw new JspException("Can only specify up to section E.");
    }

    if (this.number > 10) {
      throw new JspException("Can only specify up to number 10.");
    }

    StringBuilder sb = new StringBuilder();
    sb.append("        <tr>\n");
    sb.append(
        "          <td width=\"5%\">" + (this.number > 0 ? (this.displayNumber == null ? this.number : this.displayNumber) : "&nbsp;") + "</td>\n");
    sb.append("          <td width=\"*\">\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_BODY_INCLUDE;
  }

  @Override
  public int doEndTag() throws JspException {
    HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
    CheckListModel checklist = (CheckListModel) request.getAttribute("checklist");

    StringBuilder sb = new StringBuilder();
    sb.append("          </td>\n");
    sb.append("          <td width=\"12%\">\n");
    if (this.number > 0) {
      String val = getCheckListValue(checklist);
      // generate the yes/no questions here
      sb.append("            <span class=\"checklist-radio\">\n");
      sb.append("              <input type=\"radio\" name=\"section" + this.section.toUpperCase() + this.number + "\" value=\"Y\" "
          + ("Y".equals(val) ? "checked" : "") + " >Yes\n");
      sb.append("            </span>\n");
      sb.append("            <span class=\"checklist-radio\">\n");
      sb.append("              <input type=\"radio\" name=\"section" + this.section.toUpperCase() + this.number + "\" value=\"N\" "
          + ("N".equals(val) ? "checked" : "") + " >No\n");
      sb.append("            </span>\n");
    } else {
      if (this.dplField) {
        String val = checklist.getUsDplSanctioned();
        sb.append("            <span class=\"checklist-radio\">\n");
        sb.append("              <input type=\"radio\" name=\"usDplSanctioned\" value=\"Y\" " + ("Y".equals(val) ? "checked" : "") + " >Yes\n");
        sb.append("            </span>\n");
        sb.append("            <span class=\"checklist-radio\">\n");
        sb.append("              <input type=\"radio\" name=\"usDplSanctioned\" value=\"N\" " + ("N".equals(val) ? "checked" : "") + " >No\n");
        sb.append("            </span>\n");
      } else if (this.matchField) {
        String val = checklist.getPotentialMatch();
        sb.append("            <span class=\"checklist-radio\">\n");
        sb.append("              <input type=\"radio\" name=\"potentialMatch\" value=\"Y\" " + ("Y".equals(val) ? "checked" : "") + " >Yes\n");
        sb.append("            </span>\n");
        sb.append("            <span class=\"checklist-radio\">\n");
        sb.append("              <input type=\"radio\" name=\"potentialMatch\" value=\"N\" " + ("N".equals(val) ? "checked" : "") + " >No\n");
        sb.append("            </span>\n");
      } else {
        sb.append("            &nbsp;\n");
      }
    }
    sb.append("          </td>\n");
    sb.append("        </tr>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

  private String getCheckListValue(CheckListModel checklist) {
    try {
      Class<CheckListModel> chkClass = CheckListModel.class;

      String methodName = "getSection" + this.section.toUpperCase() + this.number;
      Method get = chkClass.getMethod(methodName, (Class[]) null);
      if (get != null) {
        return (String) get.invoke(checklist, (Object[]) null);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;

  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

  public boolean isDplField() {
    return dplField;
  }

  public void setDplField(boolean dplField) {
    this.dplField = dplField;
  }

  public boolean isMatchField() {
    return matchField;
  }

  public void setMatchField(boolean matchField) {
    this.matchField = matchField;
  }

  public String getDisplayNumber() {
    return displayNumber;
  }

  public void setDisplayNumber(String displayNumber) {
    this.displayNumber = displayNumber;
  }
}
