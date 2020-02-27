/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.ibm.cio.cmr.request.ui.GridColumn;

/**
 * Configures a column to be included in the {@link GridTag}
 * 
 * @author Jeffrey Zamora
 * 
 */
public class GridColumnTag extends BodyTagSupport {

  private static final long serialVersionUID = 1L;

  private String field;
  private String header;
  private String width;
  private String formatter;
  private String comparator;
  private String align;

  @Override
  public int doStartTag() throws JspException {
    return EVAL_BODY_INCLUDE;
  }

  @Override
  public int doEndTag() throws JspException {
    GridTag grid = (GridTag) findAncestorWithClass(this, GridTag.class);
    if (grid != null) {
      GridColumn col = new GridColumn();
      col.setField(this.field);
      col.setName(this.header);
      col.setWidth(this.width);
      col.setAlign(this.align);
      col.setFormatter(this.formatter);
      col.setComparator(this.comparator);
      grid.addColumn(col);
    }
    return EVAL_PAGE;
  }

  public String getField() {
    return field;
  }

  public void setField(String field) {
    this.field = field;
  }

  public String getHeader() {
    return header;
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getFormatter() {
    return formatter;
  }

  public void setFormatter(String formatter) {
    this.formatter = formatter;
  }

  public String getAlign() {
    return align;
  }

  public void setAlign(String align) {
    this.align = align;
  }

  public String getComparator() {
    return comparator;
  }

  public void setComparator(String comparator) {
    this.comparator = comparator;
  }
}
