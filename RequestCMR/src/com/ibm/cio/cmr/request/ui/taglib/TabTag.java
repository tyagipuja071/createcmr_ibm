/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Tag for individual tabs
 * 
 * @author Jeffrey Zamora
 * 
 */
public class TabTag extends TagSupport {

  private static final long serialVersionUID = 1L;
  private static final String DEFAULT_CLICK_FUNCTION = "switchTabs";

  private String label;
  private String id;
  private boolean active;
  private String onClick;
  private String sectionId;
  private String gridIds;

  @SuppressWarnings("unchecked")
  @Override
  public int doStartTag() throws JspException {
    HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
    List<TabInfo> tabInfoList = (List<TabInfo>) request.getAttribute("pageTabs");

    StringBuilder sb = new StringBuilder();

    String click = DEFAULT_CLICK_FUNCTION + "('" + this.sectionId + "')";
    if (this.onClick != null) {
      click = this.onClick;
    }
    String clickFunc = click.replaceAll("'", "\\\\'");
    clickFunc = "cmr.selectTab(this, '" + clickFunc + "')";

    sb.append("        <div id=\"" + this.id + "\" class=\"cmr-tab cmr-search-tab-crit" + (this.active ? " active" : "") + "\" onclick=\""
        + clickFunc + "\">" + this.label + "</div>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    if (tabInfoList != null) {
      TabInfo tab = new TabInfo(this.id, this.sectionId, this.gridIds != null ? this.gridIds.split(",") : null);
      tabInfoList.add(tab);
    }
    return SKIP_BODY;
  }

  @Override
  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getOnClick() {
    return onClick;
  }

  public void setOnClick(String onClick) {
    this.onClick = onClick;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public String getSectionId() {
    return sectionId;
  }

  public void setSectionId(String sectionId) {
    this.sectionId = sectionId;
  }

  public String getGridIds() {
    return gridIds;
  }

  public void setGridIds(String gridIds) {
    this.gridIds = gridIds;
  }

}
