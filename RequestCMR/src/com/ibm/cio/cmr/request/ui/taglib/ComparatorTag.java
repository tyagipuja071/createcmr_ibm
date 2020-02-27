package com.ibm.cio.cmr.request.ui.taglib;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.ibm.cio.cmr.request.ui.GridColumn;

/**
 * package com.ibm.cio.cmr.request.ui.taglib;
 * 
 * import javax.servlet.jsp.JspException;
 * 
 * /** Comparator for a {@link GridColumn} defined in the {@link GridColumnTag}
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ComparatorTag extends BodyTagSupport {

  private static final long serialVersionUID = 1L;

  private String functionName;

  @Override
  public int doStartTag() throws JspException {
    return EVAL_BODY_BUFFERED;
  }

  @Override
  public int doEndTag() throws JspException {

    GridColumnTag col = (GridColumnTag) findAncestorWithClass(this, GridColumnTag.class);
    if (col != null) {
      if (this.functionName != null) {
        col.setComparator(this.functionName);
      } else if (this.bodyContent != null) {
        col.setComparator(this.bodyContent.getString());
      }
    }
    return EVAL_PAGE;
  }

  public String getFunctionName() {
    return functionName;
  }

  public void setFunctionName(String functionName) {
    this.functionName = functionName;
  }

}
