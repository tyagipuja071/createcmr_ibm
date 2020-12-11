/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;

import com.ibm.cio.cmr.request.ui.GridColumn;

/**
 * Tag for a grid
 * 
 * @author Jeffrey Zamora
 * 
 */
public class GridTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  private String id;
  private int span;
  private int height;
  private int width;
  private String url;
  private List<GridColumn> columns;
  private Map<String, String> params;
  private String checkBoxKeys;
  private boolean hasCheckbox;
  private boolean usePaging = true;
  private boolean loadOnStartup = true;
  private boolean useFilter = false;
  private int innerWidth;

  @Override
  public int doStartTag() throws JspException {
    columns = new ArrayList<GridColumn>();
    params = new HashMap<String, String>();
    StringBuilder sb = new StringBuilder();
    String style = this.width > 0 ? "width:" + this.width + "px" : "width:100%";

    sb.append(
        "              <div class=\"ibm-columns\" " + (this.innerWidth > 0 ? "style=\"width:" + this.innerWidth + "px !important\"" : "") + ">\n");
    sb.append("                <div class=\"" + getColumnClass() + "\" style=\"margin:0 !important; overflow-x: auto; " + style + "\">\n");
    sb.append("                  <div id=\"" + this.id + "_GRID\">\n");
    sb.append("                    <span id=\"" + this.id + "_spinner\" class=\"ibm-spinner-small\"></span>\n");
    sb.append("                  </div>\n");
    sb.append("                </div>\n");
    sb.append("              </div>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_BODY_INCLUDE;
  }

  private String getColumnClass() {
    switch (this.span) {
    case 1:
      return "ibm-col-6-1";
    case 2:
      return "ibm-col-6-2";
    case 3:
      return "ibm-col-6-3";
    case 4:
      return "ibm-col-6-4";
    case 5:
      return "ibm-col-6-5";
    default:
      return "ibm-col-1-1";
    }
  }

  @Override
  public int doEndTag() throws JspException {
    StringBuilder sb = new StringBuilder();

    sb.append("<!-- Script for getting contents for the " + this.id + " grid. -->\n");
    sb.append("<script>\n");
    sb.append("  dojo.addOnLoad(function() {\n");

    // construct columns
    sb.append("    var cols = {\n");
    int cnt = 0;
    for (GridColumn col : this.columns) {
      sb.append(cnt > 0 ? ",\n" : "");
      sb.append(col.toString());
      cnt++;
    }
    sb.append("\n");
    sb.append("    };\n\n");

    // construct the structures
    cnt = 0;
    sb.append("    var struct = [");
    if (this.hasCheckbox) {
      sb.append("CmrGrid.createCheckboxColumn('" + this.checkBoxKeys + "', '" + this.id + "')");
      cnt++;
    }
    for (GridColumn col : this.columns) {
      sb.append(cnt > 0 ? ", " : "");
      sb.append("cols." + col.getField());
      cnt++;
    }
    sb.append("];\n\n");

    cnt = 0;
    sb.append("    var compMap = {");
    for (GridColumn col : this.columns) {
      if (!StringUtils.isEmpty(col.getComparator())) {
        sb.append(cnt > 0 ? ",\n" : "");
        sb.append("      ").append(col.getField()).append(" : ").append(col.getComparator());
        cnt++;
      }
    }
    sb.append("    };\n\n");

    String params = getParamString();
    String heightPx = this.height > 0 ? "'" + this.height + "px'" : "null";
    sb.append("    CmrGrid.create('" + this.id + "_GRID', struct, cmr.CONTEXT_ROOT + '" + this.url + "', " + this.hasCheckbox + ", " + params + ", "
        + heightPx + ", " + this.usePaging + ", " + this.loadOnStartup + ", " + this.useFilter + ", compMap);\n");
    sb.append("  });\n");
    sb.append("</script>\n");
    try {
      this.pageContext.getOut().write(sb.toString());
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

  private String getParamString() {
    if (this.params.size() == 0) {
      return "null";
    }
    StringBuilder sb = new StringBuilder();
    for (String key : this.params.keySet()) {
      sb.append(sb.length() > 0 ? "&" : "");
      sb.append(key + "=" + this.params.get(key));
    }
    return "'" + sb.toString() + "'";
  }

  public void addColumn(GridColumn column) {
    this.columns.add(column);
  }

  public void addParam(String name, String value) {
    this.params.put(name, value);
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isHasCheckbox() {
    return hasCheckbox;
  }

  public void setHasCheckbox(boolean hasCheckbox) {
    this.hasCheckbox = hasCheckbox;
  }

  public String getCheckBoxKeys() {
    return checkBoxKeys;
  }

  public void setCheckBoxKeys(String checkBoxKeys) {
    this.checkBoxKeys = checkBoxKeys;
  }

  public int getSpan() {
    return span;
  }

  public void setSpan(int span) {
    this.span = span;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public boolean isUsePaging() {
    return usePaging;
  }

  public void setUsePaging(boolean usePaging) {
    this.usePaging = usePaging;
  }

  public boolean isLoadOnStartup() {
    return loadOnStartup;
  }

  public void setLoadOnStartup(boolean loadOnStartup) {
    this.loadOnStartup = loadOnStartup;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public boolean isUseFilter() {
    return useFilter;
  }

  public void setUseFilter(boolean useFilter) {
    this.useFilter = useFilter;
  }

  public int getInnerWidth() {
    return innerWidth;
  }

  public void setInnerWidth(int innerWidth) {
    this.innerWidth = innerWidth;
  }

}
