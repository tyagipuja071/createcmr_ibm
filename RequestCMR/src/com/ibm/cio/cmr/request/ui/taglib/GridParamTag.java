/**
 * 
 */
package com.ibm.cio.cmr.request.ui.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Handles grid parameters
 * 
 * @author Jeffrey Zamora
 * 
 */
public class GridParamTag extends BodyTagSupport {

  private static final long serialVersionUID = 1L;

  private String fieldId;
  private String value;

  @Override
  public int doStartTag() throws JspException {
    return SKIP_BODY;
  }

  @Override
  public int doEndTag() throws JspException {
    GridTag grid = (GridTag) findAncestorWithClass(this, GridTag.class);
    if (grid != null) {
      grid.addParam(this.fieldId, this.value != null ? this.value : ":" + this.fieldId);
    }
    return EVAL_PAGE;
  }

  public String getFieldId() {
    return fieldId;
  }

  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
