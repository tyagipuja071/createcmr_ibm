package com.ibm.cio.cmr.request.ui.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.ibm.cio.cmr.request.ui.UIMgr;

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
public class WindowCloseTag extends TagSupport {

  private static final long serialVersionUID = 1L;

  @Override
  public int doStartTag() throws JspException {
    RowTag row = new RowTag();
    HrTag hr = new HrTag();
    row.setPageContext(this.pageContext);
    hr.setPageContext(this.pageContext);
    try {
      row.doStartTag();

      hr.doStartTag();
      hr.doEndTag();

      this.pageContext.getOut().write("<div style=\"float: right\">\n");
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_BODY_INCLUDE;
  }

  @Override
  public int doEndTag() throws JspException {
    RowTag row = new RowTag();
    ButtonTag close = new ButtonTag();
    row.setPageContext(this.pageContext);
    close.setPageContext(this.pageContext);
    close.setOnClick("WindowMgr.closeMe()");
    close.setLabel(UIMgr.getText("btn.close"));
    close.setPad(true);
    try {
      close.doStartTag();
      close.doEndTag();
      this.pageContext.getOut().write("</div>\n");
      row.doEndTag();
    } catch (IOException e) {
      throw new JspException(e);
    }
    return EVAL_PAGE;
  }

}
