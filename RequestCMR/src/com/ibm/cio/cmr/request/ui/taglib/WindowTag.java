package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * package com.ibm.cio.cmr.request.ui.taglib;
 * 
 * import java.io.IOException;
 * 
 * /**
 * 
 * @author Jeffrey Zamora
 * 
 */
public class WindowTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  @Override
  public int doStartTag() throws JspException {
    HttpServletRequest request = (HttpServletRequest) this.pageContext.getRequest();
    String title = (String) request.getAttribute("windowtitle");
    RowTag row = new RowTag();
    row.setPageContext(this.pageContext);
    try {
      this.pageContext.getOut().write("<div class=\"cmr-window-content\">\n");
      row.doStartTag();
      this.pageContext.getOut().write("    <div class=\"ibm-columns cmr-window-header\">" + (title != null ? title : "") + "</div>\n");
      row.doEndTag();
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_BODY_INCLUDE;
  }

  @Override
  public int doEndTag() throws JspException {
    try {
      this.pageContext.getOut().write("</div>\n");
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

}
