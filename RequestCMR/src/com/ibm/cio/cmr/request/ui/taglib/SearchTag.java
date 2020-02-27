/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author Jeffrey Zamora
 * 
 */
public class SearchTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String fieldId;
  private String queryId;
  private String title;
  private String textId;

  @Override
  public int doStartTag() throws JspException {

    HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
    String resourcesPath = request.getContextPath() + "/resources";

    StringBuilder sb = new StringBuilder();

    if (this.title == null) {
      this.title = "Code";
    }

    if (this.textId == null) {
      this.textId = "null";
    } else
      this.textId = "'" + this.textId + "'";

    sb.append("<img src=\"" + resourcesPath + "/images/remove-icon.png\" class=\"cmr-remove-icon\" onclick=\"cmr.clearSearchValue('" + this.fieldId
        + "', " + this.textId + ")\" title=\"Clear " + this.title + " Value\">");
    sb.append("<img src=\"" + resourcesPath + "/images/search-icon.png\" class=\"cmr-search-icon\" title=\"Search " + this.title
        + "\" onclick=\"cmr.searchCode('" + this.fieldId + "', '" + this.queryId + "', '" + this.title + "', " + this.textId + ")\">");
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

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getQueryId() {
    return queryId;
  }

  public void setQueryId(String queryId) {
    this.queryId = queryId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTextId() {
    return textId;
  }

  public void setTextId(String textId) {
    this.textId = textId;
  }

}
